function z = measure(x)
%calculates the bearings from the cats to the mouse and adds measurement
%noise
global catsClean; %where we think the cats are
global n;
for i=1:n
    %use either acos or asin
    if (x(2,1)-catsClean(i,2)>=0) %needed because of ambiguity in acos (and asin)
                             %can be simplified(?)
        z(i,1)=acos((x(1,1)-catsClean(i,1)) /...
           sqrt((x(1,1)-catsClean(i,1))^2 + (x(2,1)-catsClean(i,2))^2));
    else
        z(i,1)=2*pi-acos((x(1,1)-catsClean(i,1)) /...
            sqrt((x(1,1)-catsClean(i,1))^2 + (x(2,1)-catsClean(i,2))^2));
    end
    
    %Alternative: (somtimes less divergent, sometimes more)
    %z(i,1)=atan2(x(2,1)-cats(i,2),x(1,1)-cats(i,1));
end