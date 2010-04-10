function x_update = update2(x)
%global nx;
F=eye(4);
%here dt=1 since speed is measured in m/samples, thus:
F(1,3)=1;
F(2,4)=1;
x_update=F*x(1:4,1);
x_update(5,1)=x(5,1);%atan2(x(4,1),x(3,1));
x_update(6,1)=x(6,1);