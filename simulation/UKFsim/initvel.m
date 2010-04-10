function vc=initvel
%initializes the velocities of the cats
global N
global nm;

vc=cell(1,nm);
for i=1:nm
    vc{1,i}=zeros(2,N);
end

turnInd = [100,200,300,400,N];
turnWait = 30; %value of m means m-1 steps wait
speed=0.013;

vcSingle=zeros(2,N);
vcSingle(1,1:turnInd(1))=+speed;
vcSingle(1,turnInd(1)+turnWait:turnInd(2))=-speed;
vcSingle(1,turnInd(2)+turnWait:turnInd(3))=+speed;
vcSingle(1,turnInd(3)+turnWait:turnInd(4))=-speed;
vcSingle(1,turnInd(4)+turnWait:turnInd(5))=+speed;

vc{1,1}=vcSingle;
vc{1,2}=-vcSingle;

