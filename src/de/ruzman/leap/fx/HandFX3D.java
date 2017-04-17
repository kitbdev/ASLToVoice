package de.ruzman.leap.fx;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import com.leapmotion.leap.Bone.Type;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Hand;

/**
 * The class {@code HandFX3D} contains some {@link Sphere Spheres} and
 * {@link Cylinder Cylinders} which visualize a 3D hand in JavaFX. Call the
 * method {@link #update(Hand)} to update the position, direction, etc.
 */
public class HandFX3D extends Group {
	private Sphere palm;
	private Sphere metacarpal;
	private Sphere[] fingers = new Sphere[5];
	private Sphere[] distal = new Sphere[5];
	private Sphere[] proximal = new Sphere[5];
	private Sphere[] intermediate = new Sphere[5];

	private List<JointFX3D> joints;

	/**
	 * Initialize a 3D hand.
	 */
	public HandFX3D() {
		joints = new ArrayList<>();

		palm = createSphere();
		metacarpal = createSphere();

		for (int i = 0; i < fingers.length; i++) {
			fingers[i] = createSphere();
			distal[i] = createSphere();
			intermediate[i] = createSphere();
			proximal[i] = createSphere();

			connectSpheres(fingers[i], distal[i]);
			connectSpheres(distal[i], intermediate[i]);
			connectSpheres(intermediate[i], proximal[i]);
		}

		connectSpheres(proximal[1], proximal[2]);
		connectSpheres(proximal[2], proximal[3]);
		connectSpheres(proximal[3], proximal[4]);
		connectSpheres(proximal[0], proximal[1]);
		connectSpheres(proximal[0], metacarpal);
		connectSpheres(metacarpal, proximal[4]);
		
		getChildren().addAll(palm, metacarpal);
		getChildren().addAll(fingers);
		getChildren().addAll(distal);
		getChildren().addAll(proximal);
		getChildren().addAll(intermediate);		
	}

	private Sphere createSphere() {
		Sphere sphere = new Sphere(5);

		PhongMaterial material = new PhongMaterial();
		material.setSpecularColor(Color.CADETBLUE);
		//material.setDiffuseColor(Color.DARKRED);
                material.setDiffuseColor(Color.AQUAMARINE);
		sphere.setMaterial(material);

		return sphere;
	}

	private void connectSpheres(Sphere fromSphere, Sphere toSphere) {
		JointFX3D jointFX3D = new JointFX3D(fromSphere, toSphere);
		joints.add(jointFX3D);
		getChildren().add(jointFX3D.getBone());
	}

	/**
	 * Update the position, direction, etc. of the 3D hand.
	 * 
	 * @param hand
	 *            Hand, to be displayed.
	 */
	public void update(Hand hand) {
		FXUtil.translate(palm, hand.palmPosition());

		Iterator<Finger> itFinger = hand.fingers().iterator();

		Finger finger = null;
		for (int i = 0; i < fingers.length; i++) {
			finger = itFinger.next();

			FXUtil.translate(fingers[i], finger.tipPosition());
			FXUtil.translate(distal[i], finger.bone(Type.TYPE_DISTAL).prevJoint());
			FXUtil.translate(intermediate[i], finger.bone(Type.TYPE_INTERMEDIATE).prevJoint());
			FXUtil.translate(proximal[i], finger.bone(Type.TYPE_PROXIMAL).prevJoint());
		}
		FXUtil.translate(metacarpal, finger.bone(Type.TYPE_METACARPAL)
				.prevJoint());

		for (JointFX3D joint : joints) {
			joint.update();
		}
	}

	private class JointFX3D {
		private Sphere fromSphere;
		private Sphere toSphere;
		private Cylinder bone;
		private Rotate rotation;

		public JointFX3D(Sphere fromSphere, Sphere toSphere) {
			this.fromSphere = fromSphere;
			this.toSphere = toSphere;
			this.rotation = new Rotate();
			this.bone = createBone(rotation);
		}

		private Cylinder createBone(Rotate joint) {
			PhongMaterial material = new PhongMaterial();
			material.setSpecularColor(Color.PALEGREEN);
			material.setDiffuseColor(Color.ANTIQUEWHITE);

			Cylinder cylinder = new Cylinder();
			cylinder.setRadius(3);
			cylinder.setMaterial(material);
			cylinder.getTransforms().add(joint);

			return cylinder;
		}

		public void update() {
			double dx = (float) (fromSphere.getTranslateX() - toSphere.getTranslateX());
			double dy = (float) (fromSphere.getTranslateY() - toSphere.getTranslateY());
			double dz = (float) (fromSphere.getTranslateZ() - toSphere.getTranslateZ());

			bone.setHeight(Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2)));
			bone.setTranslateX(fromSphere.getTranslateX());
			bone.setTranslateY(fromSphere.getTranslateY() - bone.getHeight() / 2);
			bone.setTranslateZ(fromSphere.getTranslateZ());

			rotation.setPivotY(bone.getHeight() / 2);
			rotation.setAxis(new Point3D(dz, 0, -dx));
			rotation.setAngle(180 - new Point3D(dx, -dy, dz).angle(Rotate.Y_AXIS));
		}

		public Cylinder getBone() {
			return bone;
		}
	}
}