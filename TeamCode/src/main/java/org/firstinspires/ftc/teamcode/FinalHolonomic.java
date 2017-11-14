package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

/**
 * This is the final holonomic TeleOp file that will be used at the tournament, it incorporates
 * some designs used in my TeleOp concepts but also uses more proven and effective ways.
 * Created by Reece on 11/04/2017.
 */

@TeleOp(name = "FinalHolonomic")
public class FinalHolonomic extends OpMode {

    //Speed control coefficients
    private double speed    = 0.6;
    private double turn     = 0.2;

    //Servo claw variables
    private double sPos = 0;

    //Variable names used for cleaner code
    private int front   = 1;
    private int back    = -1;
    private int left    = -1;
    private int right   = 1;

    //Define drive train objects
    private DcMotor frontLeft   = null;
    private DcMotor backLeft    = null;
    private DcMotor frontRight  = null;
    private DcMotor backRight   = null;
    private Servo   jewel       = null;

    //Define glyph manipulator objects
    private DcMotor arm         = null;
    private Servo   leftClaw    = null;
    private Servo   rightClaw   = null;

    //Define relic manipulator objects
    private DcMotor slide   = null;
    private Servo   gripper = null;

    /**
     * The init method is run once when the init phase is active on the robot controller. This
     * method is used for hardware mapping hardware devices and setting parameters for the devices.
     */

    public void init() {

        //Hardware map drive train objects
        frontLeft   = hardwareMap.get(DcMotor.class,  "front left");
        backLeft    = hardwareMap.get(DcMotor.class,  "back left");
        frontRight  = hardwareMap.get(DcMotor.class,  "front right");
        backRight   = hardwareMap.get(DcMotor.class,  "back right");
        jewel       = hardwareMap.get(Servo.class,    "jewel");

        //Hardware map relic manipulator objects
        arm       = hardwareMap.get(DcMotor.class,    "arm");
        leftClaw  = hardwareMap.get(Servo.class,      "leftClaw");
        rightClaw = hardwareMap.get(Servo.class,      "rightClaw");

        //Hardware map relic manipulator objects
        //slide   = hardwareMap.get(DcMotor.class,  "slide");
        //gripper = hardwareMap.get(Servo.class,    "gripper");

        //Set direction of drive train motors
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        backLeft.setDirection(DcMotor.Direction.FORWARD);
        frontRight.setDirection(DcMotor.Direction.FORWARD);
        backRight.setDirection(DcMotor.Direction.FORWARD);

        //Set motor parameters of linear slide motor
        //slide.setDirection(DcMotor.Direction.FORWARD);
        //slide.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //Set motor parameters of arm motor
        arm.setDirection(DcMotor.Direction.REVERSE);
        arm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //Set servo parameters of jewel
        jewel.scaleRange(0.5, 1.0);
        jewel.setDirection(Servo.Direction.REVERSE);

        //Set servo parameters of claws
        leftClaw.setDirection(Servo.Direction.FORWARD);
        leftClaw.scaleRange(0.3, 1.0);
        rightClaw.setDirection(Servo.Direction.FORWARD);
        rightClaw.scaleRange(0.3, 1.0);

        //Set start position of servo claws
        leftClaw.setPosition(0.3);
        rightClaw.setPosition(0.3);
    }

    /**
     * The loop method is looped when the robot controller is in the start phase. It is used for the
     * core functionality of the robot like movement based on joysticks or other means.
     */

