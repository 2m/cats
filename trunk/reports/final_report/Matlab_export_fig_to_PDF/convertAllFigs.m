%Default width and height of the figure to be saved. The size of the figure
%determines the relative size of the graphics such as axis names.
Wx=715;
Wy=83;
A=dir;
mkdir('PDFs');
for i=[3:size(A,1)]
    if (A(i,:).name(end-3:end) == '.fig');
        open(A(i,:).name);
        nameCut = A(i,:).name(1:end-4);
        set(gcf,'WindowStyle','modal')
        set(gcf,'Position',[10 450 10+Wx 450+Wy]);
        cd PDFs
        savefig(strcat(nameCut,'.pdf'),'pdf');
        cd ..
    else
        continue
    end
end
close all