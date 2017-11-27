import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

public class ListReceiver implements SimpleMessageHandler{

	private SynchronizedListQueue incoming_list_messages = new SynchronizedListQueue();
	private MuxDemuxSimple myMuxDemux = null;
	private String myID;
	private HashMap<String, Integer> peer_sync_total = new HashMap<String, Integer>();
	private HashMap<String, Integer> peer_sync_current = new HashMap<String, Integer>();
	private HashMap<String, Integer> peer_sync_sequence_number = new HashMap<String, Integer>();

    public ListReceiver(String constructor_ID){
        myID = constructor_ID;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m, InetAddress ip_address){
        incoming_list_messages.enqueue(m);
    }

    public void run(){
        while (true){

            try{
                String msg = incoming_list_messages.dequeue();
                ListMessage new_list_request = new ListMessage(msg);


                if (new_list_request.getPeerID().equals(myID) && !new_list_request.getSenderID().equals(myID)) {
                    String sender_id = new_list_request.getSenderID();
                    int total_chunks = new_list_request.getTotalParts();
                    int current_chunk = new_list_request.getPart();
                    int peer_database_sequence_number = new_list_request.getSequenceNumber();
                    String current_content = new_list_request.getMessageData();

					if (peer_sync_total.get(sender_id) == null) {
                        peer_sync_total.put(sender_id, total_chunks);
                        peer_sync_current.put(sender_id, 0);
                        peer_sync_sequence_number.put(sender_id, peer_database_sequence_number);
                        myMuxDemux.clear_database(sender_id);

						if (peer_sync_current.get(sender_id) == current_chunk) {
							myMuxDemux.add_to_database(sender_id, current_content);
							peer_sync_current.put(sender_id, peer_sync_current.get(sender_id) + 1);
						}

					}else{

						if (peer_sync_current.get(sender_id) == current_chunk) {
							myMuxDemux.add_to_database(sender_id, current_content);
							peer_sync_current.put(sender_id, peer_sync_current.get(sender_id) + 1);
						}

					}

					if (peer_sync_current.get(sender_id) == (peer_sync_total.get(sender_id) - 1)) {
                        peer_sync_total.remove(sender_id);
                        peer_sync_current.remove(sender_id);
                        peer_sync_sequence_number.remove(sender_id);
						myMuxDemux.set_database_sequence_number(sender_id, peer_database_sequence_number);
						myMuxDemux.set_peer_as_synchronized(sender_id, peer_database_sequence_number);
					}

                }else{
                	// The message is not for me, simply ignore it...
                }
            } catch (NoSuchElementException e){
                // Nothing in queue to process...
            } catch (RuntimeException e){
                // Message received is not a Sync request, so just ignore it
            }

        }
    }

}