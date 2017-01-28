package org.usfirst.frc.team3090.robot;

import org.opencv.core.Mat;

import edu.wpi.cscore.AxisCamera;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	final Joystick stick = new Joystick(0);
	
	RobotDrive drive = new RobotDrive(0, 1, 2, 3); //Robot Drive (LF, LR, RF, RR)
	
	// Threads
	Thread vision_thread; //camera
	Thread grip;
	
	NavX navx;
	
	NetworkTable table;
	/**
	 * This function is run when the robot is first started up and should be
	 * used for any initialization code.
	 */
	
	@Override
	public void robotInit() {
		chooser.addDefault("Default Auto", defaultAuto);
		chooser.addObject("My Auto", customAuto);
		SmartDashboard.putData("Auto choices", chooser);
		
		
		/*Vision & Camera*/
		
		vision_thread = new Thread(() -> {
			// Get the Axis camera from CameraServer
			AxisCamera camera = CameraServer.getInstance().addAxisCamera("axis-camera.local");
			// Set the resolution
			camera.setResolution(640, 480);

			// Get a CvSink. This will capture Mats from the camera
			CvSink cvSink = CameraServer.getInstance().getVideo();
			// Setup a CvSource. This will send images back to the Dashboard
			CvSource outputStream = CameraServer.getInstance().putVideo("Camera", 640, 480);

			// Mats are very memory expensive. Lets reuse this Mat.
			Mat mat = new Mat();


			// This cannot be 'true'. The program will never exit if it is. This
			// lets the robot stop this thread when restarting robot code or
			// deploying.
			while (!Thread.interrupted()) {
				// Tell the CvSink to grab a frame from the camera and put it
				// in the source mat.  If there is an error notify the output.
				if (cvSink.grabFrame(mat) == 0) {
					// Send the output the error.
					outputStream.notifyError(cvSink.getError());
					// skip the rest of the current iteration
					continue;
				}
				// Put a rectangle on the image
				/*Imgproc.rectangle(mat, new Point(100, 100), new Point(400, 400),
						new Scalar(255, 255, 255), 5);*/
				// Give the output stream a new image to display
				outputStream.putFrame(mat);
			}
		});
		vision_thread.setDaemon(true);
		//vision_thread.start();
		
		table = NetworkTable.getTable("GRIP/myContoursReport");
		
		grip = new Thread(() -> {
			double[] defaultValue = new double[0];
			while (!Thread.interrupted()) {
				double[] areas = table.getNumberArray("area", defaultValue);
				System.out.println("Areas: ");
				for (double area : areas) {
					System.out.print(area + " ");
				}
				System.out.println();
				Timer.delay(1);
			}
		});
		grip.start();
		
		/*navX micro Sensor*/
		
		navx = new NavX();
		navx.init();
	}

	/**
	 * This autonomous (along with the chooser code above) shows how to select
	 * between different autonomous modes using the dashboard. The sendable
	 * chooser code works with the Java SmartDashboard. If you prefer the
	 * LabVIEW Dashboard, remove all of the chooser code and uncomment the
	 * getString line to get the auto name from the text box below the Gyro
	 *
	 * You can add additional auto modes by adding additional comparisons to the
	 * switch structure below with additional strings. If using the
	 * SendableChooser make sure to add them to the chooser code above as well.
	 */
	@Override
	public void autonomousInit() {
		autoSelected = chooser.getSelected();
		// autoSelectedpublic static final Talon front_left_motor = new Talon(2); = SmartDashboard.getString("Auto Selector",
		// defaultAuto);
		System.out.println("Auto selected: " + autoSelected);
	}

	/**
	 * This function is called periodically during autonomous
	 */
	@Override
	public void autonomousPeriodic() {
		switch (autoSelected) {
		case customAuto:
			// Put custom auto code here
			break;
		case defaultAuto:
		default:
			// Put default auto code here
			break;
		}
	}
	
	@Override
	public void teleopInit() {
		navx.rotateToAngle(stick, 90.0f, drive);
	}

	
	@Override
	public void disabledInit() {

	}

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		drive.tankDrive(stick.getRawAxis(1), stick.getRawAxis(5));
		
		if (stick.getRawButton(3)) {
			// y
			RobotParts.lift_motor_1.set(1);
			RobotParts.lift_motor_2.set(-1);
		} else if (stick.getRawButton(1)) {
			// a
			RobotParts.lift_motor_1.set(-1);
			RobotParts.lift_motor_2.set(1);
		} else {
			RobotParts.lift_motor_1.set(0);
			RobotParts.lift_motor_2.set(0);
		}
		
        Timer.delay(0.020);		/* wait for one motor update time period (50Hz)     */
        
		navx.showData(stick);
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		/* More navX Sensor Stuff - Data on dashboard*/
		
		//while (isOperatorControl() && isEnabled()) {
	          
	          Timer.delay(0.020);		/* wait for one motor update time period (50Hz)     */
	          
	          navx.showTestData(stick);
	     //}
	}
}

