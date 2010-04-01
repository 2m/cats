function camA=searchm(speed,camA)
%camA is the angle of the camera (ralative to the robot) of a cat
%speed is the angular velocity pf the motor camera
    camA=mod(camA+speed,2*pi);