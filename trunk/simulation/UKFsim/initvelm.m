function vm=initvelm
global N
vm=zeros(2,N);

turnInd = [90,120,240,300,400,N];
turnWait = 30; %value of m means m-1 steps wait
speed=0.013;

vm(:,1:turnInd(1))=+speed;

vm(1,turnInd(1)+turnWait:turnInd(2))=-speed;
vm(2,turnInd(1)+turnWait:turnInd(2))=speed;

vm(1,turnInd(2)+turnWait:turnInd(3))=0;
vm(2,turnInd(2)+turnWait:turnInd(3))=-speed;

vm(1,turnInd(3)+turnWait:turnInd(4))=speed;
vm(2,turnInd(3)+turnWait:turnInd(4))=0;

vm(1,turnInd(4)+turnWait:turnInd(5))=0;
vm(2,turnInd(4)+turnWait:turnInd(5))=speed;

vm(:,turnInd(5)+turnWait:turnInd(6))=-speed;