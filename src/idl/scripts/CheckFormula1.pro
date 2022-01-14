pro checkformula1

    Msun   = 1.9885d30
    Mearth = 5.97219d24
    G      = 6.6743d-11
    au     = 149.597870700d9

    r = [2.5d0,1.7d0,0.3d0]*au;
    v = [4000d0, 10000d0, 100d0];

    m0 = Msun/3;
    m1 = Mearth*10000;

    r1 = r*m0/(m0+m1);
    v1 = v*m0/(m0+m1);
    print, "Adjusted position: ",r1
    print, "Adjusted velocity: ",v1
    print, "Input masses:", m0, m1
    m = m0^3/(m0+m1)^2
    print, "Adjusted mass:", m
    Ls = crossp(r1,v1);
    print, "Specific angular momentum:",Ls
    Lmag = sqrt(Total(Ls*Ls))
    print, "Magnitude of angular momentum:",Lmag
    i = acos(Ls[2]/LMag)/!dtor;
    print, "Inclination: ",i
    lnodes = crossp([0,0,1],Ls);
    print, "Line of nodes vector (unscaled):", lnodes
    magLn = sqrt(total(lnodes*lnodes))
    lnodes = lnodes/magLn
    print, "Line of nodes vector:", lnodes
    print, "Longitude of ascending node: ", atan(lnodes[1], lnodes[0])/!dtor

    vcrossL = crossp(v1, Ls);
    print, "VcrossL: ", vcrossL/G/M;
    print, "GM:",G*m
    rmag = sqrt(total(r*r));
    print, "Norm:",r/rmag
    eccVec = vcrossL/(G*M) - r/rmag;
    print, "Eccen vec:", eccVec;
    ecc = sqrt(total(eccVec*eccVec));
    print,"Eccen:", ecc; 

    LxE = crossp(lnodes, eccVec);
    magLxE = sqrt(total(LxE*LxE))
    sinNu = magLxE/ecc;
    print,"LxE, Ls:", LxE, Ls, LxE/Ls;
    print,"MagLxE:", magLxE;
    LdE = total(lnodes*eccVec);
    cosNu = LdE/ecc;
    print, "Init: sin nu, cos nu:", sinNu, cosNu;
    if (total(LxE*Ls) lt 0) then begin
        sinNu = -sinNu;
    endif
    print, "final: sin nu, cos nu:", sinNu, cosNu;
    print, "Nu: ",atan(sinNu, cosNu)/!dtor;

    ExR = crossp(eccVec, r);
    magExR = sqrt(total(ExR*ExR));
    print,"ExR, Ls:", ExR, Ls, ExR/Ls;
    
    EdR = total(eccVec*r)
    print,"Init True anomaly components",magExR, EdR;

    if (total(ExR*Ls) < 0) then begin
       ExR = -ExR
    endif

    tau = atan(magExR, EdR);
    print,magExR, EdR;
    print,"True anomaly:", tau/!dtor

    coeff = sqrt((1-ecc)/(1+ecc))
    print,"Coeff", coeff
    tau2 = tau/2
    tantau2 = tan(tau2)
    print, "tau2,tantau2:", tau2/!dtor, tantau2
    inside = coeff*tantau2
    eccanom = 2*atan(inside)
    print,"inside:",inside
    print,"Eccentric anomaly:", eccanom/!dtor;
    
    print,'Eccanom in rad:', eccanom;
    meananom = eccanom-ecc*sin(eccanom)
    print,'Meananom rad/deg:', meananom,meananom/!dtor

    magR1 = sqrt(total(r1*r1));
    num  = (1+ecc*cos(tau))
    denom =(1-ecc^2)
    print, 'magR1 num, denom:', magR1, num, denom
    a = magR1*num/denom
    print, 'a is:', a

    
    period = 2*!Dpi*sqrt(a^3/G/M)
    print,"Period: ", period

    Espec = -G*M/2/a
    print, "Specific energy:", espec

    vsq = total(v*v);
    print,'V:', v;
    print, "v*v", v*v;
    print, "scalar:", total(v*v)
    print,"Vsq:",vsq
    Epot = -G*m0/sqrt(total(r*r))
    Ekin = 0.5*m0/(m0+m1) * vsq;
    Etot = Epot+Ekin
    print, 'Energies:', Epot,ekin,etot

    Epot2 = -G*m0*M0/(M0+m1)/magR1
    Ekin2 = (M0+M1)/(2*M0)*total(v1*v1)
    Etot2 = Epot2+Ekin2
    print, "Ener2:", epot2, ekin2, etot2

end

