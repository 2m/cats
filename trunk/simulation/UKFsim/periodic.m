function periodic(per,large)
global k;
global actLandm;
global R;
global Rfull;

    iper=floor(k/per);
    if k==round(iper*per + 1)
        R=Rfull;
        R(2,2)=large;
        R(3,3)=large;
        R(4,4)=large;
        actLandm=[1];
    end
    if k==round(iper*per + 1*per/4)
        R=Rfull;
        R(1,1)=large;
        R(2,2)=large;
        R(3,3)=large;
        R(4,4)=large;
        actLandm=[];
    end
    if k==round(iper*per + 2*per/4)
        R=Rfull;
        R(1,1)=large;
        R(3,3)=large;
        R(4,4)=large;
        actLandm=[2];
    end
    if k==round(iper*per + 3*per/4)
        R=Rfull;
        R(1,1)=large;
        R(2,2)=large;
        R(3,3)=large;
        R(4,4)=large;
        actLandm=[];
    end
    if k==round(iper*per + 4*per/4)
        R=Rfull;
        R(1,1)=large;
        R(2,2)=large;
        R(4,4)=large;
        actLandm=[3];
    end
    if k==round(iper*per + 5*per/4)
        R=Rfull;
        R(1,1)=large;
        R(2,2)=large;
        R(3,3)=large;
        R(4,4)=large;
        actLandm=[];
    end
    if k==round(iper*per + 6*per/4)
        R=Rfull;
        R(1,1)=large;
        R(2,2)=large;
        R(3,3)=large;
        actLandm=[4];
    end
    if k==round(iper*per + 7*per/4)
        R=Rfull;
        R(1,1)=large;
        R(2,2)=large;
        R(3,3)=large;
        R(4,4)=large;
        actLandm=[];
    end