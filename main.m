clc, clear
%Generate mouse
mouse_cart = target;
theta_mouse = cart2pol(mouse_cart(1), mouse_cart(2));
%Find bearing to second cat
tower_cart = [0.8; 0];
theta_tower = bearing(tower_cart, mouse_cart);
%Find position of the target mouse from the two bearings
r_mouse = - tower_cart(1)*sin(theta_tower) /...
 sin(theta_mouse - theta_tower );

[mouseCheck_cart(1), mouseCheck_cart(2)] = pol2cart(theta_mouse,r_mouse);

%Plotting

plot(mouse_cart(1), mouse_cart(2), 'xr',0, 0, 'x',...
    tower_cart(1), tower_cart(2), 'x',...
    mouseCheck_cart(1), mouseCheck_cart(2), 'ok')
    
