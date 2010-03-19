function [errorPerf, varPerf]=perf(beta)
global mouse
global N;
betaErrorNorm=zeros(N,1);
for i=1:N
    betaErrorNorm(i,1)=norm(beta(i,1)-mouse);
end
errorPerf=sum(betaErrorNorm)/N;
varPerf=norm(var(betaErrorNorm));
return