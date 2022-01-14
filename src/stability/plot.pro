pro zzplot, oscFile, meanFile
    print, "Starting\n";
    openr, oscLun, oscFile, /get_lun
    openr, meanLun, meanFile, /get_lun
    print, "Opended"
    t    = fltarr(10000);
    osc  = fltarr(10000);
    mean = fltarr(10000);
 
    i = 0;
    print,"Before loop:", i
    tt = 0.;
    o  = 0.;
    m  = 0.;
    while (not eof(oscLun) and not eof(meanLun)) do begin
        readf, oscLun, tt, x, o
        readf, meanLun, x, y, m;
        t[i] = tt;
        osc[i] = o;
        mean[i] = m;
        i += 1;
    endwhile
    print, "Truncating";
    t    = t[0:i-1]
    osc  = smooth(osc[0:i-1], 11); 

    mean = smooth(mean[0:i-1], 11);
    print,"Plotting"
    plot, t, osc
    oplot, t, mean
end
