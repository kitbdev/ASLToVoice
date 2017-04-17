package de.ruzman.fx;

import javafx.collections.ObservableList;
import javafx.scene.layout.Region;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

public class BezierePath extends Region {
	private Path path = new Path();
	private ObservableList<PathElement> pathElements = path.getElements();

	public BezierePath() {
		getChildren().add(path);
	}

	public void add(double x, double y) {
		if (pathElements.isEmpty()) {
			pathElements.add(new MoveTo(x, y));
		} else {
			pathElements.add(new LineTo(x, y));
		}
	}
}