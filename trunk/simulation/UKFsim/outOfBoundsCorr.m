function xcorr=outOfBoundsCorr(x,xV,bound)
%If estimate is outside the arena,
%set it to the previous instead
global k;
xcorr=x;    %default
if k~=1;
    if (x(1)<bound(1) || x(1)>bound(2) ||...
            x(2)<bound(3) || x(2)>bound(4))
        xcorr=xV(:,k-1);
    end
end