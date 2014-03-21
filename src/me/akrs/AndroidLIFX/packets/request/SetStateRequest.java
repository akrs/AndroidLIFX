package me.akrs.AndroidLIFX.packets.request;

import me.akrs.AndroidLIFX.packets.PacketType;
import me.akrs.AndroidLIFX.utils.MacAddress;
import me.akrs.AndroidLIFX.utils.java.Logger;

public class SetStateRequest extends StandardRequest {
	
	private short hue;
	private short saturation;
	private short luminance;
	private short whiteColor;
	private short fadeTime;
	
	public SetStateRequest (MacAddress mac, MacAddress gatewayMac, short hue, short saturation, short luminance, short whiteColor, short fadeTime) { //If mac == null the message will be broadcasted
		super(mac, gatewayMac, PacketType.SET_STATE_REQUEST, 48);
		Logger.log("Setting state for bulb with mac: " + mac, Logger.DEBUG);
		Logger.log("Brightness is " + luminance, Logger.DEBUG);
		this.hue = hue;
		this.saturation = saturation;
		this.luminance = luminance;
		this.whiteColor = whiteColor;
		this.fadeTime = fadeTime;
	}

	@Override
	public byte getByte (int position) {
		if (position <= 32) {return super.getByte(position);} //The standard request takes care of most things
		
		
		//this.***** >>> 8*(position-**) is the way used to get the bytes from the short. >>> is unsigned shift of bits.
		switch (position) {
		case 42: case 43:
			return (byte)(this.hue >>> 8*(position-37));
		case 44: case 45:
			return (byte)(this.saturation >>> 8*(position-39));
		case 46: case 47:
			return (byte)(this.luminance >>> 8*(position-41));
		case 48: case 49:
			return (byte)(this.whiteColor >>> 8*(position-43));
		case 50: case 51:
			return (byte)(this.fadeTime >>> 8*(position-45));
		}
		
		return 0;
	}

	public byte[] getBytes() {
		byte[] bytes = new byte[this.getLength()];
		
		//Loop throw all bytes and add them to the byte array
		for(int i=0;i<this.getLength();i++){
			bytes[i] = this.getByte(i);
		}
		
		return bytes;
	}
}
