clc
clear
figure
%close all
%n=3;
%cats=[0, 0; 0, 0.8; 0.8, 0];
n=2;
cats=[0, 0.8; 0.8, 0];
mouse=[0.8;1];
N=500;
beta=zeros(N,2);
for j=1:N
    th=zeros(n,1);
    for i=1:n
        th(i,1)=atan((mouse(2,1)-cats(i,2))/(mouse(1,1)-cats(i,1)));
    end
    th=th+0.08*randn(n,1);
    
    A=zeros(n,2);
    C=zeros(n,1);
    for i=1:n
        A(i,:)=[tan(th(i,1)), -1];
        C(i,1)=tan(th(i,1))*cats(i,1)-cats(i,2);
    end
    
    beta(j,:)=A\C;
end

% %Find crossing bearings, grouped by 2
% Acell=cell(3,1);
% Ccell=cell(3,1);
% 
% Acell{1}=[A(1,:); A(2,:)];
% Acell{2}=[A(2,:); A(3,:)];
% Acell{3}=[A(1,:); A(3,:)];
% 
% Ccell{1}=[C(1); C(2)];
% Ccell{2}=[C(2); C(3)];
% Ccell{3}=[C(1); C(3)];
% 
% betacross=zeros(n,2);
% for i=1:n
%     betacross(i,:)=Acell{i}\Ccell{i};
% end
% 
% faraway=10;
% slope(1)=(cats(1,2)-betacross(1,2))/(cats(1,1)-betacross(1,1));
% raysX(1,:)=[cats(1,1), betacross(1,1)+faraway];
% raysY(1,:)=[cats(1,2), betacross(1,2)+faraway*slope(1)];
% 
% slope(2)=(cats(2,2)-betacross(2,2))/(cats(2,1)-betacross(2,1));
% raysX(2,:)=[cats(2,1), betacross(2,1)+faraway];
% raysY(2,:)=[cats(2,2), betacross(2,2)+faraway*slope(2)];
% 
% slope(3)=(cats(3,2)-betacross(3,2))/(cats(3,1)-betacross(3,1));
% raysX(3,:)=[cats(3,1), betacross(3,1)+faraway];
% raysY(3,:)=[cats(3,2), betacross(3,2)+faraway*slope(3)];
% 
% betaError=norm(beta-mouse)
% 
% plot(raysX',raysY','k',betacross(:,1),betacross(:,2),'o',beta(1,1),beta(2,1),'xr',...
%     mouse(1,1),mouse(2,1),'mx',cats(:,1),cats(:,2),'*k')
% 

plot(beta(:,1),beta(:,2),'.b',...
    mouse(1,1),mouse(2,1),'rx',cats(:,1),cats(:,2),'*k')

%axis([0 1 0 1])


