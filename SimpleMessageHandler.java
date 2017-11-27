import java.net.InetAddress;

/**
 * This interface declares the basic functions of any MessageHandler.
 * 
 */
public interface SimpleMessageHandler extends Runnable{

    public void run();        

    public void handleMessage(String m, InetAddress ip_address);

    public void setMuxDemux(MuxDemuxSimple new_demux);

}
