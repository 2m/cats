function ang=camAng(k,speed)
global N;
ang=mod(k*speed*2*pi/N,2*pi);