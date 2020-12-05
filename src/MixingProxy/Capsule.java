package MixingProxy;

import java.time.LocalDate;

public class Capsule {
	private LocalDate time;
	private byte[] visitorToken;
	private byte[] catheringCode;
	
	
	public Capsule(LocalDate time, byte[] token, byte[] code) {
		this.time = time;
		visitorToken = token;
		catheringCode = code;
	}
	public LocalDate getTime() {
		return time;
	}
	public void setTime(LocalDate time) {
		this.time = time;
	}
	public byte[] getVisitorToken() {
		return visitorToken;
	}
	public void setVisitorToken(byte[] visitorToken) {
		this.visitorToken = visitorToken;
	}
	public byte[] getCatheringCode() {
		return catheringCode;
	}
	public void setCatheringCode(byte[] catheringCode) {
		this.catheringCode = catheringCode;
	}
	
	
}
