function radius, ta, a, ecc
    return, a*(1-ecc^2)/(1+ecc*cos(ta))
end

function rotmat, omega, i, nu
    so = sin(omega)
    co = cos(omega)
    si = sin(i)
    ci = cos(i)
    sn = sin(nu)
    cn = cos(nu)
    
    r1 = [co*cn-so*sn*ci, -co*sn-so*cn*ci, so*si]
    r2 = [so*cn+co*sn*ci, -so*sn+co*cn*ci, -co*si]
    r3 = [sn*si, cn*si, ci]
    mat = [[r1],[r2],[r3]]
    help,mat
    return, mat
end

function posvec, ta, omega, i, nu
    print,ta,omega,i,nu
    sum = nu+ta;
    x = cos(omega)*cos(sum)-sin(omega)*sin(sum)*cos(i)
    y = sin(omega)*cos(sum)+cos(omega)*sin(sum)*cos(i)
    z = sin(sum)*sin(i)
    print, "norm:",sqrt(x*x+y*y+z*z);

    return,[x,y,z]
end

function position,ta, a, ecc
    r = radius(ta, a, ecc);
    return, [r*cos(ta), r*sin(ta), 0]
end

function velocity, ta, a, ecc, GM
    r     = radius(ta, a, ecc);
    dtadt = sqrt(GM*a*(1-ecc^2))/r^2
    drdt  = a*ecc*(1-ecc^2)*sin(ta)/(1+ecc*cos(ta))^2
    return, dtadt*[drdt*cos(ta)-r*sin(ta), drdt*sin(ta)+r*cos(ta), 0]
end
   
pro checkformula2
    print,"T0 in days:", (2451849.447384673171d0-2451544.500000000d0),format='(a,f30.15)'
    t0 = 86400.d0*(2451849.447384673171d0-2451544.500000000d0)
    print,"T0 in seconds:", t0
    AinAU = 2.644734941760980d0;
    AU = 149.597870700d9
    a = AinAU*AU
    print, "A in AU and meters:", AinAU, a
    GMau = 2.9630927493457475D-04;
    GM   = GMau*AU^3/86400.d0^2
    print, "GMau, GM:", GMau, GM
    G = 6.6743d-11
    M = GM/G
    print, "G, M:", G, M
    period = 2*!dpi * sqrt(a^3/GM)
    print, "Period:", period, period/86400
    mar356 = 2435535.5
    jan100 = 2451544.5
    deltad = mar356-jan100
    print,'Difference in days:", deltad
    dt = deltad*86400
    print,"Mar 3,1956 is:", deltad, dt, format='(a,2g25.15)';
    ma0 = 290.072671
    ma = ma0 + dt/period * 360
    print, "Ma init is:", ma
    ma = ma mod 360
    print, 'mamod:', ma
    if (ma < 0) then begin
        ma += 360
    endif
    print, "Ma final is:", ma
    ecc = 0.1555906714443290
    ma0    = ma0*!dtor;
    ea     = ma0
    mat    = ea - ecc*sin(ea)
    deadma = 1  - ecc*cos(ea)
    offset = ma0-mat
    for i=0,10 do begin
        print, ma0, ea, mat, sin(ea), ecc*sin(ea), offset
        print, "Iter ", i, ea/!dtor,format='(a, i4, g25.15)'
        if (offset eq 0) then break;
        ea     = ea + offset/deadma
        mat    = ea - ecc*sin(ea)
        deadma = 1  - ecc*cos(ea)
        offset = ma0-mat
    endfor
    ea2000 = ea

    ma     = ma*!dtor
    ea     = ma
    mat    = ea - ecc*sin(ea)
    deadma = 1  - ecc*cos(ea)
    offset = ma-mat
    print, 'Starting 1956'
    for i=0,30 do begin
        print, i, ecc, ma, ea, mat, sin(ea), ecc*sin(ea),offset
        print, ea/!dtor,format='(g25.15)'
        if (offset eq 0) then break;
        ea     = ea + offset/deadma
        mat    = ea - ecc*sin(ea)
        deadma = 1  - ecc*cos(ea)
        offset = ma-mat
    endfor
    ea1956 = ea

    coeff = sqrt((1+ecc)/(1-ecc));
    tan2000 = tan(ea2000/2);
    tan1956 = tan(ea1956/2);
    ta2000 = 2*atan(coeff*tan2000)
    ta1956 = 2*atan(coeff*tan1956);
    if (ta2000 < 0) then begin
        ta2000 += 2*!dpi
    endif
    if (ta1956 < 0) then begin
        ta1956 += 2*!dpi
    endif

    print,"TAs:", ta2000/!dtor, ta1956/!dtor,format='(a,2g25.15)'
    r2000 = position(ta2000, a, ecc)
    v2000 = velocity(ta2000, a, ecc, GM)

    r1956 = position(ta1956, a, ecc)
    v1956 = velocity(ta1956, a, ecc, GM)
    print,"Position/Velocity 2000:", r2000, v2000
    print,"Position/Velocity 1956:", r1956, v1956

    print, "Radius in au:", sqrt(total(r2000*r2000))/au
    omega =  70.45508495808998d0*!dtor
    nu    = 350.8486510213602d0*!dtor
    inc   = 13.72432149577838d0*!dtor
    mat   = rotmat(omega, inc, nu);

    fac =posvec(ta2000, omega, inc, nu);
    rmag = sqrt(total(r2000*r2000))
    print, " 2000 Fac:", fac
    print, " 2000 Pos in au:", rmag*fac/au

    
    print, "  2000 Using matrix:", mat##r2000/au
    print, "  v2000 Using matrix:m/s  ", mat##v2000
    print, "  v2000 Using matrix:au/d ", mat##v2000*86400/au
 
    fac = posvec(ta1956, omega, inc, nu);

    rmag = sqrt(total(r1956*r1956))
    print, " 1956 Fac:", fac
    print, " 1956 Pos in au:", rmag*fac/au

    
end
