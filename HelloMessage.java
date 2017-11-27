import java.util.*;

public class HelloMessage {

	private String sender_ID;
	private int sequence_number;
	private int hello_interval;
	private int number_of_peers = 0;
	private List<String> peers_array = new ArrayList<String>();

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This constructor will be necessary to transform raw hello messages into usable objects.
	// Since I am not in controll of what others can send in the link, I better check if the package is well formatted, and if not, throw an exception.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public HelloMessage(String s) {
		// This constructor takes a string formatted as bellow, and populates the attributes of the HelloMessage object accordingly.
		// HELLO;senderID;sequence#;HelloInterval;NumPeers;peer1;peer2;….;peerN
		String[] content_array = s.split(";");

		if (content_array[0].toLowerCase().equals("hello")) {

			if (content_array.length < 5) {
				throw new java.lang.RuntimeException("Not enough arguments in the HELLO String");
			}
			if (content_array.length > 260) {
				throw new java.lang.RuntimeException("Too many arguments in the HELLO String");
			}

			try {
				sender_ID = content_array[1];
				sequence_number = Integer.parseInt(content_array[2].trim());
				hello_interval = Integer.parseInt(content_array[3].trim());
				number_of_peers = Integer.parseInt(content_array[4].trim());

				if (number_of_peers > 0) {
					String[] current_peers_array;
					current_peers_array = Arrays.copyOfRange(content_array, 5, content_array.length);
					for (int i = 0; i < current_peers_array.length; i++){
						peers_array.add(current_peers_array[i]);
					}
				}
			} catch (Exception e){
				System.out.println(e);
				throw new java.lang.RuntimeException("I had an error while creating the message...");
			}

		}else{
			throw new java.lang.RuntimeException("Not a valid HELLO String");
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This method will probably be used to create my own hello messages.
	// I dont have to be too rigorous since I know in which context this constructor will be called.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public HelloMessage(String senderID, int sequenceNo, int HelloInterval) {
		// This constructor creates a HelloMessage object which has (at the time of creation) no peers associated.
		sender_ID = senderID;
		sequence_number = sequenceNo;
		hello_interval = HelloInterval;
	}

	public String getHelloMessageAsEncodedString(){
		// This Method will return a string of the format indicated above, encoding the attributes of the HelloMessage object, all ready to be sent out over the network.
		String encoded_message = "HELLO";
		encoded_message = encoded_message + ";" + sender_ID + ";" + Integer.toString(sequence_number) + ";" + Integer.toString(hello_interval) + ";" + Integer.toString(number_of_peers);
		if (number_of_peers > 0) {
			for (String element : peers_array) {
				encoded_message = encoded_message + ";" + element;
			}
		}

		return encoded_message;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This method will probably be used to create my own hello messages with the peers I will have in my peers array.
	// Just to be sure, I better check if the max ammount of peers was exceeded for this particular message.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void addPeer(String peerID){
		// This Method will add a peer to the HelloMessage object.
		if (peers_array.size() < 256) {		
			number_of_peers = number_of_peers + 1;
			peers_array.add(peerID);
		}else{
			throw new java.lang.RuntimeException("Too many peers in list already...");
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Fancy way to print my HelloMessage ccontent
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public String toString(){
		String encoded_message = "HELLO";
		encoded_message = encoded_message + ";" + sender_ID + ";" + Integer.toString(sequence_number) + ";" + Integer.toString(hello_interval) + ";" + Integer.toString(number_of_peers);
		if (number_of_peers > 0) {
			for (String element : peers_array) {
				encoded_message = encoded_message + ";" + element;
			}
		}
		return encoded_message;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// GET Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public String getSenderID(){
		return sender_ID;
	}

	public int getSequenceNumber(){
		return sequence_number;
	}

	public int getHelloInterval(){
		return hello_interval;
	}

	public int getNumberOfPeers(){
		return number_of_peers;
	}

	public List<String> getPeersArray(){
		return peers_array;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// SET Methods
	// It doesn't make sense for me to offer a set method for the number_of_peers and peers_array
	// since they already have the more reliable method addPeer() for that.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void setSenderID(String senderID){
		sender_ID = senderID;
	}

	public void setSequenceNumber(int sequenceNo){
		sequence_number = sequenceNo;
	}

	public void setHelloInterval(int HelloInterval){
		hello_interval = HelloInterval;
	}

}