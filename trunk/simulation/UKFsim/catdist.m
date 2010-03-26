function r = catdist(cats)
global mouse;
global n;
r=zeros(n,1);
for i=1:3
r(i,:)=sqrt(cats(i,:)-mouse(:,1));
end
return