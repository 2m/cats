function reint(activeCats)
%reinitializes when cats drop out
global n;
global cats;
global R;
global r;
global catsFull;
n=0;
for i=1:length(activeCats)
    if activeCats(i)==1
        n=n+1;
        catsTemp(n,:)=catsFull(i,:);
    end
end
if n==0
    catsTemp=[];
end
R = r^2*eye(n);
cats=catsTemp;