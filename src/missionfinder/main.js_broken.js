import Constants from "../std/Constants.js";
import Util      from "../std/Util.js";
import Dump      from "../std/Dump.js";
import Orbit        from "../orbits/Orbit.js";
import SolarPlanets from "../solarsystem/SolarPlanets.js";
import ssData    from "../data/PlanetaryData.js";

import {execSync}   from "child_process";


let argv = process.argv;

Util.log("Process is:"+process);
Util.log("argv:"+argv);

let missionID = -31;
if (argv.length > 2) {
    missionID = argv[2];
} 
let startTime = "1977-09-27";
let endTime   = "2000-01-01";
if (argv.length > 3) {
    startTime = argv[3];
}
if (argv.length > 4) {
    endTime = argv[4];
}

const data = horizon(missionID, startTime, endTime);
let   json = JSON.parse(data);
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
let pos;
let useOrbits = [];
let orbE = SolarPlanets.getOrbit("Earth", 2.e8);
let orbV = SolarPlanets.getOrbit("Venus", 2.e8);
let orbM = SolarPlanets.getOrbit("Mercury", 2.e8);

processInput(json);


function horizon(id, start, stop, center) {
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

function processInput(json) {
    
    let prior  = json[0];
    pos    = [];
    pos[0]     = getOrb(prior);
    let rPrior = Util.mag(pos[0].position(prior.t));
    let nDelts = 0;  
    
    let tstart = prior.t;
    let n      = 1;
    let periods = [];
    let accel = false;
    let ravg  = 0;
    let iStart = 0;
    
    for (let i=1; i<json.length; i += 1) {
        let curr  = json[i];
        pos[i]    = getOrb(curr);
        if (pos[i] == null) {
            Util.log("Null pos at:"+i);
        }
        let rvec = pos[i].position(curr.t);
        let rCurr = Util.mag(rvec);
        let rE = orbE.position(curr.t);
        let rV = orbV.position(curr.t);
        let rM = orbM.position(curr.t);
        let deltaE = Util.mag(Util.diff(rvec, rE));
        let deltaV = Util.mag(Util.diff(rvec, rV));
        let deltaM = Util.mag(Util.diff(rvec, rM));
        let de    = Math.abs(prior.Eccen-curr.Eccen);
        let da    = 2*Math.abs(prior.a-curr.a)/(Math.abs(prior.a)+Math.abs(curr.a));
//        Util.log("Look:",i,de,da);
//        Util.log("  ", rCurr, rvec);
//        Util.log("  ",deltaE,deltaV, deltaM);
        
        if (de > 0.01 || da > 0.02) {
            
            if (!accel) {
                nDelts = 0;
                ravg = 0;
            }
            
            
            accel = true;
            // We are in a transition period of some kind.
            if (nDelts == 0) {
                // We are starting the period.
                if (i !== 1) {
                    periods.push({mode: "coast", start: iStart, stop:i-1});
                }
                iStart = i-1;
                tstart = prior.t;
            }
            ravg   += rCurr;
            nDelts += 1;
            
        } else if (accel) { // Acceleration has stopped.
            accel = false;
            if (nDelts == 1) { // Course correction?
                periods.push({mode:"manuever", start:iStart, stop: i-1});
                iStart = i-1;
            } else {
                ravg /= nDelts;
                periods.push({mode:"encounter", start: iStart, stop: i-1, radius: ravg});
                iStart = i-1;
            }
        }
        prior  = curr;
        rPrior = rCurr;
    }
    
    if (accel) {
        if (nDelts == 1) {            
            periods.push({mode: "manuever", start: iStart, stop: json.length-1});            
        } else {
            ravg /= nDelts;
            periods.push({mode: "encounter", start: iStart, stop: json.length-1, radius: ravg});            
        }
    } else {
        periods.push({mode:"coast", start: iStart, stop: json.length-1});
    }
    
    Util.log("Preliminary analysis gives following periods:");
    for (let i=0; i<periods.length; i += 1) {
        let per = periods[i];
        Util.log(i,per.mode,per.start,per.stop);
        if (per.mode=="encounter") {
            Util.log("   Radius is:"+per.radius);
        }
        
        if (per.mode == "encounter") {
            for (let z in planets ) {
                let delta = Math.abs(planets[z].au-per.radius);
                if (delta/per.radius < 0.2) {
                    Util.log("   Encounter with:",z);
                    per.mode = z;
                }
            }
            if (per.mode == "encounter") {
                Util.log("Unable to identify encounter..");
                per.mode = "coast";
            }
        }
        if (!per.radius) {
            per.radius = "";
        }
        let prev = null;
        if (i > 0) {
            prev = periods[i-1];
        }
        let next = null;
        if (i < periods.length-1) {
            next = periods[i+1];
        }
        processPeriod(per, prev, next, i == 0, i == periods.length-1);
    }
    
    Util.log(' "TBD": {');
    Util.log(' "color":', 0xFFFF00,",");    
    Util.log(' "horizonsID":',missionID,",");
    Util.log(' "orbits":[');    
    let last = useOrbits.length-1;
    tstart   = useOrbits[0].start;
    for (let i=0; i<useOrbits.length; i += 1) {
        const orb = useOrbits[i];
        const type = orb.type;
        if (i == last && type == "destination") {
            break;
        }
        Util.log("   {");
        Util.log('    "start":', tstart, ",");
        if (type == "coast") {
            if (i < last && useOrbits[i+1].type == "manuever") {
                Util.log('    "stop":', useOrbits[i+1].stop, ",");
                Util.log('    "interp":', orb.stop, ",");
                tstart = useOrbits[i+1].stop;
                i += 1; // skip the manuever entry.
            } else if (i < last) {                
                Util.log('    "stop":',orb.stop + 86400, ",");
                Util.log('    "interp":',orb.stop - 86400, ",");
                tstart = orb.stop + 86400;
            } else {
                Util.log('    "stop":',1.e12,",");
            }
            writeElems("sat", orb.orbit, "");
        } else if (type == "manuever") {
            Util.log("Got unexpected manuever");
        } else if (type == "encounter") {
            Util.log('    "stop":',orb.stop + 86400,",");
            tstart = orb.stop+86400;
            Util.log('    "interp":', orb.stop - 86400,",");
            Util.log('    "planet":"'+orb.planet+'",');
            writeElems("sat", orb.orbit, ",");
            writeElems("planetOrb", orb.planetOrb, "");
        }
        let comma = ",";
        if (i == last  || i == last-1 && useOrbits[last].type == 'destination') {
            comma = "";
        }
        
        Util.log("   }"+comma);

    }
    Util.log(" ]")
}
function writeElems(field, orbit, comma) {
    Util.log('    "'+field+'":{');
    Util.log('      "e":',    orbit.e+",");
    Util.log('      "a":',    orbit.a+",");
    Util.log('      "per":',  orbit.per+",");
    Util.log('      "t0":',   orbit.t0+",");
    Util.log('      "rot":[', orbit.rot,"],");
    Util.log('    }'+comma);
}


function processPeriod(per, prev, next, first, last) {
    if (per.mode == "coast") {
        processCoast(per);
        
    } else if (per.mode == "manuever") {
        // This is a usually small transition in orbits over a two day period.
        // We need to interpolate between the earlier and subsequenct coasts.
        // So this affects when we interpolate the last and next coasts.
        useOrbits.push({type:"manuever", start: json[per.start].t, stop: json[per.stop].t});
    } else {
        if (!last) { 
            showEncounter(per, prev, next, first);
        } else {
            useOrbits.push({type: "destination"})
        }
    }
}

function processCoast(per) {
    let tol   = 0.003;
    let nStep = per.stop-per.start;
    // get the orbit at the middle of the period and see if it will work.
    if (nStep == 1) {
        let oTest = pos[per.start];
        useOrbits.push({type: "coast", orbit: oTest, start: json[per.start].t, stop: json[per.stop].t});
        return;
    }
    let test  = per.start+Math.floor(nStep/2);
    let oTest = pos[test];
    let valid = true;
    for (let i=per.start; i<=per.stop; i += 1) {
        let t = json[i].t;
        let rAct  = pos[i].position(t);  // Get the position using the then current osculating elements.
        let rTest = oTest.position(t);
        let delta = Util.mag(Util.diff(rAct, rTest));
        if (delta > tol) {
            let per0 = {mode: "coast", start: per.start, stop: test};            
            let per1 = {mode: "coast", start: test, stop:per.stop};
            processCoast(per0);
            processCoast(per1);
            valid = false;
            break;
        }
    }
    if (valid) {
        useOrbits.push({type: "coast", orbit: oTest, start: json[per.start].t, stop: json[per.stop].t});
    }
}

function showEncounter(per, prev, next, first) {
    // Get the planet orbit for the middle of the encounter interval,
    // unless this is at the beginning of the mission when
    // presumably Earth was closest at the beginning of the interval.
    let tstart = json[per.start].t;
    let tend   = json[per.stop].t;
    
    let tPlanet = tstart;
    if (!first) {
        tPlanet = Math.floor((tstart+tend)/2);        
    }
    
    // To get a continuous function we'll use the Horizons orbit
    // for the planet at the time.
    let tJD    = timeJD(tPlanet);
    let eJD    = timeJD(tPlanet+1);
    let pid    = planets[per.mode].id;
    
    let jdStart = timeJD(tstart);
    let jdEnd   = timeJD(tend);

    
    let pEphem = horizon(pid, jdStart, jdEnd, 0);
    let pElem  = JSON.parse(pEphem);
    
    
    let data = horizon(missionID, jdStart, jdEnd, pid);
    let encJson  = JSON.parse(data);
    
    let test = 0;
    if (!first) {
        test = Math.floor((per.stop-per.start)/2);
    }
    let planetOrb = getOrb(pElem[test]);
    
    let telem   = encJson[test];    
    let testOrb = getOrb(telem);
    
    let fits = true;
    for (let i=per.start; i<=per.stop; i += 1) {
        // In the processing below we are assuming that the
        // encounter has an initial period of nonfitting, a period of fitting near
        // the time of the test orbit, and a period of nonfitting after the test orbit.
        // If it's more complex this may not work.
        let beginning = (i-per.start < test); 

        let t       = json[i].t;
        let rAct    = pos[i].position(t);
        if (pos[i].e > 1) {
            rAct    = Util.times(-1, rAct);
        }
        let rPlanet = planetOrb.position(t);
        let rSat    = testOrb.position(t);
        if (telem.Eccen > 1) {
            rSat = Util.times(-1, rSat);
        }        
        let sum     = Util.vadd(rPlanet, rSat);        
        let mag     = Util.mag(Util.diff(rAct,sum));
        
        if (mag > 0.01) {
            // We're in a bad patch at the beginning of the encounter.
            // Just note that and we'll handle it when we start fitting.
            if (beginning) {
                fits = false;
            } else {
                // Has to be a next since we don't precess terminal encounters.
                // Make this part of the next coasting interval...
                // Since we'll split that down this is perfectly OK.
                next.start = i;
                // Now put whatever was successful as an interval.
                useOrbits.push({type:"encounter",planet: per.mode, start: tstart, stop: json[i].t, orbit: testOrb, planetOrb: planetOrb});
                Util.log(per.mode, "terminated encounter early.");
                return;  // Finished with this encounter.

            }
        } else {
            if (beginning && !fits) {
                // This interval fitted, but pervious intervals did not.
                // We'll treat them as belonging to the previous group.
                per.start = i;
                tstart = json[per.start].t;
                fits = true;
                useOrbits[useOrbits.length-1].stop = tstart;
            }
        }
    }
    Util.log(per.mode, "sucessful encounter");
    useOrbits.push({type:"encounter",planet: per.mode, start: tstart, stop: tend, orbit: testOrb, planetOrb: planetOrb});
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