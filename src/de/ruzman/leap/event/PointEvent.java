package de.ruzman.leap.event;

import java.util.EnumSet;

import com.leapmotion.leap.Vector;

import de.ruzman.leap.AbstractPoint;

public class PointEvent {
	public enum Zone {
		UP, DOWN, LEFT, RIGHT, FRONT, BACK, OUTSIDE, UNKOWN;
	}
	
	private EnumSet<Zone> prevZones;
	private EnumSet<Zone> zones;
	private EnumSet<Zone> clickZone;
	
	private Vector position;
	private Vector absolutePosition;
	
	private AbstractPoint source;
	
	public PointEvent(AbstractPoint source, EnumSet<Zone> zones, EnumSet<Zone> prevZones, EnumSet<Zone> clickZone) {
		this.source = source;
		this.zones = zones;
		this.prevZones = prevZones;
		this.clickZone = clickZone;
		this.position = source.getPosition();
		this.absolutePosition = source.getPoint();
	}
	
	public boolean enteredViewPort() {
		return wasInZone(Zone.UNKOWN);
	}
	
	public boolean leftViewPort() {
		return isInZone(Zone.UNKOWN);
	}
	
	public EnumSet<Zone> getZones() {
		return zones.clone();
	}
	
	public boolean isInsideTrackingBox() {		
		return !zones.contains(Zone.OUTSIDE) && !zones.contains(Zone.UNKOWN) ;
	}
	
	public boolean isInZone(Zone zone1) {
		return zones.contains(zone1);
	}
	
	public boolean isInZone(Zone zone1, Zone zone2) {
		return zones.contains(zone1) && zones.contains(zone2);
	}
	
	public boolean isInZone(Zone zone1, Zone zone2, Zone zone3) {
		return zones.contains(zone1) && zones.contains(zone2) && zones.contains(zone3);
	}
	
	public boolean isInClickZone() {
		return clickZone.size() != 0 &&  zones.containsAll(clickZone);
	}
	
	public boolean wasInsideTrackingBox() {
		return !prevZones.contains(Zone.OUTSIDE) && !prevZones.contains(Zone.UNKOWN);
	}
	
	public boolean wasInZone(Zone zone1) {
		return prevZones.contains(zone1);
	}
	
	public boolean wasInZone(Zone zone1, Zone zone2) {
		return prevZones.contains(zone1) && prevZones.contains(zone2);
	}
	
	public boolean wasInZone(Zone zone1, Zone zone2, Zone zone3) {
		return prevZones.contains(zone1) && prevZones.contains(zone2) && prevZones.contains(zone3);
	}
	
	public boolean wasInClickZone() {
		return clickZone.size() != 0 && prevZones.containsAll(clickZone);
	}
	
	public float getX() {
		return position.getX();
	}
	
	public float getY() {
		return position.getY();
	}
	
	public float getZ() {
		return position.getZ();
	}
	
	public float getAbsoluteX() {
		return absolutePosition.getX();
	}
	
	public float getAbsoluteY() {
		return absolutePosition.getY();
	}
	
	public float getAbsoluteZ() {
		return absolutePosition.getZ();
	}
	
	public AbstractPoint getSource() {
		return source;
	}
}
