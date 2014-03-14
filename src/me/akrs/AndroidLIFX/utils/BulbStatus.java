package me.akrs.AndroidLIFX.utils;

public enum BulbStatus {
	ON,OFF,UNKNOWN;
	
	public static BulbStatus fromData(byte data){
		switch (data) {
		case (byte)0xFF:
			return ON;
		case 0x00:
			return OFF;
		default:
			return UNKNOWN;
		}
	}
}
