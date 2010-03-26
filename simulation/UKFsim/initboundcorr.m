function xcorr=initboundcorr(x,bound)
xcorr=x; %default
if x(1)<bound(1)
    xcorr(1)=bound(1);
end
if x(1)>bound(2)
    xcorr(1)=bound(2);
end
if x(2)<bound(3)
    xcorr(2)=bound(3);
end
if x(2)>bound(4)
    xcorr(2)=bound(4);
end