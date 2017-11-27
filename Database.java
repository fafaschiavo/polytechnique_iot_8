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
import java.util.Vector;

public class Database{

	private Vector table = new Vector(); // Better use a Vector to ensure a thread safe class 
	private int sequence_number = -1;
	private String peerID;

	Database(String peer_id){
		peerID = peer_id;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Return current sequence number
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public int getDatabaseSequenceNumber(){
		return sequence_number;
	}

	private String serialize_table(){
		String current_serialized_vector = "";
		for (Object obj : table) {
			current_serialized_vector = current_serialized_vector + obj + ";";
		}
		return current_serialized_vector;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Return current sequence number - for our own database
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public int getInternalDatabaseSequenceNumber(){
		System.out.println("=============================== Let's check the folder");
		File folder = new File("rootfolder/mysharefilesfolder/");
		File[] listOfFiles = folder.listFiles();

		if (table.size() == 0 && sequence_number == -1) {
			sequence_number = 0;
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					table.add(listOfFiles[i].getName());
				}
			}
		}

		String old_serialized_table = serialize_table();

		String current_serialized_vector = "";
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				current_serialized_vector = current_serialized_vector + listOfFiles[i].getName() + ";";
			}
		}

		if (!old_serialized_table.equals(current_serialized_vector)) {
			sequence_number = sequence_number + 1;
			table.clear();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					table.add(listOfFiles[i].getName());
				}
			}
		}

		return sequence_number;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Empty the database, preparing it to receive the peer's dump
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void clear_database(){
		table.clear();
		sequence_number = -1;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Receives updates and increment sequence number
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void add_to_database(String element){
		table.add(element);
		sequence_number = sequence_number + 1;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Set new squence number - will be called after done receciving dump to establish current version 
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public void set_new_sequence_number(int seqNumber){
		sequence_number = seqNumber;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////
	// Return a dump of the current database - important for brain dumping myy ownn database to 
	// /////////////////////////////////////////////////////////////////////////////////////////////
	public Vector get_database_dump(){
		return table;
	}

}