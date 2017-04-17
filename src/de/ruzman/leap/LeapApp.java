package de.ruzman.leap;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;

import com.leapmotion.leap.Controller;

import de.ruzman.leap.event.LeapEventHandler;

/**
 * LeapApp contains the configuration of a Leap Motion project. This class
 * follows the singleton pattern and cannot be initialized twice. Create an
 * instance with {@link LeapAppBuilder}.
 */
public final class LeapApp {
	private static LeapApp instance;

	private AWTDispatcher awtDispatcher;
	private MotionRegistry motionRegistry;
	private TrackingBox trackingBox;
	private int minimumHandNumber = 1;
	private int maximumHandNumber = Integer.MAX_VALUE;
	private int displayWidth;
	private int displayHeight;
	private boolean usePolling;

	private Controller controller;

	public LeapApp(Controller controller) {
		this.controller = controller;
                this.controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
	}

	private void init(TrackingBox trackingBox,
			int minimumHandNumber,
			int maximumHandNumber,
			int displayWidth,
			int displayHeight,
			boolean usePolling,
			boolean activateAWTDispatcher,
			MotionRegistry motionRegistry) {

		this.trackingBox = trackingBox;
		this.displayWidth = displayWidth;
		this.displayHeight = displayHeight;
		setMinimumHandNumber(minimumHandNumber);
		setMaximumHandNumber(maximumHandNumber);
		this.usePolling = usePolling;

		// FIXME: Beim doppelten Aufruf ist das nicht mehr korrekt.
		if (!usePolling) {
			controller.addListener(LeapEventHandler.getInstance());
		}

		// FIXME: Beim doppelten Aufruf ist das nicht mehr korrekt.
		this.motionRegistry = motionRegistry;
		LeapEventHandler.addLeapListener(motionRegistry);

		if (activateAWTDispatcher) {
			getAWTMouseListener();
		}
	}

	public static void setTrackingBox(TrackingBox trackingBox) {
		instance.trackingBox = trackingBox;
	}

	public static TrackingBox getTrackingBox() {
		return instance.trackingBox;
	}

	public static void setMinimumHandNumber(int minimumHandNumber) {
		validateHandNumber();
		instance.minimumHandNumber = minimumHandNumber;
	}

	public static int getMinimumHandNumber() {
		return instance.minimumHandNumber;
	}

	public static void setMaximumHandNumber(int maximumHandNumber) {
		validateHandNumber();
		instance.maximumHandNumber = maximumHandNumber;
	}

	public static int getMaximumHandNumber() {
		return instance.maximumHandNumber;
	}

	private static void validateHandNumber() {
		if (instance.maximumHandNumber < instance.minimumHandNumber) {
			throw new IllegalArgumentException(
					"MaximumHandNumber must be >= minumumHandNumber");
		}

		if (instance.maximumHandNumber < 1) {
			throw new IllegalArgumentException("MinimumHandNumber must be >= 1");
		}

		if (instance.minimumHandNumber < 1) {
			throw new IllegalArgumentException("MinimumHandNumber must be >= 1");
		}
	}

	public static void setDisplayWidth(int displayWidth) {
		instance.displayWidth = displayWidth;
	}

	public static int getDisplayWidth() {
		return instance.displayWidth;
	}

	public static void setDisplayHeight(int displayHeight) {
		instance.displayHeight = displayHeight;
	}

	public static int getDisplayHeight() {
		return instance.displayHeight;
	}

	public static void setMotionRegistry(MotionRegistry motionRegistry) {
		if (motionRegistry != null) {
			LeapEventHandler.removeLeapListener(instance.motionRegistry);
			instance.motionRegistry = motionRegistry;
			LeapEventHandler.addLeapListener(motionRegistry);
		}
	}

	public static MotionRegistry getMotionRegistry() {
		return instance.motionRegistry;
	}

	public static Controller getController() {
             instance.controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
	     return instance.controller;
	}

	public static void update() {
		if (instance.usePolling) {
			LeapEventHandler.updateFrame();
		}
	}

	public static WindowAdapter getAWTMouseListener() {
		if (instance.awtDispatcher == null) {
			instance.awtDispatcher = new AWTDispatcher();
			instance.motionRegistry.addListener(instance.awtDispatcher);
		}
		return instance.awtDispatcher;
	}

	public static void destroy() {
		LeapEventHandler.removeAllLeapListener();
                instance.controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
		instance.controller.delete();
		try {
			instance.finalize();
		} catch (Throwable t) {
			// Do nothing.
		} finally {
			instance = null;
			System.exit(0);
		}
	}

	/**
	 * LeapAppBuilder initialize a LeapApp instance. Default configuration:
	 * <ul>
	 * <li>Load native libraries from: "native"
	 * <li>TrackingBox: use predefined user settings (InteractionBox)
	 * <li>Minimum hand number: 1
	 * <li>Maximum hand number: unlimited
	 * <li>Display width: width of the default screen
	 * <li>Display height: height of the default screen
	 * <li>Polling: activated
	 * <li>AWTDispatcher: inactive
	 * <li>motionRegistry: default
	 * </ul>
	 * <p>
	 * Short version: <code>new LeapAppBuilder().initLeapApp();</code>
	 * <p>
	 * Same as:
	 * 
	 * <pre>
	 * <code>new LeapAppBuilder(true, "native")
	 * 	.trackingBox(new TrackingBox())
	 * 	.minimumHandNumber(1)
	 * 	.maximumHandNumber(Integer.MAX_VALUE)
	 * 	.displayWidth(dispMode.getWidth())
	 * 	.displayHeight(dispMode.getHeight())
	 * 	.usePolling(true)
	 * 	// .activeAWTDispatcher()
	 * 	.motionRegistry(new MotionRegistry());
	 * 	.initLeapApp();
	 * </code>
	 * </pre>
	 */
	public static class LeapAppBuilder {
		private TrackingBox trackingBox;
		private int minimumHandNumber = 1;
		private int maximumHandNumber = Integer.MAX_VALUE;
		private int displayWidth = -1;
		private int displayHeight = -1;
		private boolean usePolling = true;
		private boolean activeAWTDispatcher = false;
		private MotionRegistry motionRegistry;

