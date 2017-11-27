import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.util.Arrays;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.util.HashMap;

public class FileReceiver implements Runnable{

    private SynchronizedListQueue incoming = new SynchronizedListQueue();
    private MuxDemuxSimple myMuxDemux = null;
    private HashMap<String, InetAddress> ip_addresses = new HashMap<String, InetAddress>();
    private HashMap<String, String> peer_ids = new HashMap<String, String>();
    private String root_folder;

    public FileReceiver(String rootFolder){
        root_folder = rootFolder;
    }

    public void setMuxDemux(MuxDemuxSimple md){
        myMuxDemux = md;
    }

    public void handleFile(String peerID, String file_name, InetAddress ip_address){
        if (!incoming.contains_element(file_name)) {
            incoming.enqueue(file_name);
            ip_addresses.put(file_name, ip_address);
            peer_ids.put(file_name, peerID);
        }
    }

    public void run(){
        while (true){

            // /////////////////////////////////////////////////////////////////////////////////////////////
            // FileReceiver only dequeue files, request and download them
            // /////////////////////////////////////////////////////////////////////////////////////////////
            try{
                String file_name = incoming.dequeue();
                InetAddress ip_to_request = ip_addresses.get(file_name);
                System.out.println("============================================================ Dequeueing: " + file_name + " | " + ip_to_request);

                
                String message_to_send = "get " + file_name + "\n";
                Socket sock = new Socket(ip_to_request, 4242);
                sock.getOutputStream().write(message_to_send.getBytes("UTF-8"));

                // // receive file
                // byte [] mybytearray  = new byte [FILE_SIZE];
                // InputStream is = sock.getInputStream();
                // fos = new FileOutputStream(FILE_TO_RECEIVED);
                // bos = new BufferedOutputStream(fos);
                // bytesRead = is.read(mybytearray,0,mybytearray.length);
                // current = bytesRead;

                // do {
                //    bytesRead =
                //       is.read(mybytearray, current, (mybytearray.length-current));
                //    if(bytesRead >= 0) current += bytesRead;
                // } while(bytesRead > -1);

                // bos.write(mybytearray, 0 , current);
                // bos.flush();


                try {
                  Thread.sleep(200);
                } catch(InterruptedException ex) {
                  Thread.currentThread().interrupt();
                }

            } catch (NoSuchElementException e){
                // Nothing in queue
            } catch (RuntimeException e){
                // Message received is not a Hello, so just ignore it
            } catch (IOException e){
                // Just in case of connection problems
            }

        }
    }
}