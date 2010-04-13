function vc=initvelCircle
%initializes the velocities of the cats
global N
global nm;
global phi;   %angle of the circular movement of the cat
speed=0.03;
vc=cell(1,nm);
for i=1:nm
    vc{1,i}=zeros(2,N);
end

for k=1:N
    for i=1:nm
        vc{1,i}(:,k)=speed*[-sin(phi(i));cos(phi(i))];
        phi(i)=mod(phi(i)+3*2*pi/N,2*pi);
    end
end