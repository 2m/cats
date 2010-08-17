A=dir;
mkdir('PDF');
for i=[3:size(A,1)]
    open(A(i,:).name);
    if (A(i,:).name(end-3:end) == '.fig');
        nameCut = A(i,:).name(1:end-4);
        set(gcf,'WindowStyle','modal')
        set(gcf,'Position',[10 800 725 533]);
        cd PDF
        savefig(strcat(nameCut,'.pdf'),'pdf');
        cd ..
    else
        continue
    end
end
close all