package nxt.brick;

import nxt.contoller.Controller;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;

/**
* The Control class. Handles the actual driving and movement of the robot, once
* BotCommunication has processed the commands.
*
* That is -- defines the behaviour of the robot when it receives the command.
*
* Adapted from SDP2013 groups 7 code -- original author sauliusl
*
* @author Ross Grassie
*/
public class Movement implements Controller {
        
		public int maxPilotSpeed = 20;
		static final int TYRE_DIAMETER = 56;
		static final int TRACK_WIDTH = 116;
		static final int TRAVEL_SPEED = 90;
    	static NXTRegulatedMotor LEFT_WHEEL = Motor.B;
    	static NXTRegulatedMotor RIGHT_WHEEL = Motor.C;
    	static NXTRegulatedMotor KICKER = Motor.A;
    	static DifferentialPilot pilot = new DifferentialPilot(TYRE_DIAMETER, TRACK_WIDTH, LEFT_WHEEL, RIGHT_WHEEL);

        public final boolean INVERSE_WHEELS = false;


        // TODO: potential changes to be made here due to different robots
        public static final int MAXIMUM_MOTOR_SPEED = 900;
        public static final int ACCELERATION = MAXIMUM_MOTOR_SPEED * 8;
        public static final int GEAR_ERROR_RATIO = 2; // Gears cut our turns in half


        private volatile boolean isKicking = false;

        public Movement() {

                pilot = new DifferentialPilot(TYRE_DIAMETER, TRACK_WIDTH, LEFT_WHEEL,
                                RIGHT_WHEEL, INVERSE_WHEELS);
                pilot.setTravelSpeed(maxPilotSpeed);
                pilot.setRotateSpeed(45);
                pilot.setAcceleration(ACCELERATION);

        }

        /*
         * (non-Javadoc)
         * 
         * TODO: May not be necessary
         *
         * @see balle.brick.Controller#floatWheels()
         */
        @Override
        public void floatWheels() {
                LEFT_WHEEL.flt();
                RIGHT_WHEEL.flt();
        }

        /*
         * (non-Javadoc)
         *
         * @see balle.brick.Controller#stop()
         */
        @Override
        public void stop() {
                pilot.setAcceleration(ACCELERATION * 2);
                pilot.stop();
                pilot.setAcceleration(ACCELERATION);
        }

        /*
         * (non-Javadoc)
         *
         * @see balle.brick.Controller#kick()
         */
        
        @Override
        public void kick() {

                if (isKicking) {
                	LCD.drawString("Preparing to kick", 0, 0);
                    return;
                }

                isKicking = true;

                KICKER.setSpeed(MAXIMUM_MOTOR_SPEED);

                // Move kicker back
                LCD.drawString("moving back", 0, 1);
                KICKER.rotateTo(-10);
                KICKER.waitComplete();

                // Kick
                KICKER.rotateTo(36);
                KICKER.waitComplete();

                // Reset
                KICKER.rotateTo(-36);
                KICKER.waitComplete();

                KICKER.flt();

                isKicking = false;
        }

        public float getTravelDistance() {
                return pilot.getMovementIncrement();
        }

        public void reset() {
                pilot.reset();
        }

        private void setMotorSpeed(NXTRegulatedMotor motor, int speed) {
                boolean forward = true;
                if (speed < 0) {
                        forward = false;
                        speed = -1 * speed;
                }

                motor.setSpeed(speed);
                if (forward)
                        motor.forward();
                else
                        motor.backward();
        }

        @Override
        public void setWheelSpeeds(int leftWheelSpeed, int rightWheelSpeed) {
                if (leftWheelSpeed > MAXIMUM_MOTOR_SPEED)
                        leftWheelSpeed = MAXIMUM_MOTOR_SPEED;
                if (rightWheelSpeed > MAXIMUM_MOTOR_SPEED)
                        rightWheelSpeed = MAXIMUM_MOTOR_SPEED;

                if (INVERSE_WHEELS) {
                        leftWheelSpeed *= -1;
                        rightWheelSpeed *= -1;
                }
                setMotorSpeed(LEFT_WHEEL, leftWheelSpeed);
                setMotorSpeed(RIGHT_WHEEL, rightWheelSpeed);
        }

        @Override
        public int getMaximumWheelSpeed() {
                return MAXIMUM_MOTOR_SPEED;
        }

        @Override
        public void backward(int speed) {
                pilot.setTravelSpeed(speed);
                // setWheelSpeeds(speed, speed);
                pilot.backward();
        }

        @Override
        public void forward() {
                pilot.setTravelSpeed(TRAVEL_SPEED);
                pilot.forward();

        }

        @Override
        /*
         * (non-Javadoc)
         * 
         * Don't think this code is necessary
         * 
         * @see nxt.contoller.Controller#forward(int, int)
         */
        public void forward(int left, int right) {
                setWheelSpeeds(left, right);
        }

        /*
         * TODO: potentially unnecessary code
         * (non-Javadoc)
         * 
         * @see nxt.contoller.Controller#rotate(int, int)
         */
        @Override
        public void rotate(int deg, int speed) {
                pilot.setRotateSpeed(speed);
                pilot.rotate(deg); // GEAR_ERROR_RATIO
        }

        @Override
        public void penaltyKick() {
                int turnAmount = 27;
                if (Math.random() <= 0.5)
                        turnAmount *= -1;
                rotate(turnAmount, 180);
                kick();
        }

        @Override
        public void penaltyKickStraight() {
                kick();
        }

        @Override
        public boolean isReady() {
                return true;
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }

        /* TODO: potentially change or remove these as Tachometer appears to be out-dated.
         * 		
         * 		If altering look at the OdometryPoseProvider class 
         * 
         */
        public int getLeftTacho() {
                return LEFT_WHEEL.getTachoCount();
        }

        public int getRightTacho() {
                return RIGHT_WHEEL.getTachoCount();
        }

        public void resetLeftTacho() {
                LEFT_WHEEL.resetTachoCount();
        }

        public void resetRightTacho() {
                RIGHT_WHEEL.resetTachoCount();
        }

        public int getLeftSpeed() {
                return LEFT_WHEEL.getSpeed();
        }

        public int getRightSpeed() {
                return RIGHT_WHEEL.getSpeed();
        }

        public void setLeftSpeed(int speed) {
                setMotorSpeed(LEFT_WHEEL, speed);
        }

        public void setRightSpeed(int speed) {
                setMotorSpeed(RIGHT_WHEEL, speed);
        }

}
