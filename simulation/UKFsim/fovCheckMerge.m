function [Rm,actLandm,actCats]=fovCheckMerge(z,zm,fov,large,camAabs)
    %global Rfull;
    global n;
    global r;
    %global Rfullm;
    global nm;
    global rm;
    global R;
    
    for i=1:nm
        %R{1,i}=Rfull{1,i};
        R{1,i}(1:n,1:n)=large*eye(n);
        actLandm{1,i}=[];
        for j=1:n
            if abs(z(j,i)-camAabs(i))<fov/2
                R{1,i}(j,j)=r(1)^2;
                actLandm{1,i}(end+1)=j;
            end
        end
    end
    
   % Rm=Rfullm;
    Rm(1:nm,1:nm)=large*eye(nm);
    actCats=[];
    for i=1:nm
        if abs(zm(i,1)-camAabs(i))<fov/2
            Rm(i,i)=rm^2;
            actCats(end+1)=i;
        end
    end