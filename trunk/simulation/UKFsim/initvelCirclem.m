function vm=initvelCirclem
global N
vm=zeros(2,N);
phi=0;   %angle of the circular movement of the mouse
speed=0.02;
for k=1:N
    vm(:,k)=speed*[-sin(phi);cos(phi)];
    phi=mod(phi + 5*2*pi/N,2*pi);
end