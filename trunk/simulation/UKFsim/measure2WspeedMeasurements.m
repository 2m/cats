function z = measure2WspeedMeasurements(x)
%calculates the bearings from the landm to the mouse and adds measurement
%noise
global landm;
global n;

for i=1:n
    %use either acos or asin
%     if (landm(i,2)-x(2,1)>=0) %needed because of ambiguity in acos (and asin)
%         %can be simplified(?)
%         z(i,1)=acos((landm(i,1)-x(1,1)) /...
%             sqrt((landm(i,1)-x(1,1))^2 + (landm(i,2)-x(2,1))^2));
%     else
%         z(i,1)=2*pi-acos((x(1,1)-landm(i,1)) /...
%             sqrt((landm(i,1)-x(1,1))^2 + (landm(i,2)-x(2,1))^2));
%     end
%something wrong with the above, use instead:
    
    %Alternative: (somtimes less divergent, sometimes more)
     z(i,1)=atan2(landm(i,2)-x(2,1),landm(i,1)-x(1,1));
     if z(i,1)<0
         z(i,1)=2*pi+z(i,1);
     end
end
z(n+1,1)=x(3,1);
z(n+2,1)=x(4,1);
z(n+3,1)=x(6,1);

%spoiled, rather than helped:
%z(n+1,1)=sqrt(x(3,1)^2+x(4,1)^2);%norm(x(3:4)); 