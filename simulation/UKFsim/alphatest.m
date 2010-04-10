close all
clc
figure
colormap winter;
plot(linspace(0,15,10),linspace(0,4,10),...
    linspace(0,9,10),linspace(0,5,10),...
    linspace(0,15,10),linspace(0,5,10))
map=[0 0 0;0 0 1;0 1 0;1 0 0];
