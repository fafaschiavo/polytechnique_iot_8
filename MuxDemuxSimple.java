import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.concurrent.*;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

public class MuxDemuxSimple implements Runnable{

	private String myID = null;
    private DatagramSocket myS = null;
    private BufferedReader in;
    private SimpleMessageHandler[] myMessageHandlers;
    private SynchronizedListQueue outgoing = new SynchronizedListQueue();
    private Boolean reading_thread_up = false;
    private HashMap<String, Peer> peer_table = new HashMap<String, Peer>();
    private Database self_database;
    private HashMap<String, Database> peer_databases = new HashMap<String, Database>();
    private FileReceiver file_receciver = new FileReceiver("rootfolder/");

	MuxDemuxSimple(SimpleMessageHandler[] h, DatagramSocket s, String constructor_ID){
		myS = s;
		myMessageHandlers = h;
		myID = constructor_ID;
		self_database = new Database(myID);
		new Thread(file_receciver).start();
	}

	public void run(){
		if (reading_thread_up) {

			// /////////////////////////////////////////////////////////////////////////////////////////////
			// This is the writting loop thread 
			// /////////////////////////////////////////////////////////////////////////////////////////////
			System.out.println("Writting Thread Started...");
			while(true){
				try{
					String message_to_send = outgoing.dequeue();
					byte[] byteArray = message_to_send.getBytes();
					try{
						DatagramPacket dp = new DatagramPacket(byteArray, byteArray.length, InetAddress.getByName("255.255.255.255"), 4242);
						myS.send(dp);
					}catch (UnknownHostException e){
						System.err.println("Ops... Got an UnknownHostException error...");
					}catch (IOException e){
						System.err.println("Ops... Got an IOException error...");
					}

				} catch (NoSuchElementException e){}
			}

		}else{

			// /////////////////////////////////////////////////////////////////////////////////////////////
			// This is the reading loop thread 
			// /////////////////////////////////////////////////////////////////////////////////////////////
			reading_thread_up = true;
			for (int i=0; i<myMessageHandlers.length; i++){
				myMessageHandlers[i].setMuxDemux(this);
			}
			try{
				System.out.println("Reading Thread Started...");
				while(true){
					byte[] receiveData = new byte[1024];
					DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
					myS.receive(receivePacket);
					String message = new String( receivePacket.getData());
					InetAddress ip_address = receivePacket.getAddress();
					int port = receivePacket.getPort();

					for (int i=0; i<myMessageHandlers.length; i++){
						myMessageHandlers[i].handleMessage(message, ip_address);
					}
				}

			}catch (IOException e){ }		
			try{
				in.close();
				myS.close();
			}catch(IOException e){ }

		}
	}

	public void send(String s){
		outgoing.enqueue(s);
	}

	public void touch_new_peer(String new_peerID, InetAddress new_peerIPAddress, int new_peerSeqNum, int expiration_delay){
		if (!new_peerID.equals(myID)) {
			if (peer_table.get(new_peerID) == null) {
				Peer new_peer = new Peer(new_peerID, new_peerIPAddress, new_peerSeqNum, expiration_delay);
				peer_table.put(new_peerID, new_peer);
				Database new_database = new Database(new_peerID);
				peer_databases.put(new_peerID, new_database);
			}else{
				Peer existing_peer = peer_table.get(new_peerID);
				existing_peer.update_peer_state(new_peerSeqNum, expiration_delay);
			}
		}

	}

	public String[] get_valid_peers(){
		int valid_counter = 0;
		List<String> valid_peers_list = new ArrayList<String>();
		Iterator it = peer_table.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			if (!peer_table.get(pair.getKey()).is_peer_expired()) {
				valid_peers_list.add(peer_table.get(pair.getKey()).get_peer_id());
			}else{
				it.remove();
			}
		}

		String[] valid_peers_array = new String[valid_peers_list.size()];
		valid_peers_array = valid_peers_list.toArray(valid_peers_array);
		return valid_peers_array;
	}

	public HashMap<String, Integer> get_inconsistent_peers(){
		int valid_counter = 0;
		HashMap<String, Integer> inconsistent_peers_list = new HashMap<String, Integer>();
		Iterator it = peer_table.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			if (peer_table.get(pair.getKey()).is_peer_inconsistent()) {
				String peer_id = peer_table.get(pair.getKey()).get_peer_id();
				int peer_available_sequence_number = peer_table.get(pair.getKey()).get_peer_available_sequence_number();
				inconsistent_peers_list.put(peer_id, peer_available_sequence_number);
			}else{
				// The peer is synchronized or it already sent a sync request and it is waiting for the list messages...
			}

		}
		return inconsistent_peers_list;
	}

	public void set_peer_as_synchronized(String peerID, int seqNumber){
		peer_table.get(peerID).set_as_synchronized(seqNumber);
	}

	public int get_self_sequence_number(){
		int sequence_number = self_database.getInternalDatabaseSequenceNumber();
		return sequence_number;
	}

	public void add_to_self_database(String element){
		self_database.add_to_database(element);
	}

	public Vector get_self_database_dump(){
		return self_database.get_database_dump();
	}

	public int get_database_sequence_number(String peerID){
		return peer_databases.get(peerID).getDatabaseSequenceNumber();
	}

	public void set_database_sequence_number(String peerID, int seqNumber){
		peer_databases.get(peerID).set_new_sequence_number(seqNumber);
	}

	public void add_to_database(String peerID, String content){
		peer_databases.get(peerID).add_to_database(content);

		InetAddress ip_address = peer_table.get(peerID).get_peer_IP_address();
		file_receciver.handleFile(peerID, content, ip_address);
	}

	public void clear_database(String peerID){
		peer_databases.get(peerID).clear_database();
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: java MuxDemuxSimple YOUR_ID");
			System.exit(-1);
		}

		String myID = args[0];
		System.out.println("==========================================");
		System.out.println("Hi there! This computer's ID is: " + myID);
		System.out.println("==========================================");
		SimpleMessageHandler[] handlers = new SimpleMessageHandler[6];
		handlers[0] = new HelloSender(myID);
		handlers[1]= new HelloReceiver(myID);
		handlers[2]= new DebugReceiver();
		handlers[3]= new SynSender(myID);
		handlers[4]= new ListSender(myID);
		handlers[5]= new ListReceiver(myID);

		try {
			DatagramSocket mySocket = new DatagramSocket(4242);
			mySocket.setBroadcast(true);
			MuxDemuxSimple dm = new MuxDemuxSimple(handlers, mySocket, myID);
			handlers[0].setMuxDemux(dm);
			handlers[3].setMuxDemux(dm);
			handlers[4].setMuxDemux(dm);
			handlers[5].setMuxDemux(dm);
			new Thread(handlers[0]).start();
			new Thread(handlers[1]).start();
			new Thread(handlers[2]).start();
			new Thread(handlers[3]).start();
			new Thread(handlers[4]).start();
			new Thread(handlers[5]).start();

			FileServer file_server = new FileServer("rootfolder/mysharefilesfolder/");
			new Thread(file_server).start();

			// Launch reading Thread
			new Thread(dm).start();
			try {
			  Thread.sleep(100);
			} catch(InterruptedException ex) {}
			// Launch writting Thread
			new Thread(dm).start();

		} catch (SocketException e){
			System.err.println(e);
			System.err.println("Ops... Got an Socket exception error...");
		}
	}

}
