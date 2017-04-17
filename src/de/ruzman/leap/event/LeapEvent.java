package de.ruzman.leap.event;

import com.leapmotion.leap.Frame;

public final class LeapEvent {
	private Frame frame;
	
	private boolean hasDisconnected;
	private boolean hasExited;
	private boolean isFocusLost;
	
	protected void setDisconnected(boolean hasDisconnected) {
		this.hasDisconnected = hasDisconnected;
	}
	
	public boolean hasDisconnected() {
		return hasDisconnected;
	}
	
	protected void setExited(boolean hasExited) {
		this.hasExited = hasExited;
	}
	
	public boolean hasExited() {
		return hasExited;
	}
	
	public void setFocusLost(boolean isFocusLost) {
		this.isFocusLost = isFocusLost;
	}
	
	public boolean isFocusLost() {
		return isFocusLost;
	}
	
	protected void setFrame(Frame frame) {
		this.frame = frame;
	}
	
	public Frame getFrame() {
		return frame;
	}
}
