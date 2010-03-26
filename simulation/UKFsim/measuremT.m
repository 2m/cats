function zm = measuremT(xm)
%calculates the bearings from the cats to the mouse and adds measurement
%noise
global s; %where we think the cats are
global nm;
zm=zeros(nm,1);
for i=1:nm
    %use either acos or asin
    if (xm(2,1)-s(2,i)>=0) %needed because of ambiguity in acos (and asin)
                             %can be simplified(?)
        zm(i,1)=acos((xm(1,1)-s(1,i)) /...
           sqrt((xm(1,1)-s(1,i))^2 + (xm(2,1)-s(2,i))^2));
    else
        zm(i,1)=2*pi-acos((xm(1,1)-s(1,i)) /...
            sqrt((xm(1,1)-s(1,i))^2 + (xm(2,1)-s(2,i))^2));
    end
    
%      zm(i,1)=atan2(xm(2,1)-s(2,i),xm(1,1)-s(1,i));
%      if zm(i,1)<0
%          zm(i,1)=2*pi+zm(i,1);
%      end
    
    %Alternative: (somtimes less divergent, sometimes more)
    %z(i,1)=atan2(x(2,1)-cats(i,2),x(1,1)-cats(i,1));
end