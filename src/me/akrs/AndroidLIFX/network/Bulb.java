package me.akrs.AndroidLIFX.network;

import me.akrs.AndroidLIFX.utils.BulbStatus;
import me.akrs.AndroidLIFX.utils.MacAddress;

public class Bulb {
	private MacAddress macAddress;
	private short hue;
	private short saturation;
	private short luminance;
	private short temperature;
	private BulbStatus status;
	private String name;
	
	public Bulb (MacAddress mac){
		this.macAddress = mac;
	}
	
	public MacAddress getMacAddress () {
		return macAddress;
	}
	
	public String toString () {
		return "Bulb: Name:" + this.getName() + " Mac:" + this.macAddress.toString();
	}

	public short getHue () {
		return hue;
	}

	public void setHue (short hue) {
		this.hue = hue;
	}

	public short getSaturation () {
		return saturation;
	}

	public void setSaturation (short saturation) {
		this.saturation = saturation;
	}

	public short getLuminance() {
		return luminance;
	}

	public void setLuminance (short luminance) {
		this.luminance = luminance;
	}

	public short getTemperature () {
		return temperature;
	}

	public void setTemperature (short temperature) {
		this.temperature = temperature;
	}

	public BulbStatus getStatus () {
		return status;
	}

	public void setStatus (BulbStatus status) {
		this.status = status;
	}

	public String getName () {
		return name;
	}

	public void setName (String name) {
		this.name = name;
	}
	
	public boolean equals (Object o) {
		if (o == null || !(o instanceof Bulb)) {
			return false;
		}
		
		Bulb b = (Bulb)o;
		return this.macAddress.equal(b.macAddress);
	}
}
