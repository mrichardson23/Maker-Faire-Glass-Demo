package com.mattrichardson.makerfaire;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.util.Log;

public class UDPClient implements Runnable {

	/*
	 * Usage to send UDP Packet: new Thread(new UDPClient("182.168.1.15",
	 * "4444", "Hello, world!")).start(); new Thread(new UDPClient(String ip,
	 * String port, string message)).start();
	 */

	private String udpIp;
	private int udpPort;
	private String message;

	public UDPClient(String _udpIp, String _udpPort, String _message) {
		this.udpIp = _udpIp;
		this.udpPort = Integer.parseInt(_udpPort);
		this.message = _message;
	}

	@Override
	public void run() {
		try {
			// Retrieve the ServerName
			InetAddress serverAddr = InetAddress.getByName(udpIp);
			DatagramSocket socket = new DatagramSocket();

			/* Prepare some data to be sent. */
			byte[] buf = (message).getBytes();

			/*
			 * Create UDP-packet with data & destination(url+port)
			 */
			DatagramPacket packet = new DatagramPacket(buf, buf.length,
					serverAddr, udpPort);

			/* Send out the packet */
			socket.send(packet);
			Log.d("UDP", "Sent: '" + new String(buf) + "'");

		} catch (Exception e) {
			Log.e("UDP", "C: Error", e);
		}
	}
}