function zcorrm=noCheatm(zm)
    %If no measurement available, use the last available
    %This measurement will be given a very small wheight in R, to be used ukf,
    %but just so were not cheating
    global nm;
    global zVm;
    global k;
    global actCats;
    
    zcorrm=zm; %default
    if k==1
        zcorrm(1:nm,1)=pi; %best initial guess
    else
        zcorrm(1:nm,1)=zVm(1:nm,k-1);    %best guess, use last available value
    end
    %Use only available measurements
    for i=actCats
        zcorrm(i,1)=zm(i,1);
    end