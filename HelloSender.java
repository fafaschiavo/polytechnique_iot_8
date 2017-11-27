import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.Arrays;

public class HelloSender implements SimpleMessageHandler{

    private SynchronizedListQueue incoming = new SynchronizedListQueue();
    private MuxDemuxSimple myMuxDemux = null;
    private String myID;
    private int sequence_number = 0;
    private int hello_interval = 10;


    public HelloSender(String constructor_ID){
        myID = constructor_ID;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleMessage(String m, InetAddress ip_address){
    }
	
    public void run(){
        while (true){

            // /////////////////////////////////////////////////////////////////////////////////////////////
            // HelloSender only generate messages
            // /////////////////////////////////////////////////////////////////////////////////////////////

            sequence_number = myMuxDemux.get_self_sequence_number();
            HelloMessage new_message = new HelloMessage(myID, sequence_number, hello_interval);
            String[] valid_peers = myMuxDemux.get_valid_peers();
            for (int i=0; i < valid_peers.length ; i++) {
                new_message.addPeer(valid_peers[i]);
            }
            String encoded_new_message = new_message.getHelloMessageAsEncodedString();
            // System.out.println("HelloSender sent: " + encoded_new_message);
            myMuxDemux.send(encoded_new_message);
            try {
              Thread.sleep(10000);
            } catch(InterruptedException ex) {
              Thread.currentThread().interrupt();
            }
        }
    }
}