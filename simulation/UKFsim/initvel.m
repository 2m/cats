function vc=initvel
global N
vc=zeros(2,N);

turnInd = [90,120,240,300,400,N];
turnWait = 30; %value of m means m-1 steps wait
speed=0.013;

vc(:,1:turnInd(1))=+speed;

vc(1,turnInd(1)+turnWait:turnInd(2))=-speed;
vc(2,turnInd(1)+turnWait:turnInd(2))=speed;

vc(1,turnInd(2)+turnWait:turnInd(3))=0;
vc(2,turnInd(2)+turnWait:turnInd(3))=-speed;

vc(1,turnInd(3)+turnWait:turnInd(4))=speed;
vc(2,turnInd(3)+turnWait:turnInd(4))=0;

vc(1,turnInd(4)+turnWait:turnInd(5))=0;
vc(2,turnInd(4)+turnWait:turnInd(5))=speed;

vc(:,turnInd(5)+turnWait:turnInd(6))=-speed;