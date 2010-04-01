function x_update = update(x)
%global nx;
F=eye(4);
%here sampling period is dt=1, since speed is measured in meters/samples, thus:
F(1,3)=1;
F(2,4)=1;
x_update=F*x;