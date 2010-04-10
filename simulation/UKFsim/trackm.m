function [camA,dir]=trackm(zm,speed,camA,camAabs)
%camA is the angle of the camera (ralative to the robot) of a cat
%speed is the angular velocity pf the motor camera
if abs(zm-camAabs)>speed
    step=speed;
else
    step=zm-camAabs;
end
if camAabs<zm
   camA=camA+step;
   dir=1;
else
   camA=camA-step;
   dir=-1;
end