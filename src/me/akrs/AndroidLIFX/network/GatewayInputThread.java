package me.akrs.AndroidLIFX.network;

import java.io.DataInputStream;
import java.io.IOException;

import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.packets.responses.OnOffResponse;
import me.akrs.AndroidLIFX.packets.responses.StatusResponse;
import me.akrs.AndroidLIFX.utils.Logger;

class GatewayInputThread extends Thread {

	private DataInputStream inputStream;
	private BulbNetwork net;
	private boolean running;

	public GatewayInputThread (DataInputStream inputStream, BulbNetwork net) throws IOException {
		this.inputStream = inputStream;
		this.net = net;
	}

	public void run() {
		this.running = true;
		while (running) {
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
					Bulb onOffBulb = net.getBulb(onOffResponse.getTargetBulb());
					onOffBulb.status = onOffResponse.getStatus();
					break;
				case STATUS_RESPONSE:
					StatusResponse statusResponse = new StatusResponse(data);
					Bulb statusBulb = net.getBulb(statusResponse.getTargetBulb());
					statusBulb.hue = statusResponse.getHue();
					statusBulb.luminance = statusResponse.getLuminance();
					statusBulb.name = statusResponse.getName();
					statusBulb.saturation = statusResponse.getSaturation();
					statusBulb.temperature = statusResponse.getTemperature();
					break;
				case LABEL_RESPONSE:
					StatusResponse changeNameResponse = new StatusResponse(data);
					Bulb changeNameBulb = net.getBulb(changeNameResponse.getTargetBulb());
					changeNameBulb.name = changeNameResponse.getName();
					break;
				default:
					break;
				}

			} catch (IOException e) {
				Logger.log("Failed to receive response", e);
			}
		}
	}
	
	public void cease () {
		this.running = false;
	}
}