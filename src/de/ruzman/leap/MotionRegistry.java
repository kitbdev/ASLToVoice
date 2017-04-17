package de.ruzman.leap;

import com.leapmotion.leap.Controller;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Hand;

import de.ruzman.leap.event.IListener;
import de.ruzman.leap.event.LeapEvent;
import de.ruzman.leap.event.LeapListener;
import de.ruzman.leap.event.PointDraggListener;
import de.ruzman.leap.event.PointEvent.Zone;
import de.ruzman.leap.event.PointZoneListener;
import de.ruzman.leap.event.PointMotionListener;

public class MotionRegistry implements LeapListener {	
	protected ConcurrentMap<Integer, ExtendedHand> hands;
	protected List<IListener> listeners;

	public MotionRegistry() {
		hands = new ConcurrentHashMap<>(10);
		listeners = new CopyOnWriteArrayList<>();
                LeapApp.getController().setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
	}
	
	public void update(Frame frame) {
		
		if(frame.hands().count() >= LeapApp.getMinimumHandNumber()) {
			for(ExtendedHand extendedHand: hands.values()) {
				if(frame.hand(extendedHand.id()).id() == -1) {
					extendedHand.destroy();
					hands.remove(extendedHand.id());
				}
			}
			
			ExtendedHand extendedHand;
			int handCount = 0;
	
			for(Hand hand: frame.hands()) {
				extendedHand = hands.get(hand.id());

				if (extendedHand == null
						&& hands.size() < LeapApp.getMaximumHandNumber()) {
					extendedHand = new ExtendedHand(hand.id(),
							LeapApp.getTrackingBox());
					extendedHand.setClickZone(EnumSet.of(Zone.BACK));

					for (IListener listener : listeners) {
						extendedHand.addListener(listener);
					}

					hands.put(hand.id(), extendedHand);
				}
	
				
				if(extendedHand != null) {
					if(handCount++ < LeapApp.getMaximumHandNumber()) {
						extendedHand.update(frame, frame.hand(extendedHand.id()).palmPosition(),
							frame.hand(extendedHand.id()).stabilizedPalmPosition());
					}
				}
			}
		} else {
			for(ExtendedHand extendedHand: hands.values()) {
				extendedHand.destroy();
				hands.remove(extendedHand.id());
			}
		}
		
		for(Hand hand: frame.hands()) {
			ExtendedHand extendedHand = hands.get(hand.id());
			if(extendedHand != null) {
				extendedHand.updateEvents();
			}
		}

	}
	
	public void clear() {
		hands.clear();
	}
	
	public void addListener(IListener listener) {
		if(listener instanceof PointZoneListener
			|| listener instanceof PointMotionListener
			|| listener instanceof PointDraggListener) {

			listeners.add(listener);
			
			for(AbstractPoint abstractPoint: hands.values()) {
				abstractPoint.addListener(listener);
			}
		}
	}
	
	public void removePointListener(IListener listener) {
		if(listener instanceof PointZoneListener
				|| listener instanceof PointMotionListener
				|| listener instanceof PointDraggListener) {
			
			listeners.remove(listener);
			
			for(AbstractPoint abstractPoint: hands.values()) {
				abstractPoint.removeListener(listener);
			}
		}
	}
	
	@Override
	public void update(LeapEvent event) {
		update(event.getFrame());
	}

	@Override
	public void statusChanged(LeapEvent event) {
		clear();
	}

	public synchronized void removeAllListener() {		
		listeners.clear();
		
		hands.clear();
	}
	
	public int handCount() {
		return hands.size();
	}
}
