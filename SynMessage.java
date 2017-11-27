import java.util.*;

public class SynMessage {

	String sender_id;
	String peer_id;
	int sequence_number;

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This constructor will be necessary to transform raw syn messages into usable objects.
	// Since I am not in controll of what others can send in the link, I better check if the package is well formatted, and if not, throw an exception.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public SynMessage(String s) {
		String[] content_array = s.split(";");

		if (content_array[0].toLowerCase().equals("syn")) {

			if (content_array.length < 4) {
				throw new java.lang.RuntimeException("Not enough arguments in the SYN String");
			}

			try {
				sender_id = content_array[1];
				peer_id = content_array[2];
				sequence_number = Integer.parseInt(content_array[3].trim());

			} catch (Exception e){
				System.out.println(e);
				throw new java.lang.RuntimeException("I had an error while creating the message...");
			}

		}else{
			throw new java.lang.RuntimeException("Not a valid SYN String");
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This method will probably be used to create my own Syn messages.
	// I dont have to be too rigorous since I know in which context this constructor will be called.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public SynMessage(String senderId, String peerId, int sequenceNumber){
		sender_id = senderId;
		peer_id = peerId;
		sequence_number = sequenceNumber;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This Method will return a string of the format established, encoding the attributes of the SynMessage object,
	// all ready to be sent out over the network.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public String getSynMessageAsEncodedString(){
		String encoded_message = "SYN";
		encoded_message = encoded_message + ";" + sender_id + ";" + peer_id + ";" + Integer.toString(sequence_number) + ";";
		return encoded_message;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Fancy way to print my SynMessage ccontent
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public String toString(){
		String encoded_message = "SYN";
		encoded_message = encoded_message + ";" + sender_id + ";" + peer_id + ";" + Integer.toString(sequence_number) + ";";
		return encoded_message;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// GET Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public String getSenderID(){
		return sender_id;
	}

	public String getPeerID(){
		return peer_id;
	}

	public int getSequenceNumber(){
		return sequence_number;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// SET Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void setSenderID(String senderID){
		sender_id = senderID;
	}

	public void setPeerID(String peerID){
		peer_id = peerID;
	}

	public void setSequenceNumber(int sequenceNo){
		sequence_number = sequenceNo;
	}

}