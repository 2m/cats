function [R,actLandm]=fovCheck(z,fov,large,camA)
    global Rfull;
    global n;
    global r;
    R=Rfull;
    R(1:n,1:n)=large*eye(n);
    actLandm=[];
    for i=1:n
        if abs(z(i,1)-camA)<fov/2
            R(i,i)=r(1)^2;
            actLandm(end+1)=i;
        end
    end