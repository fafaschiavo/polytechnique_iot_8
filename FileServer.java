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
import java.io.DataOutputStream;

public class FileServer implements Runnable{

    private String folder_to_serve;

    public FileServer(String my_local_folder){
        folder_to_serve = my_local_folder;
    }

    private void handleConnection(Socket client_socket){
        try{

            InputStreamReader isr =  new  InputStreamReader(client_socket.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            String line = reader.readLine();
            String requested_path = "";
            BufferedInputStream bis = null;
            OutputStream os = null;
            while (!line.isEmpty()) {                
                if (line.toLowerCase().startsWith("get")) {
                    String file_to_serve = line.split(" ")[1].split("\n")[0];

                    File folder = new File(folder_to_serve);
                    File[] listOfFiles = folder.listFiles();
                    for (int i = 0; i < listOfFiles.length; i++) {
                        if (listOfFiles[i].isFile() && listOfFiles[i].getName().equals(file_to_serve)) {

                            System.out.println("Now Serving: " + file_to_serve);
                            String message_to_send = file_to_serve+"\n";

                            client_socket.getOutputStream().write(message_to_send.getBytes("UTF-8"));
                            
                            Long file_size = listOfFiles[i].length();
                            message_to_send = Long.toString(file_size)+"\n";
                            client_socket.getOutputStream().write(message_to_send.getBytes("UTF-8"));

                            DataOutputStream dos = new DataOutputStream(client_socket.getOutputStream());
                            FileInputStream fis = new FileInputStream(listOfFiles[i]);
                            byte[] buffer = new byte[4096];
                            
                            while (fis.read(buffer) > 0) {
                                dos.write(buffer);
                                dos.flush();
                            }
                            
                            fis.close();
                            dos.close();

                        }
                    }
                }

                line = reader.readLine();
            }

        } catch (Exception e){
            System.err.println(e);
        }

    }
    
    public void run(){
        try{
            int port_to_listen = 4242;
            final ServerSocket server = new ServerSocket(port_to_listen);
            System.out.format("File server listening for GET requests on port %d .... \n", port_to_listen);
            while(true){
                final Socket client_socket = server.accept();
                handleConnection(client_socket);
            }
        } catch (Exception e){
            System.err.println(e);
        }
    }
}