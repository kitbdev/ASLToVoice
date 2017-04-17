package de.ruzman.leap;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Vector;

import de.ruzman.leap.event.IListener;
import de.ruzman.leap.event.PointDraggListener;
import de.ruzman.leap.event.PointEvent;
import de.ruzman.leap.event.PointEvent.Zone;
import de.ruzman.leap.event.PointMotionListener;
import de.ruzman.leap.event.PointZoneListener;

public abstract class AbstractPoint {
	private int id;
	protected boolean isActive;

	private Vector diameter;

	private float maxXPos;
	private float minYPos;

	protected TrackingBox trackingBox;
	private List<IListener> listeners;
	
	/** PointMotion Listener */
	private EnumSet<Zone> clickZone;
	private Vector point;
	private Vector position;

	/** Point Listener */
	private PointEvent pointEvent;
	private Vector zone;
	private EnumSet<Zone> zones;
	private EnumSet<Zone> prevZones;
	private EnumSet<Zone> bufferedZone;

	public AbstractPoint(int id) {
		this(id, new TrackingBox());
	}

	public AbstractPoint(int id, TrackingBox trackingBox) {
		this.id = id;
		this.trackingBox = trackingBox;
		isActive = true;

		listeners = new CopyOnWriteArrayList<>(); 
		
		clickZone = EnumSet.noneOf(Zone.class);
		position = new Vector();

		zone = new Vector();
		zones = EnumSet.of(Zone.UNKOWN);
		prevZones = EnumSet.of(Zone.UNKOWN);
	}

	public int id() {
		return id;
	}

	protected void update(Frame frame, Vector point, Vector stabilizedPoint) {
		this.point = point;
		
		trackingBox.calcScreenPosition(stabilizedPoint, position);
		trackingBox.calcZone(point, zone);

		if (diameter != null) {
			if (position.getX() < 0) {
				position.setX(0);
			} else if (position.getX() > maxXPos) {
				position.setX(maxXPos);
			}

			if (position.getY() < 0) {
				position.setY(0);
			} else if (position.getY() > minYPos) {
				position.setY(minYPos);
			}
		}
	}

	public void destroy() {
		isActive = false;
		updateEvents();
	}
	
	public void addListener(IListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(IListener listener) {
		listeners.remove(listener);
	}

	protected void updateEvents() {
		pointEvent = new PointEvent(this, zones, prevZones,
				clickZone);
		fireUpdate();
	}
	
	protected void fireUpdate() {
		boolean isZoneUpdated = updateZone();
		boolean isDraggDetected = zones.containsAll(clickZone);
		
		for (IListener listener : listeners) {
			if(listener instanceof PointMotionListener) {
				PointMotionListener pointMotionListener = (PointMotionListener) listener;
				
				if(pointEvent.enteredViewPort()) {
					pointMotionListener.enteredViewoport(pointEvent);
					pointMotionListener.moved(pointEvent);
				} else if(pointEvent.leftViewPort()) {
					pointMotionListener.moved(pointEvent);
					pointMotionListener.leftViewport(pointEvent);
				} else {
					pointMotionListener.moved(pointEvent);
				}
			}
			if(isZoneUpdated && listener instanceof PointZoneListener) {
				PointZoneListener pointZoneListener = (PointZoneListener) listener;
				pointZoneListener.zoneChanged(pointEvent);
			}
			if(isDraggDetected && listener instanceof PointDraggListener) {
				PointDraggListener pointDraggListener = (PointDraggListener) listener;
				pointDraggListener.pointDragged(pointEvent);
			}
		}
	}

	private boolean updateZone() {
		if (!isActive) {
			bufferedZone = EnumSet.of(Zone.UNKOWN);
		} else {
			bufferedZone = EnumSet.of(
					getZone(zone.getX(), Zone.RIGHT, Zone.LEFT),
					getZone(zone.getY(), Zone.DOWN, Zone.UP),
					getZone(zone.getZ(), Zone.BACK, Zone.FRONT));
		}

		prevZones.clear();
		prevZones.addAll(zones);
		
		if (!bufferedZone.equals(zones)) {
			zones.clear();
			zones.addAll(bufferedZone);

			return true;
		} else {
			return false;
		}
	}

	private Zone getZone(float value, Zone zone0, Zone zone1) {
		switch ((int) (value + 1)) {
		case 0:
			return zone0;
		case 1:
			return zone1;
		default:
			return Zone.OUTSIDE;
		}
	}

	public final void setDiameter(Vector diameter) {
		if (diameter != null) {
			maxXPos = LeapApp.getDisplayWidth() - diameter.getX();
			minYPos = LeapApp.getDisplayHeight() - diameter.getY();
		}

		this.diameter = diameter;
	}
	
	public void setTrackingBox(TrackingBox trackingBox) {
		this.trackingBox = trackingBox;
	}

	public final void setClickZone(EnumSet<Zone> zones) {
		clickZone.clear();
		clickZone.addAll(zones);
	}
	
	public Vector getPosition() {
		return new Vector(position);
	}
	
	public Vector getPoint() {
		return new Vector(point);
	}
}
