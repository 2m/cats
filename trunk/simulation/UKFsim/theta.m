function th=theta(mouse)
    global cats
    global n;
    th=zeros(n,1);
    for i=1:n
        th(i,1)=atan((mouse(2,1)-cats(i,2))/(mouse(1,1)-cats(i,1)));
    end
    return