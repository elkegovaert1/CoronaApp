package MixingProxy;

import java.io.Serializable;

import javax.xml.bind.DatatypeConverter;


public class Capsule implements Serializable{
	private static final long serialVersionUID = 32418571595068622L;
	private int timeHour;
	private byte[] visitorToken;
	private byte[] catheringCode; //HRnym
	
	
	public Capsule(int time, byte[] token, byte[] code) {
		this.timeHour = time;
		visitorToken = token;
		catheringCode = code;
	}
	public int getTime() {
		return timeHour;
	}
	public void setTime(int time) {
		this.timeHour = time;
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
	@Override
	public String toString() {
		return (this.timeHour + " " + DatatypeConverter.printHexBinary(visitorToken));
	}
}
