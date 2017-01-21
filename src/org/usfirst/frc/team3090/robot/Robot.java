package org.usfirst.frc.team3090.robot;

import org.opencv.core.Mat;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.cscore.AxisCamera;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.CvSource;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
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
	
	AHRS ahrs; //navX micro
	
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
			AxisCamera camera = CameraServer.getInstance().addAxisCamera("10.30.90.89");
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
				
			}
		});
		vision_thread.setDaemon(true);
		vision_thread.start();
		
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
		
		try {
	          /* Communicate w/navX-MXP via the MXP SPI Bus.                                     */
	          /* Alternatively:  I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB     */
	          /* See http://navx-mxp.kauailabs.com/guidance/selecting-an-interface/ for details. */
	          ahrs = new AHRS(I2C.Port.kMXP); 
	      } catch (RuntimeException ex ) {
	          DriverStation.reportError("Error instantiating navX-MXP:  " + ex.getMessage(), true);
	      }
		
		
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

	/**
	 * This function is called periodically during operator control
	 */
	@Override
	public void teleopPeriodic() {
		
		
		drive.tankDrive(stick.getRawAxis(1), stick.getRawAxis(5));
		Timer.delay(0.005);
		
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
		

        //Timer.delay(0.020);		/* wait for one motor update time period (50Hz)     */
        
        boolean zero_yaw_pressed = stick.getTrigger();
        if ( zero_yaw_pressed ) {
            ahrs.zeroYaw();
        }

        /* Display 6-axis Processed Angle Data                                      */
        SmartDashboard.putBoolean(  "IMU_Connected",        ahrs.isConnected());
        SmartDashboard.putBoolean(  "IMU_IsCalibrating",    ahrs.isCalibrating());
        SmartDashboard.putNumber(   "IMU_Yaw",              Math.round(100.0 * ahrs.getYaw()) / 100.0);
        SmartDashboard.putNumber(   "IMU_Pitch",            Math.round(100.0 * ahrs.getPitch()) / 100.0);
        SmartDashboard.putNumber(   "IMU_Roll",             Math.round(100.0 * ahrs.getRoll()) / 100.0);
        
        /* Display tilt-corrected, Magnetometer-based heading (requires             */
        /* magnetometer calibration to be useful)                                   */
        
        SmartDashboard.putNumber(   "IMU_CompassHeading",   Math.round(100.0 * ahrs.getCompassHeading()) / 100.0);
        
        /* Display 9-axis Heading (requires magnetometer calibration to be useful)  */
        SmartDashboard.putNumber(   "IMU_FusedHeading",     Math.round(100.0 * ahrs.getFusedHeading()) / 100.0);

        /* These functions are compatible w/the WPI Gyro Class, providing a simple  */
        /* path for upgrading from the Kit-of-Parts gyro to the navx-MXP            */
        
        SmartDashboard.putNumber(   "IMU_TotalYaw",         Math.round(100.0 * ahrs.getAngle()) / 100.0);
        SmartDashboard.putNumber(   "IMU_YawRateDPS",       Math.round(100.0 * ahrs.getRate()) / 100.0);

        /* Display Processed Acceleration Data (Linear Acceleration, Motion Detect) */
        
        SmartDashboard.putNumber(   "IMU_Accel_X",          Math.round(100.0 * ahrs.getWorldLinearAccelX()) / 100.0);
        SmartDashboard.putNumber(   "IMU_Accel_Y",          Math.round(100.0 * ahrs.getWorldLinearAccelY()) / 100.0);
        SmartDashboard.putBoolean(  "IMU_IsMoving",         ahrs.isMoving());
        SmartDashboard.putBoolean(  "IMU_IsRotating",       ahrs.isRotating());

        /* Display estimates of velocity/displacement.  Note that these values are  */
        /* not expected to be accurate enough for estimating robot position on a    */
        /* FIRST FRC Robotics Field, due to accelerometer noise and the compounding */
        /* of these errors due to single (velocity) integration and especially      */
        /* double (displacement) integration.                                       */
        
        SmartDashboard.putNumber(   "Velocity_X",           Math.round(100.0 * ahrs.getVelocityX()) / 100.0);
        SmartDashboard.putNumber(   "Velocity_Y",           Math.round(100.0 * ahrs.getVelocityY()) / 100.0);
        SmartDashboard.putNumber(   "Displacement_X",       Math.round(100.0 * ahrs.getDisplacementX()) / 100.0);
        SmartDashboard.putNumber(   "Displacement_Y",       Math.round(100.0 * ahrs.getDisplacementY()) / 100.0);
        
        /* Display Raw Gyro/Accelerometer/Magnetometer Values                       */
        /* NOTE:  These values are not normally necessary, but are made available   */
        /* for advanced users.  Before using this data, please consider whether     */
        /* the processed data (see above) will suit your needs.                     */
        
        SmartDashboard.putNumber(   "RawGyro_X",            Math.round(100.0 * ahrs.getRawGyroX()) / 100.0);
        SmartDashboard.putNumber(   "RawGyro_Y",            Math.round(100.0 * ahrs.getRawGyroY()) / 100.0);
        SmartDashboard.putNumber(   "RawGyro_Z",            Math.round(100.0 * ahrs.getRawGyroZ()) / 100.0);
        SmartDashboard.putNumber(   "RawAccel_X",           Math.round(100.0 * ahrs.getRawAccelX()) / 100.0);
        SmartDashboard.putNumber(   "RawAccel_Y",           Math.round(100.0 * ahrs.getRawAccelY()) / 100.0);
        SmartDashboard.putNumber(   "RawAccel_Z",           Math.round(100.0 * ahrs.getRawAccelZ()) / 100.0);
        SmartDashboard.putNumber(   "RawMag_X",             Math.round(100.0 * ahrs.getRawMagX()) / 100.0);
        SmartDashboard.putNumber(   "RawMag_Y",             Math.round(100.0 * ahrs.getRawMagY()) / 100.0);
        SmartDashboard.putNumber(   "RawMag_Z",             Math.round(100.0 * ahrs.getRawMagZ()) / 100.0);
        SmartDashboard.putNumber(   "IMU_Temp_C",           Math.round(100.0 * ahrs.getTempC()) / 100.0);
        
        /* Omnimount Yaw Axis Information                                           */
        /* For more info, see http://navx-mxp.kauailabs.com/installation/omnimount  */
        AHRS.BoardYawAxis yaw_axis = ahrs.getBoardYawAxis();
        SmartDashboard.putString(   "YawAxisDirection",     yaw_axis.up ? "Up" : "Down" );
        SmartDashboard.putNumber(   "YawAxis",              yaw_axis.board_axis.getValue() );
        
        /* Sensor Board Information                                                 */
        SmartDashboard.putString(   "FirmwareVersion",      ahrs.getFirmwareVersion());
        
        /* Quaternion Data                                                          */
        /* Quaternions are fascinating, and are the most compact representation of  */
        /* orientation data.  All of the Yaw, Pitch and Roll Values can be derived  */
        /* from the Quaternions.  If interested in motion processing, knowledge of  */
        /* Quaternions is highly recommended.                                       */
        SmartDashboard.putNumber(   "QuaternionW",          Math.round(100.0 * ahrs.getQuaternionW()) / 100.0);
        SmartDashboard.putNumber(   "QuaternionX",          Math.round(100.0 * ahrs.getQuaternionX()) / 100.0);
        SmartDashboard.putNumber(   "QuaternionY",          Math.round(100.0 * ahrs.getQuaternionY()) / 100.0);
        SmartDashboard.putNumber(   "QuaternionZ",          Math.round(100.0 * ahrs.getQuaternionZ()) / 100.0);
        
        /* Connectivity Debugging Support                                           */
        SmartDashboard.putNumber(   "IMU_Byte_Count",       Math.round(100.0 * ahrs.getByteCount()) / 100.0);
        SmartDashboard.putNumber(   "IMU_Update_Count",     Math.round(100.0 * ahrs.getUpdateCount()) / 100.0);
		
		
		
	}

	/**
	 * This function is called periodically during test mode
	 */
	@Override
	public void testPeriodic() {
		
		/* More navX Sensor Stuff - Data on dashboard*/
		
		while (isOperatorControl() && isEnabled()) {
	          
	          Timer.delay(0.020);		/* wait for one motor update time period (50Hz)     */
	          
	          boolean zero_yaw_pressed = stick.getTrigger();
	          if ( zero_yaw_pressed ) {
	              ahrs.zeroYaw();
	          }

	          /* Display 6-axis Processed Angle Data                                      */
	          SmartDashboard.putBoolean(  "IMU_Connected",        ahrs.isConnected());
	          SmartDashboard.putBoolean(  "IMU_IsCalibrating",    ahrs.isCalibrating());
	          SmartDashboard.putNumber(   "IMU_Yaw",              Math.round(100.0 * ahrs.getYaw()) / 100.0);
	          SmartDashboard.putNumber(   "IMU_Pitch",            Math.round(100.0 * ahrs.getPitch()) / 100.0);
	          SmartDashboard.putNumber(   "IMU_Roll",             Math.round(100.0 * ahrs.getRoll()) / 100.0);
	          
	          /* Display tilt-corrected, Magnetometer-based heading (requires             */
	          /* magnetometer calibration to be useful)                                   */
	          
	          SmartDashboard.putNumber(   "IMU_CompassHeading",   Math.round(100.0 * ahrs.getCompassHeading()) / 100.0);
	          
	          /* Display 9-axis Heading (requires magnetometer calibration to be useful)  */
	          SmartDashboard.putNumber(   "IMU_FusedHeading",     Math.round(100.0 * ahrs.getFusedHeading()) / 100.0);

	          /* These functions are compatible w/the WPI Gyro Class, providing a simple  */
	          /* path for upgrading from the Kit-of-Parts gyro to the navx-MXP            */
	          
	          SmartDashboard.putNumber(   "IMU_TotalYaw",         Math.round(100.0 * ahrs.getAngle()) / 100.0);
	          SmartDashboard.putNumber(   "IMU_YawRateDPS",       Math.round(100.0 * ahrs.getRate()) / 100.0);

	          /* Display Processed Acceleration Data (Linear Acceleration, Motion Detect) */
	          
	          SmartDashboard.putNumber(   "IMU_Accel_X",          Math.round(100.0 * ahrs.getWorldLinearAccelX()) / 100.0);
	          SmartDashboard.putNumber(   "IMU_Accel_Y",          Math.round(100.0 * ahrs.getWorldLinearAccelY()) / 100.0);
	          SmartDashboard.putBoolean(  "IMU_IsMoving",         ahrs.isMoving());
	          SmartDashboard.putBoolean(  "IMU_IsRotating",       ahrs.isRotating());

	          /* Display estimates of velocity/displacement.  Note that these values are  */
	          /* not expected to be accurate enough for estimating robot position on a    */
	          /* FIRST FRC Robotics Field, due to accelerometer noise and the compounding */
	          /* of these errors due to single (velocity) integration and especially      */
	          /* double (displacement) integration.                                       */
	          
	          SmartDashboard.putNumber(   "Velocity_X",           Math.round(100.0 * ahrs.getVelocityX()) / 100.0);
	          SmartDashboard.putNumber(   "Velocity_Y",           Math.round(100.0 * ahrs.getVelocityY()) / 100.0);
	          SmartDashboard.putNumber(   "Displacement_X",       Math.round(100.0 * ahrs.getDisplacementX()) / 100.0);
	          SmartDashboard.putNumber(   "Displacement_Y",       Math.round(100.0 * ahrs.getDisplacementY()) / 100.0);
	          
	          /* Display Raw Gyro/Accelerometer/Magnetometer Values                       */
	          /* NOTE:  These values are not normally necessary, but are made available   */
	          /* for advanced users.  Before using this data, please consider whether     */
	          /* the processed data (see above) will suit your needs.                     */
	          
	          SmartDashboard.putNumber(   "RawGyro_X",            Math.round(100.0 * ahrs.getRawGyroX()) / 100.0);
	          SmartDashboard.putNumber(   "RawGyro_Y",            Math.round(100.0 * ahrs.getRawGyroY()) / 100.0);
	          SmartDashboard.putNumber(   "RawGyro_Z",            Math.round(100.0 * ahrs.getRawGyroZ()) / 100.0);
	          SmartDashboard.putNumber(   "RawAccel_X",           Math.round(100.0 * ahrs.getRawAccelX()) / 100.0);
	          SmartDashboard.putNumber(   "RawAccel_Y",           Math.round(100.0 * ahrs.getRawAccelY()) / 100.0);
	          SmartDashboard.putNumber(   "RawAccel_Z",           Math.round(100.0 * ahrs.getRawAccelZ()) / 100.0);
	          SmartDashboard.putNumber(   "RawMag_X",             Math.round(100.0 * ahrs.getRawMagX()) / 100.0);
	          SmartDashboard.putNumber(   "RawMag_Y",             Math.round(100.0 * ahrs.getRawMagY()) / 100.0);
	          SmartDashboard.putNumber(   "RawMag_Z",             Math.round(100.0 * ahrs.getRawMagZ()) / 100.0);
	          SmartDashboard.putNumber(   "IMU_Temp_C",           Math.round(100.0 * ahrs.getTempC()) / 100.0);
	          
	          /* Omnimount Yaw Axis Information                                           */
	          /* For more info, see http://navx-mxp.kauailabs.com/installation/omnimount  */
	          AHRS.BoardYawAxis yaw_axis = ahrs.getBoardYawAxis();
	          SmartDashboard.putString(   "YawAxisDirection",     yaw_axis.up ? "Up" : "Down" );
	          SmartDashboard.putNumber(   "YawAxis",              yaw_axis.board_axis.getValue() );
	          
	          /* Sensor Board Information                                                 */
	          SmartDashboard.putString(   "FirmwareVersion",      ahrs.getFirmwareVersion());
	          
	          /* Quaternion Data                                                          */
	          /* Quaternions are fascinating, and are the most compact representation of  */
	          /* orientation data.  All of the Yaw, Pitch and Roll Values can be derived  */
	          /* from the Quaternions.  If interested in motion processing, knowledge of  */
	          /* Quaternions is highly recommended.                                       */
	          SmartDashboard.putNumber(   "QuaternionW",          Math.round(100.0 * ahrs.getQuaternionW()) / 100.0);
	          SmartDashboard.putNumber(   "QuaternionX",          Math.round(100.0 * ahrs.getQuaternionX()) / 100.0);
	          SmartDashboard.putNumber(   "QuaternionY",          Math.round(100.0 * ahrs.getQuaternionY()) / 100.0);
	          SmartDashboard.putNumber(   "QuaternionZ",          Math.round(100.0 * ahrs.getQuaternionZ()) / 100.0);
	          
	          /* Connectivity Debugging Support                                           */
	          SmartDashboard.putNumber(   "IMU_Byte_Count",       Math.round(100.0 * ahrs.getByteCount()) / 100.0);
	          SmartDashboard.putNumber(   "IMU_Update_Count",     Math.round(100.0 * ahrs.getUpdateCount()) / 100.0);
	      }
	}
}

