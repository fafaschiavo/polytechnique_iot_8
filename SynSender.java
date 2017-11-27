import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SynSender implements SimpleMessageHandler{

    private MuxDemuxSimple myMuxDemux = null;
    private String myID;

    public SynSender(String constructor_ID){
        myID = constructor_ID;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m, InetAddress ip_address){
    }
	
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // I opted for a thread that periodically verify the peers states and send a SYN if inconsistent
    // 
    // This decision was made to avoid problems with SYN messages being lost and that couls stuck a
    // peer in a forevert inconsistent state.
    //
    // That way I also ensure a minimun interval between two SYN requests
    // 
    // To avoid an avalanche of incoming LIST messages I also decided to establish a minimunn interval
    // between two syn requests.
    // ///////////////////////////////////////////////////////////////////////////////////////////////
    public void run(){
        while (true){

            HashMap<String, Integer> invalid_peers = myMuxDemux.get_inconsistent_peers();

            for (Map.Entry<String, Integer> entry : invalid_peers.entrySet())
            {
                String peer_id = entry.getKey();
                if (!peer_id.equals(myID)) {                
                    int peer_available_sequence_number = entry.getValue();
                    SynMessage new_message = new SynMessage(myID, peer_id, peer_available_sequence_number);
                    String encoded_new_message = new_message.getSynMessageAsEncodedString();
                    myMuxDemux.send(encoded_new_message);

                    try {
                      Thread.sleep(100);
                    } catch(InterruptedException ex) {
                      Thread.currentThread().interrupt();
                    }
                }
            }

            try {
              Thread.sleep(1000);
            } catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
            }

        }
    }
}