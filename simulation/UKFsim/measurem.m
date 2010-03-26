function zm = measurem(xm)
%calculates the bearings from the cats to the mouse and adds measurement
%noise
global x; %where we think the cats are
global nm;
for i=1:nm
    %use either acos or asin
    if (xm(2,1)-x(2,i)>=0) %needed because of ambiguity in acos (and asin)
                             %can be simplified(?)
        zm(i,1)=acos((xm(1,1)-x(1,i)) /...
           sqrt((xm(1,1)-x(1,i))^2 + (xm(2,1)-x(2,i))^2));
    else
        zm(i,1)=2*pi-acos((xm(1,1)-x(1,i)) /...
            sqrt((xm(1,1)-x(1,i))^2 + (xm(2,1)-x(2,i))^2));
    end
    
    %Alternative: (somtimes less divergent, sometimes more)
    %z(i,1)=atan2(x(2,1)-cats(i,2),x(1,1)-cats(i,1));
end