package me.akrs.AndroidLIFX.utils.android;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import me.akrs.AndroidLIFX.network.BulbNetwork;
import me.akrs.AndroidLIFX.packets.request.DiscoveryRequest;
import me.akrs.AndroidLIFX.packets.responses.StandardResponse;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;

public class Discoverer {
	private Context mContext;
	private BulbNetwork network;
	private FinderThread finder;

	public Discoverer (Context c) {
		this.mContext = c;
		try {
			this.finder = new FinderThread();
		} catch (IOException e) {
			Logger.log("Unable to start finder thread", e);
		}
	}

	public void startSearch () {
		this.finder.start();
	}

	public void stopSearch () {
		this.finder.stopDiscovery();
	}

	public BulbNetwork getBulbNetwork () {
		return this.network;
	}

	InetAddress getBroadcastAddress() throws IOException {
		WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();
		if (dhcp == null) {
			Logger.log("Unable to get DHCP info", Logger.ERROR);
		}

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++) {
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		}
		return InetAddress.getByAddress(quads);
	}

	private class FinderThread extends Thread {
		private boolean discovering;

		public FinderThread() throws IOException {

		}

		public void run () {
			InetAddress broadcast = null;

			try {
				broadcast = getBroadcastAddress();
			} catch (IOException e) {
				Logger.log("Failed to get broadcast address", e);
			}
			DatagramSocket serverSocket = null;
			try {
				serverSocket = new DatagramSocket(56700);
			} catch (SocketException e) {
				Logger.log("Failed to open discovery socket", e);
			}

			try {
				serverSocket.setBroadcast(true);
			} catch (SocketException e) {
				Logger.log("Failed to set broadcast", e);
			}

			WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
			MulticastLock mlock = wifi.createMulticastLock("me.akrs.AndroidLIFX");
			mlock.acquire();

			byte[] receiveData = new byte[0xFF];
			byte[] data;

			DiscoveryRequest req = new DiscoveryRequest();
			DatagramPacket pack = new DatagramPacket(req.getBytes(), req.getBytes().length, broadcast, 56700);
			try {
				serverSocket.send(pack);
				Logger.log("Sent discovery request", Logger.DEBUG);
			} catch (IOException e1) {
				Logger.log("Failed to send broadcast", e1);
			}


			this.discovering = true;
			while (this.discovering) {
				DatagramPacket receivePacket =
						new DatagramPacket(receiveData, receiveData.length);
				try {
					serverSocket.receive(receivePacket);
				} catch (IOException e) {
					Logger.log("Failed to recieve discovery response", e);
				}

				data = receivePacket.getData();

				InetAddress ip = receivePacket.getAddress();

				StandardResponse res = new StandardResponse(data);

				switch(res.getPacketType()){
				case DISCOVER_RESPONSE:
					network.addBulb(res.getTargetBulb(),res.getGatewayBulb(),ip);
					Logger.log("Got Response", Logger.DEBUG);
					notify();
					break;
				default:
					break;
				}
			}

			mlock.release();
			serverSocket.close();
		}

		public void stopDiscovery () {
			this.discovering = false;
		}
	}
}