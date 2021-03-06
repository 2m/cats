function zcorr=noCheat(z)
%If no measurement available, use the last available
%This measurement will be given a very small wheight in R, to be used ukf,
%but just so were not cheating
global n;
global nm;
global zV;
global k;
global activeLandm;

zcorr=z; %default
for i=1:nm
    if k==1
        zcorr(1:n,i)=pi; %best initial guess
    else
        zcorr(1:n,i)=zV{1,i}(1:n,k-1);    %best guess, use last available value
    end
    %Use only available measurements
    for j=activeLandm{1,i}(:)
        zcorr(j,i)=z(j,i);
    end
end