		/**
		 * See: {@link LeapAppBuilder#LeapAppBuilder(boolean)
		 * LeapAppBuilder(true)}.
		 */
		public LeapAppBuilder() {
                        //this.controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);                        
			this(true);
		}

		/**
		 * See: {@link LeapAppBuilder#LeapAppBuilder(boolean, String)
		 * LeapAppBuilder(param, "native")}.
		 * 
		 * @param shouldLoadNativeLibraries
		 *            Whether the LeapAppBuilder should load native libraries.
		 */
		public LeapAppBuilder(boolean shouldLoadNativeLibraries) {
			this(shouldLoadNativeLibraries, "native");
		}

		/**
		 * Loads native libraries if
		 * <code>shouldLoadNativeLibraries} == true</code> and initializes this
		 * Leap Motion framework.
		 * 
		 * @param shouldLoadNativeLibraries
		 *            Whether the LeapAppBuilder should load native libraries.
		 * @param path
		 *            Path of the native libraries.
		 */
		public LeapAppBuilder(boolean shouldLoadNativeLibraries, String path) {
			if (shouldLoadNativeLibraries) {
				NativeLibrary.loadSystem(path);
			}

			if(instance == null) {
				instance = new LeapApp(new Controller());
                                instance.controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES);
			}
			trackingBox = new TrackingBox();
			motionRegistry = new MotionRegistry();
		}

		/**
		 * Initializes this Leap Motion framework with the given configuration.
		 * 
		 * @return LeapApp
		 */
		public LeapApp initLeapApp() {
			initDisplaySize();
			instance.init(trackingBox,
					minimumHandNumber,
					maximumHandNumber,
					displayWidth,
					displayHeight,
					usePolling,
					activeAWTDispatcher,
					motionRegistry);

			return instance;
		}

		private void initDisplaySize() {
			GraphicsDevice device = GraphicsEnvironment
					.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			DisplayMode dispMode = device.getDisplayMode();

			if (displayWidth < 0) {
				displayWidth = dispMode.getWidth();
			}
			if (displayHeight < 0) {
				displayHeight = dispMode.getHeight();
			}
		}

		/**
		 * Use a specified {@link TrackingBox}, within which the hands are
		 * tracked. Default value: <code>new TrackingBox()</code>.
		 * 
		 * @param trackingBox
		 *            TrackingBox
		 * @return LeapAppBuilder
		 */
		public LeapAppBuilder trackingBox(TrackingBox trackingBox) {
			this.trackingBox = trackingBox;
			return this;
		}

		/**
		 * Set the minimum number of hands. Default value: <code>1</code>.
		 * 
		 * @param minimumHandNumber
		 *            Minimum number of hands
		 * @return LeapAppBuilder
		 */
		public LeapAppBuilder minimumHandNumber(int minimumHandNumber) {
			this.minimumHandNumber = minimumHandNumber;
			return this;
		}

		/**
		 * Set the maximum number of hands. Default value:
		 * <code>Integer.MAX_VALUE</code>.
		 * 
		 * @param maximumHandNumber
		 *            Maximum number of hands
		 * @return LeapAppBuilder
		 */
		public LeapAppBuilder maximumHandNumber(int maximumHandNumber) {
			this.maximumHandNumber = maximumHandNumber;
			return this;
		}

		/**
		 * Set the width of the application (frame). Default value:
		 * <code>dispMode.getWidth()</code>
		 * 
		 * @param displayWidth
		 *            width of the application (frame)
		 * @return LeapAppBuilder
		 */
		public LeapAppBuilder displayWidth(int displayWidth) {
			this.displayWidth = displayWidth;
			return this;
		}

		/**
		 * Set the height of the application (frame). Default value:
		 * <code>dispMode.getHeight()</code>
		 * 
		 * @param displayWidth
		 *            height of the application (frame)
		 * @return LeapAppBuilder
		 */
		public LeapAppBuilder displayHeight(int displayHeight) {
			this.displayHeight = displayHeight;
			return this;
		}

		/**
		 * Whether the framework-logic should get the data by polling. Default
		 * value: <code>true</code>
		 * 
		 * @param usePolling
		 *            <code>true</code> if the framework-logic should get the
		 *            data by polling.
		 * @return LeapAppBuilder
		 */
		public LeapAppBuilder usePolling(boolean usePolling) {
			this.usePolling = usePolling;
			return this;
		}

		/**
		 * Activate the AWT Dispatcher.
		 * 
		 * @return LeapAppBuilder
		 */
		public LeapAppBuilder activeAWTDispatcher() {
			this.activeAWTDispatcher = true;
			return this;
		}

		/**
		 * Set the {@link MotionRegistry}.
		 * 
		 * @param motionRegistry
		 *            MotionRegistry
		 * @return LeapAppBuilder
		 */
		public LeapAppBuilder motionRegistry(MotionRegistry motionRegistry) {
			this.motionRegistry = motionRegistry;
			return this;
		}
	}
}