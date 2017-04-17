package de.ruzman.leap.fx;

import javafx.scene.Node;

import com.leapmotion.leap.Vector;

public final class FXUtil {
	
	/**
	 * Translate a {@link Node node} by the given {@link com.leapmotion.leap.Vector vec3}.
	 * Y-Axis and Z-Axis will be inverted.
	 * <p>
	 * For Example:<br>
	 * vec3[15, 40, 13] -&gt; node[15, -40, -13]
	 * 
	 * @param node
	 * @param vec3
	 */
	public static void translate(Node node, Vector vec3) {		
		node.setTranslateX(vec3.getX());
		node.setTranslateY(-vec3.getY());
		node.setTranslateZ(-vec3.getZ());
	}
}
