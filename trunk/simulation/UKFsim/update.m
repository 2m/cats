function x_update = update(x)
global dt;
global nx;
F=eye(nx);
F(1,3)=dt;
F(2,4)=dt;
x_update=F*x;