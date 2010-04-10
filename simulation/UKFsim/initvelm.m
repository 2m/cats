function vm=initvelm
global N
vm=zeros(2,N);

turnInd = [1*50,2*50,3*50,4*50,5*50,N];
turnWait = 10; %value of m means m-1 steps wait
speed=0.03;

% vm(:,1:turnInd(1))=+speed;
% 
% vm(1,turnInd(1)+turnWait:turnInd(2))=-speed;
% vm(2,turnInd(1)+turnWait:turnInd(2))=speed;
% 
% vm(1,turnInd(2)+turnWait:turnInd(3))=0;
% vm(2,turnInd(2)+turnWait:turnInd(3))=-speed;
% 
% vm(1,turnInd(3)+turnWait:turnInd(4))=speed;
% vm(2,turnInd(3)+turnWait:turnInd(4))=0;
% 
% vm(1,turnInd(4)+turnWait:turnInd(5))=0;
% vm(2,turnInd(4)+turnWait:turnInd(5))=speed;
% 
% vm(:,turnInd(5)+turnWait:turnInd(6))=-speed;

vm(1,1:turnInd(1))=+speed;
vm(1,turnInd(1)+turnWait:turnInd(2))=-speed;
vm(1,turnInd(2)+turnWait:turnInd(3))=+speed;
vm(1,turnInd(3)+turnWait:turnInd(4))=-speed;
vm(1,turnInd(4)+turnWait:turnInd(5))=+speed;