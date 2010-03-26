function x_update = update(x)
%global nx;
F=eye(4);
%here dt=1 since speed is measured in m/samples, thus:
F(1,3)=1;
F(2,4)=1;
x_update=F*x;