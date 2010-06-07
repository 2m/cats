x=1.5;
y=1.5;

kor = [x y];

g= atan2(3-y,-x)-atan2(3-y,3-x);
G=g*180/pi

h= atan2(-y,-x)+2*pi-atan2(3-y,-x);
H=h*180/pi

b = [pi/2
     pi-g
    % pi/2-g    
     pi/2
     pi-h
    % pi/2-h    
     pi/2
     0;
     pi/2
     0];
    

A=  [1 1 0 0 0 0    %a+b=pi/2
     0 1 1 0 0 0    %b+c=pi-g
    %-1 0 1 0 0 0    %-a+c=pi/2-g   beroende
     0 0 0 1 1 0    %d+e=pi/2
     0 0 0 0 1 1    %e+f=pi-h
    % 0 0 0 -1 0 1   %-d+f=pi/2-h   beroende
     0 1 0 0 1 0   %b+e=pi/2
     1 0 0 0 -1 0   %a-e=0         beroende
     1 0 0 1 0 0  %a+d=pi/2
     0 1 0 -1 0 0]
X=b\A
X*(180/pi)

