function camA=searchm(speed,camA,dir)
%camA is the angle of the camera (ralative to the robot) of a cat
%speed is the angular velocity pf the motor camera
    camA=camA+dir*speed;