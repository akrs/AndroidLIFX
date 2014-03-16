package me.akrs.AndroidLIFX.network;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.packets.request.SetStateRequest;
import me.akrs.AndroidLIFX.packets.request.StatusRequest;
import me.akrs.AndroidLIFX.packets.responses.OnOffResponse;
import me.akrs.AndroidLIFX.packets.responses.StatusResponse;
import me.akrs.AndroidLIFX.utils.Logger;
import me.akrs.AndroidLIFX.utils.MacAddress;

public class BulbNetwork implements Closeable {
	
	private InetAddress gatewayAddress = null;
	private MacAddress gatewayMac;
	private List<Bulb> bulbList = Collections.synchronizedList(new ArrayList<Bulb>());
	
	private Socket gatewaySocket;
	private DataOutputStream gatewayOutStream;
	private DataInputStream gatewayInStream;
	
	private GatewayInputThread gatewayThread;
	
	private Timer statusRequesterTimer;

	public BulbNetwork () {
		
	}
	
	public void addBulb (MacAddress mac, MacAddress gatewayMac, InetAddress ip) {
		if (!bulbExist(mac)) {
			if (gatewayAddress == null) {
				this.gatewayAddress = ip;
				this.gatewayMac = gatewayMac;
				this.initiateConnection();
			}
			this.bulbList.add(new Bulb(mac));
		}
	}
	
	private void initiateConnection () {
	    try {
			gatewaySocket = new Socket(gatewayAddress, 56700);
			gatewayOutStream = new DataOutputStream(gatewaySocket.getOutputStream());
			gatewayInStream = new DataInputStream(gatewaySocket.getInputStream());
			
			//New thread for listening of the TCP connection
			gatewayThread = new GatewayInputThread(gatewayInStream);
			gatewayThread.start();
			
			//Send status request with fixed interval
			statusRequesterTimer = new Timer();
			statusRequesterTimer.schedule(statusRequester, 500,1000);
			
		} catch (IOException e) {
			Logger.log("Failed to initiate connection", e);
		}
	}
	
	public Iterator<Bulb> getBulbIterator () {
		return bulbList.iterator();
	}
	
	public boolean bulbExist (MacAddress mac) {
		Iterator<Bulb> itr = bulbList.iterator();
		
		while(itr.hasNext()){
			if(itr.next().getMacAddress().equal(mac)){
				return true;
			}
		}
		
		return false;
	}
	
	public int getNumberOfBulbs () {
		return bulbList.size();
	}
	
	public Bulb getBulbById (int id) {
		return bulbList.get(id);
	}
	
	public Bulb getBulb (MacAddress mac) {
		Iterator<Bulb> itr = bulbList.iterator();
		while(itr.hasNext()){
			Bulb bulb = itr.next();
			if(bulb.getMacAddress().equal(mac)){
				return bulb;
			}
		}
		return null;
	}
	
	public List<Bulb> getBulbList () {
		return bulbList;
	}
	
	
	//Commands
	// TODO: Move to Bulb class (After we've ported/understood SetStateRequest)
	//Set bulb state
	public void setState(short hue,short saturation,short luminance,short whiteColor,short fadeTime) throws IOException{
		if(gatewayAddress != null){
			gatewayOutStream.write((new SetStateRequest(null, gatewayMac, hue, saturation, luminance, whiteColor, fadeTime)).getBytes());
		}
	}
	
	
	private class GatewayInputThread extends Thread {
		
		private DataInputStream inputStream;
		
		public GatewayInputThread (DataInputStream inputStream) throws IOException {
			this.inputStream = inputStream;
        }

		public void run() {
        	while (true) {
				try {
					int length = inputStream.readByte();
					byte[] data = new byte[length];
					data[0] = (byte) length;
		    		for(int i=1;i<length;i++){
		    			data[i] = inputStream.readByte();
		    		}
		    		
		    		PacketType type = PacketType.fromData(data[32]);
		    		
		    		switch (type) {
		    		case ON_OFF_RESPONSE:
		    			OnOffResponse onOffResponse = new OnOffResponse(data);
		    			Bulb onOffBulb = getBulb(onOffResponse.getTargetBulb());
		    			onOffBulb.setStatus(onOffResponse.getStatus());
		    			break;
		    		case STATUS_RESPONSE:
		    			StatusResponse statusResponse = new StatusResponse(data);
		    			Bulb statusBulb = getBulb(statusResponse.getTargetBulb());
		    			statusBulb.setHue(statusResponse.getHue());
		    			statusBulb.setLuminance(statusResponse.getLuminance());
		    			statusBulb.setName(statusResponse.getName());
		    			statusBulb.setSaturation(statusResponse.getSaturation());
		    			statusBulb.setTemperature(statusResponse.getTemperature());
		    			break;
		    		case LABEL_RESPONSE:
		    			StatusResponse changeNameResponse = new StatusResponse(data);
		    			Bulb changeNameBulb = getBulb(changeNameResponse.getTargetBulb());
		    			changeNameBulb.setName(changeNameResponse.getName());
		    			break;
					default:
						break;
		    		}
		    		
				} catch (IOException e) {
					Logger.log("Failed to receive response", e);
				}
	        }
        }
	}
	
	
	TimerTask statusRequester = new TimerTask(){
	    public void run() {
	    	if(gatewayAddress != null){
	    		try {
					gatewayOutStream.write((new StatusRequest(gatewayMac)).getBytes());
				}
	    		catch (IOException e) {
	    			Logger.log("Unable to send perodic status request", e);
	    		}
	    	}
	    }
	};
	
	public String toString () {
		StringBuilder sb = new StringBuilder();
		sb.append("LIFXNetwork:\nGateway IP: ");
		sb.append(gatewayAddress);
		sb.append("\nGateway MAC: ");
		sb.append(gatewayMac);
		sb.append("\n\nBulbs:\n");
		
		Iterator<Bulb> itr = this.getBulbIterator();
		int i = 0;
		while(itr.hasNext()){
			sb.append(++i);
			sb.append(") ");
			sb.append(itr.next());
			sb.append("\n");
		}
		
		return sb.toString();
	}


	public void close () throws IOException {
		gatewaySocket.close();
		
	}

}