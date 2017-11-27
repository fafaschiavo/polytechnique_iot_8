import java.util.*;

public class ListMessage {

	String sender_id;
	String peer_id;
	int sequence_number;
	int total_parts;
	int part;
	String message_data;

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This constructor will be necessary to transform raw List messages into usable objects.
	// Since I am not in controll of what others can send in the link, I better check if the package is well formatted, and if not, throw an exception.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public ListMessage(String s) {
		// LIST;senderID;peerID;sequence#;TotalParts;part#;data;
		String[] content_array = s.split(";");

		if (content_array[0].toLowerCase().equals("list")) {

			if (content_array.length < 7) {
				throw new java.lang.RuntimeException("Not enough arguments in the LIST String");
			}

			try {
				sender_id = content_array[1];
				peer_id = content_array[2];
				sequence_number = Integer.parseInt(content_array[3].trim());
				total_parts = Integer.parseInt(content_array[4].trim());
				part = Integer.parseInt(content_array[5].trim());
				message_data = content_array[6];
				if (part > total_parts-1) {
					throw new java.lang.RuntimeException("The current list index is bigger than the total amount of parts");
				}
			} catch (Exception e){
				System.out.println(e);
				throw new java.lang.RuntimeException("I had an error while creating the message...");
			}

		}else{
			throw new java.lang.RuntimeException("Not a valid LIST String");
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This method will probably be used to create my own List messages.
	// I dont have to be too rigorous since I know in which context this constructor will be called.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public ListMessage(String senderId, String peerId, int sequenceNumber, int totalParts, int currentPart, String messageData){
		sender_id = senderId;
		peer_id = peerId;
		sequence_number = sequenceNumber;
		total_parts = totalParts;
		part = currentPart;
		message_data = messageData;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// This Method will return a string of the format established, encoding the attributes of the ListMessage object,
	// all ready to be sent out over the network.
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public String getListMessageAsEncodedString(){
		String encoded_message = "LIST";
		encoded_message = encoded_message + ";" + sender_id + ";" + peer_id + ";" + Integer.toString(sequence_number) + ";" + Integer.toString(total_parts) + ";" + Integer.toString(part) + ";" + message_data + ";";
		return encoded_message;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Fancy way to print my ListMessage ccontent
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public String toString(){
		String encoded_message = "LIST";
		encoded_message = encoded_message + ";" + sender_id + ";" + peer_id + ";" + Integer.toString(sequence_number) + ";" + Integer.toString(total_parts) + ";" + Integer.toString(part) + ";" + message_data + ";";
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

	public int getTotalParts(){
		return total_parts;
	}

	public int getPart(){
		return part;
	}

	public String getMessageData(){
		return message_data;
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

	public void setTotalParts(int totalParts){
		total_parts = totalParts;
	}

	public void setPart(int currentPart){
		part = currentPart;
	}

	public void SetMessageData(String messageData){
		message_data = messageData;
	}

}