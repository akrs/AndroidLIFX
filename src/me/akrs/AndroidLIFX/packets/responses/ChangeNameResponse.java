package me.akrs.AndroidLIFX.packets.responses;

public class ChangeNameResponse extends StandardResponse {
	private String name;

	public ChangeNameResponse (byte[] packet) {
		super(packet);
		
		StringBuilder sb = new StringBuilder();
		for(int i = 36; i < this.getLength(); i++){
			sb.append((char)packet[i]);
		}
		
		this.name = sb.toString().trim();
	}

	public String getName() {
		return name;
	}
}
