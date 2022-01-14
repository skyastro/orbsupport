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
let endTime   = "2022-01-02";
let t0 = (2459580.50000000 - Constants.JD0)*86400;
let orbit = SolarPlanets.getOrbit(id, t0);
/*
const data = horizon(id, startTime , endTime);
let json  = JSON.parse(data);
let orbit = getOrb(json[0]);
 */
for (let i= -730; i<=730; i += 1) {
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
    Util.log("In Horizon:"+id+" "+start+" "+stop);
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