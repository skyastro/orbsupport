import Constants    from "../std/Constants.js";
import Util         from "../std/Util.js";
import Dump         from "../std/Dump.js";
import Orbit        from "../orbits/Orbit.js";
import SolarPlanets from "../solarsystem/SolarPlanets.js";
import ssData       from "../data/PlanetaryData.js";

import {execSync}   from "child_process";


let argv = process.argv;

Util.log("Processy is:"+process);
Util.log("argv:"+argv);

let id = 299;
if (argv.length > 2) {
    id = argv[2];
} 
let startTime = "2022-01-01";
let endTime   = "2022-03-31";

const data = horizon(id, startTime , endTime);
let json  = JSON.parse(data);
//Util.log("JSON length: "+json.length);
let n = json.length;
let tact = (json[0].t + json[n-1].t)/2
let aavg = 0;
let eavg = 0;
let iavg = 0;
let omegaavg = 0;
let nuavg=0;
let Mavg = 0;
let maavg = 0;
let orb;
for (let i=0; i<n; i += 1) {
    orb = json[i];
    aavg += orb.a;
    eavg += orb.Eccen;
    iavg += orb.i;
    omegaavg += orb.Omega;
    nuavg += orb.nu;
    Mavg += orb.M;
    let ma = orb.MA + (tact-orb.t)/86400 * orb.N0 * 2*Math.PI;
    if (ma > 2*Math.PI) {
        ma -= 2*Math.PI;        
    }
    if (ma < 0) {
        ma += 2*Math.PI;
    }
    maavg += ma;
//    Util.log("tact, t, ma, orb.MA:", tact, orb.t, ma, orb.MA);
}
orb = json[0];
orb.t = tact;
orb.Eccen = eavg/n;
orb.a     = aavg/n;
orb.MA    = maavg/n;
orb.i     = iavg/n;
orb.Omega = omegaavg/n;
orb.nu    = nuavg/n;
orb.M     = Mavg/n;
let orbit = getOrb(orb);
for (let i= -730; i<=730; i += 1) {
//for (let i= -1; i<=1; i += 1) {
    let day = 2459580.50000000 + 50*i;
    let t = (day-Constants.JD0)*86400;
    let pos = orbit.position(t);
    let au  = Constants.AU;
    console.log("%f %f %f %f %f", t, day, pos[0]*au, pos[1]*au, pos[2]*au );
}
const planets = {
    Mercury: {au:  0.39,  id: 199},
    Venus:   {au:  0.723, id: 299},
    Earth:   {au:  1,     id: 399},
    Mars:    {au:  1.524, id: 499},
    Jupiter: {au:  5.203, id: 5},
    Saturn:  {au:  9.539, id: 6},    
    Uranus:  {au: 19.2,   id: 7},
    Neptune: {au: 30.1,   id: 8},
    Pluto:   {au: 39.5,   id: 9}
};


function horizon(id, start, stop, center) {
    Util.log("In Horizons:"+id+" "+start+" "+stop);
    const  HorizonJar = "../../../HorizonsQuery/dist/HorizonsQuery.jar";
    if (center != null) {
        center = " "+center;
    } else {
        center = "";
    }
    // space before optional center already added.
    let cmd = "java -jar "+HorizonJar+" "+id+" "+start+" "+stop+center;
    Util.log("Horizon command is:"+cmd);
    return execSync(cmd);
}

function timeJD(time) {
    return "JD"+((time/86400)+Constants.JD0);
}

function getOrb(params) {
    let orb = new Orbit();
    orb.addStructure(params);
    let rp = orb.getParameter("r", true);
    if (rp == null) {
        Util.log("rp is null");
        process.exit(-1);
    }
    return orb.quickOrbit();    
}