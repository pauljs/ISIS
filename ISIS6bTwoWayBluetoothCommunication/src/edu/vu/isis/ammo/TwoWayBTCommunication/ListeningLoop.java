package edu.vu.isis.ammo.TwoWayBTCommunication;

import java.io.InputStream;

import android.bluetooth.BluetoothSocket;

// This class is used by the Zephyr Main Activity
public class ListeningLoop extends Thread {
	BluetoothSocket socket;
	InputStream input;
	byte[] buffer;
	
	public ListeningLoop(BluetoothSocket socket) {
		socket = socket;
		try {
			input = socket.getInputStream();
		} catch (Exception e) {
			
		}
		buffer = new byte[10000]; 
		
	}
	
	@Override
	public void run() { // this the thread that continuously listens for data
		while (true) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				
			}
			try {
				int bytesRead = input.read(buffer); // whatever is being sent is put in the array buffer
				if (bytesRead != 0) { //means we got something, so we are going to handle whatever we got
					//look at byte buffer class for appending
				}
			} catch (Exception e) {
				
			}
		}
	}
}