    public void loop() {

        //Send motor speeds to telemetry
        telemetry.addData("Speed FL", Double.toString(frontLeft.getPower()));
        telemetry.addData("Speed BL", Double.toString(backLeft.getPower()));
        telemetry.addData("Speed FR", Double.toString(frontRight.getPower()));
        telemetry.addData("Speed BR", Double.toString(backRight.getPower()));

        //Send joystick positions to telemetry
        telemetry.addData("Driver Left X", Double.toString(driverLeftX()));
        telemetry.addData("Driver Left Y", Double.toString(driverLeftY()));
        telemetry.addData("Driver Right X", Double.toString(driverRightX()));

        //Update telemetry
        telemetry.update();

        //Use driveCode method to set power to motors based on joystick values
        frontLeft.setPower(driveCode(front, left));
        backLeft.setPower(driveCode(back, left));
        frontRight.setPower(driveCode(front, right));
        backRight.setPower(driveCode(back, right));

        //Set wheels to brake when not being powered
        if (driverLeftX() + driverLeftY() == 0) {
            frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        } else {
            frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
            backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        }

        //Keep jewel servo upright so it doesn't get broken off
        jewel.setPosition(1.0);

        //Use armCode method to set power to arm motor based so it isn't too fast
        arm.setPower(armCode(manipulatorLeftY()));

        //Use clawCode method to set position of claw and keep values within range
        clawCode(leftClaw, rightClaw);
    }

    /**
     * The stop method is ran once when the robot controller ends the op mode. It is usually used to
     * turn off motors or other moving devices so the robot doesn't accidentally go haywire.
     */

    public void stop() {

        //Stop motors
        frontLeft.setPower(0);
        backLeft.setPower(0);
        frontRight.setPower(0);
        backRight.setPower(0);
        arm.setPower(0);
        //slide.setPower(0);

        //Reset servos
        jewel.setPosition(1);
        leftClaw.setPosition(0.3);
        rightClaw.setPosition(0.3);
        //gripper.setPosition(1);
    }

    /**
     * The driveCode method calculates what motors need to be set to what power by giving it which
     * side and end the motor is on
     *
     * @param side  The side (horizontally) that the motor is on
     * @param end   The end (vertically) that the motor is on
     * @return      Returns the power at which the motor needs to be set
     */

    private double driveCode(int side, int end) {
        double move = side * driverLeftX() + end * driverLeftY();
        double turn = driverRightX();
        return Range.clip(move * speed + turn * this.turn, -1, 1);
    }

    /**
     * The armCode method uses simple math in order to not have the arm slam into the robot when it
     * is set to go downwards
     *
     * @param joystick  The joystick values to multiply by and return
     * @return          The adjusted power returned from the method
     */

    private double armCode(double joystick) {
        double power;
        if (joystick < 0) {
            power = joystick / 5;
        } else {
            power = joystick / 2;
        }
        return power;
    }

    /**
     * The clawCode method is a slightly different approach I wanted to try in order to control the
     * claws. It directly sets the power to the servos supplied and manipulates a separate variable
     *
     * @param leftServo     The left servo you want to control
     * @param rightServo    The right servo you want to control
     */

    private void clawCode(Servo leftServo, Servo rightServo) {
        if (manipulatorLeftX() != 0 && sPos <= 1 && sPos >= 0) {
            sPos += 0.1;
        } else if (sPos > 1) {
            sPos = 1;
        } else if (sPos < 0) {
            sPos = 0;
        }
        leftServo.setPosition(sPos);
        rightServo.setPosition(sPos);
    }

    /**
     * The round method rounds the input to the place specified. Ex: tenths place is 0.1, hundredths
     * place is 0.01, etc.
     *
     * @param input The double you want to round
     * @param place The decimal place it should be rounded to, any multiple of 10 works
     * @return      Returns the rounded value
     */

    private double round(double input, double place) {
        double multiplier = 1 / place;
        return ((int) (input * multiplier)) / multiplier;
    }

    private double driverLeftX() {
        return round(gamepad1.left_stick_x, 0.1);
    }

    private double driverLeftY() {
        return round(gamepad1.left_stick_y, 0.1);
    }

    private double driverRightX() {
        return round(gamepad1.right_stick_x, 0.2);
    }

    private double manipulatorLeftX() {
        return round(gamepad2.left_stick_x, 0.05);
    }

    private double manipulatorLeftY() {
            return round(gamepad2.left_stick_y, 0.05);
        }
}