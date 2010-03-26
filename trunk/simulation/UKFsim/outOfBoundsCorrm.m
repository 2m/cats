function xcorrm=outOfBoundsCorrm(xm,xVm,bound)
%If estimate is outside the arena,
%set it to the previous instead
global k;
xcorrm=xm;    %default
if k~=1;
    if (xm(1)<bound(1) || xm(1)>bound(2) ||...
            xm(2)<bound(3) || xm(2)>bound(4))
        xcorrm=xVm(:,k-1);
    end
end