package org.firstinspires.ftc.teamcode.newLib.hardware;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.newLib.Robot;
import org.firstinspires.ftc.teamcode.newLib.Targets;
import org.firstinspires.ftc.teamcode.newLib.Comms;

@Config
public class Shooter {

    // Define 3 states. on, off or reverse

    private enum FeederState {
        PUSHING,
        RETRACTING,
        RETRACTED
    }

    private DcMotorEx shooter1;
    private DcMotorEx shooter2;

    private Servo feeder;
    private Servo flap;

    private Robot.Mode mode;
    private Robot.Mode lastMode;

    private FeederState feederState;

    private double currentRuntime;
    private double actuationTime = 0.19;
    private double startTime;

    private final double feederStartPosition = 0.33;
    private final double feederExtendedPosition = 0.5;

    private double targetVelocity;

    private double flapPos = 0.6;

    public Shooter() {
        shooter1 = Comms.hardwareMap.get(DcMotorEx.class, "shooter1");
        shooter2 = Comms.hardwareMap.get(DcMotorEx.class, "shooter2");
        feeder = Comms.hardwareMap.get(Servo.class, "feeder");
        flap = Comms.hardwareMap.get(Servo.class, "flap");
        feeder.setPosition(feederStartPosition);
        feederState = FeederState.RETRACTED;
        shooter1.setDirection(DcMotorEx.Direction.FORWARD);
        shooter2.setDirection(DcMotorEx.Direction.FORWARD);
        shooter1.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        shooter2.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        shooter1.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        shooter2.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.FLOAT);
        shooter1.setPower(0);
        shooter2.setPower(0);
        mode = Robot.Mode.IDLE;

        setTargetVelocity(5000);
    }

    public Robot.Mode getMode() {
        return mode;
    }

    public double getShooterVelocity() {
        return 0.5*(shooter1.getVelocity() + shooter2.getVelocity());
    }
    
    public double getTargetVelocity() {
        return targetVelocity;
    }

    public void setTargetVelocity(double targetVelocity) {
        this.targetVelocity = targetVelocity;
    }

    public void shoot() {
        if(feederState == FeederState.RETRACTED && shooter1.getPower() != 0) {
            feeder.setPosition(feederExtendedPosition);
            startTime = currentRuntime;
            feederState = FeederState.PUSHING;
        }
    }

    public void reset() { //ONLY for autonomous
        feeder.setPosition(feederStartPosition);
        feederState = FeederState.RETRACTED;
    }

    //flap
    public void setFlapPos(double flapPos) {
        this.flapPos = flapPos;
    }

    public void toggleFlap(Boolean enabled) {
        if (enabled) {
            flap.setPosition(flapPos);
        } else {
            flap.setPosition(0.51);
        }
    }

    public void updateFlapPos(double[] position) {
        double distance = Math.sqrt(Math.pow(position[0] - Targets.currentTarget.getX(), 2)
                + Math.pow(position[1] - Targets.currentTarget.getY(), 2));
        flapPos = flapPos(distance);
    }

    private double flapPos(double distance) {
        double baseFlapValue = 0;
        double a = 0;
        double b = 0;
        return baseFlapValue + a * Math.sqrt(b * distance);
    }

    public Robot.Mode getLastMode(){
        return lastMode;
    }

    public void setMode(Robot.Mode mode) {
        if (this.mode != mode) {
            lastMode = this.mode;
        }
        this.mode = mode;
        switch (this.mode)
        {
            case IDLE: //no power
                shooter1.setPower(0);
                shooter2.setPower(0);
                break;
            case RUNNING: //shoot
                shooter1.setVelocity(targetVelocity);
                shooter2.setVelocity(targetVelocity);
                break;
        }
    }

    public void update(double currentRuntime) {
        if (Comms.driveMode == Comms.DriveMode.GOAL_CENTRIC) {
            //updateFlapPos(5);   //ToDo
        }
        this.currentRuntime = currentRuntime;
    }
}