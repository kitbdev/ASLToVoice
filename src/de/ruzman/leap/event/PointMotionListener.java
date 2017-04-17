package de.ruzman.leap.event;

public interface PointMotionListener extends IListener {
	public void enteredViewoport(PointEvent event);
	public void moved(PointEvent event);
	public void leftViewport(PointEvent event);
}