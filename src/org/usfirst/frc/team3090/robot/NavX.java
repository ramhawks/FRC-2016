package org.usfirst.frc.team3090.robot;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class NavX {
	private AHRS ahrs;
	private PIDController pid_controller;
	
	public void init() {
		try {
	          /* Communicate w/navX-MXP via the MXP SPI Bus.                                     */
	          /* Alternatively:  I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB     */
	          /* See http://navx-mxp.kauailabs.com/guidance/selecting-an-interface/ for details. */
	          ahrs = new AHRS(I2C.Port.kMXP); 
	      } catch (RuntimeException ex ) {
	          DriverStation.reportError("Error instantiating navX-MXP:  " + ex.getMessage(), true);
	      }
	}
	
	public void showData(Joystick stick) {
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
	
	public void showTestData(Joystick stick) {
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
