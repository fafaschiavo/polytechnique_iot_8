import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

public class ListSender implements SimpleMessageHandler{

	private SynchronizedListQueue incoming_sync_requests = new SynchronizedListQueue();
	private MuxDemuxSimple myMuxDemux = null;
	private String myID;

    public ListSender(String constructor_ID){
        myID = constructor_ID;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m, InetAddress ip_address){
        incoming_sync_requests.enqueue(m);
    }

    public void run(){
        while (true){

            try{
                String msg = incoming_sync_requests.dequeue();
                SynMessage new_sync_request = new SynMessage(msg);
	            String peerID = new_sync_request.getSenderID();
	            String destinationID = new_sync_request.getPeerID();

	            if (!peerID.equals(myID)) {
		            Vector local_dump = myMuxDemux.get_self_database_dump();
		            int my_sequence_number = myMuxDemux.get_self_sequence_number();
		            int total_parts = local_dump.size();
		            int current_part = 0;
		            Iterator itr = local_dump.iterator();
		            while(itr.hasNext()){
		            	String next_database_chunk = (String) itr.next();
		            	ListMessage new_list_request = new ListMessage(myID, peerID, my_sequence_number, total_parts, current_part, next_database_chunk);
		            	current_part = current_part + 1;
		            	myMuxDemux.send(new_list_request.getListMessageAsEncodedString());
		            }	
	            }
            } catch (NoSuchElementException e){
                // System.out.println("Nothing in queue...");
            } catch (RuntimeException e){
                // Message received is not a Sync request, so just ignore it
            }

        }
    }

}