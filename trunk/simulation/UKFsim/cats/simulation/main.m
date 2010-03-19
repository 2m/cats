clc, clear
tic
N=200000; %Number of iteration in Monte Carlo simulation
%Generate mouse
mouse_cart = target; %initialize target position
theta_mouse(1:N) = cart2pol(mouse_cart(1), mouse_cart(2))...
    + 0.1*randn(1,N); %target angle (gaussian error added)

%Find bearing to second cat
tower_cart = [1; 0]; %position of the second tower
theta_tower(1:N) = bearing(tower_cart, mouse_cart)...
    +0.1*randn(1,N); %the second tower's angle to the target

%Find position of the target mouse from the two bearings
figure
ax=[0 2 0 3.5];
axis(ax);
hold on
pos(:,1) = zeros(N,1);
pos(:,2) = zeros(N,1);

for i=1:N %calculate position of the target from the two angles
        pos(i,1) = -tower_cart(1)*sin(theta_tower(i))*cos(theta_mouse(i))/...
         sin(theta_mouse(i) - theta_tower(i) );
        pos(i,2) = -tower_cart(1)*sin(theta_tower(i))*sin(theta_mouse(i))/...
         sin(theta_mouse(i) - theta_tower(i) ); 
end

%fit a gaussian distribution to the data
obj = gmdistribution.fit(pos,2); %NB: parameter k (should be 2?)
pdfEst = pdf(obj,pos); %estimate the PDF
xlin = linspace(min(pos(:,1)),max(pos(:,1)),1000); %init gridlines
ylin = linspace(min(pos(:,2)),max(pos(:,2)),1000);
[Xm,Ym] = meshgrid(xlin,ylin); %construct mesh
Z = griddata(pos(:,1),pos(:,2),pdfEst,Xm,Ym,'cubic'); %interpolate the PDF's
    %values at the mesh points

contour(Xm,Ym,Z) %countour plot with exact position shown by the 
    %crossing of the red rays
plot([0 mouse_cart(1)*50], [0 mouse_cart(2)*50], 'r', 0, 0, 'o',...
    tower_cart(1), tower_cart(2), 'o',...
    [tower_cart(1) mouse_cart(1)*50 - tower_cart(1)*(50-1)],...
    [tower_cart(2) mouse_cart(2)*50], 'r')
hold off

%Opional: scatter plot. slow for large N!
% figure
% hold on
% axis(ax);
% scatter(pos(:,1), pos(:,2), 1, '.k')
% plot([0 mouse_cart(1)*50], [0 mouse_cart(2)*50], 'r', 0, 0, 'o',...
%     tower_cart(1), tower_cart(2), 'o',...
%     [tower_cart(1) mouse_cart(1)*50 - tower_cart(1)*(50-1)],...
%     [tower_cart(2) mouse_cart(2)*50], 'r')

toc %display elapsed time, good for persormance comparison
    
