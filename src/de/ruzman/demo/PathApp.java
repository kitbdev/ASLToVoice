package de.ruzman.demo;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.leapmotion.leap.Vector;

import de.ruzman.fx.BezierePath;
import de.ruzman.leap.LeapApp;
import de.ruzman.leap.LeapApp.LeapAppBuilder;
import de.ruzman.leap.event.PointEvent;
import de.ruzman.leap.event.PointMotionListener;

public class PathApp extends Application implements PointMotionListener {
	private Group root;

	private Map<Integer, BezierePath> paths = new HashMap<>();
	private Vector position = new Vector();

	public static void main(String[] args) {
		new LeapAppBuilder()
			.displayWidth(764)
			.displayHeight(221)
			.maximumHandNumber(1)
			.initLeapApp();
		
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		root = new Group();

		Scene scene = new Scene(root, LeapApp.getDisplayWidth(), 
				LeapApp.getDisplayHeight());

		primaryStage.setScene(scene);
		primaryStage.show();

		synchronizeWithLeapMotion();
		LeapApp.getMotionRegistry().addListener(this);
	}

	private void synchronizeWithLeapMotion() {
		Timeline timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.seconds(1.0 / 60.0), ea -> LeapApp
						.update()));
		timeline.play();
	}

	@Override
	public void enteredViewoport(PointEvent event) {
		BezierePath path = new BezierePath();
		paths.put(event.getSource().id(), path);
		root.getChildren().add(path);
	}

	@Override
	public void moved(PointEvent event) {
		BezierePath path = paths.get(event.getSource().id());
		path.add(position.getX(), position.getY());
	}

	@Override
	public void leftViewport(PointEvent event) {
		int handId = event.getSource().id();
		paths.remove(handId);
		root.getChildren().remove(paths.get(handId));
	}
}
