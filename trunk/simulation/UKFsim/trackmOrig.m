function [camA,dir]=trackmOrig(zm,speed,camA)
%camA is the angle of the camera (ralative to the robot) of a cat
%speed is the angular velocity pf the motor camera
% disp('diff:')
% zm-camAabs
if abs(zm-camA)>speed
    step=speed;
else
    step=zm-camA;
end
if camA<zm
   camA=camA+step;
   dir=1;
else
   camA=camA-step;
   dir=-1;
end