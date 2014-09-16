package piedpipers.group9;

import java.util.*;

import piedpipers.sim.Point;
import piedpipers.group9.*;


public class Player extends piedpipers.sim.Player {
	static int npipers;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;
	
	
	static Point target = new Point();
	static int[] thetas;
	static boolean finishround = true;
	static boolean initi = false;
	static boolean sweep = false; // false if the pipers have not began sweeping, true if pipers begin sweeping
	static boolean []helper = new boolean[10] ;
	static boolean iniHelper = true;
	
	public void init() {
		thetas = new int[npipers];
		initi=true;
		
		/*for (int i=0; i< npipers; i++) {
			Random random = new Random();
			int theta = random.nextInt(180);
			thetas[i]=theta;
			System.out.println(thetas[i]);
		}*/
		
		
	}

	// Return: the next position
	// my position: dogs[id-1]

	public Point move(Point[] pipers, // positions of dogs
			Point[] rats, boolean[] pipermusic) { // positions of the rats
		
		if (!initi) {
			this.init();
		}
		if(!Global.initGlobal){
			Init.initGlobal(pipers,rats,dimension);
			Init.initGate();
			Init.initLuredRats();
		}
		if(!Global.initThetas){
			thetas=Init.initThetas();
		}
		if(!Global.initAssignedRats){
			Init.initAssignRats();
		}
		if(!Global.initEpsiolon){
			Init.initEpsilonDensity();
		}
		
		PlayUtilities.updateOldRatsAndPipers(id, rats);
		PlayUtilities.updateGlobal(pipers,rats);
		
		
		npipers = pipers.length;
		System.out.println(initi);
		Point gate = new Point(dimension/2, dimension/2);
		if(iniHelper)
		{
		for (int i=0;i<npipers;i++)
		{
			helper[i] = false;
		}
		iniHelper = false;
		}
		Point current = pipers[id]; // Where the pipers are right now
		double ox = 0, oy = 0; // direction of the piper
		//nrats = rats.length;
		
		
		if (PlayUtilities.getSide(current) == 0 && !sweep) { // if on the left side - starting point
			this.music = false;
			Global.isPlaying[id]=this.music;
			
			// this is the change in movement towards the gate
			double dist = PlayUtilities.distance(current, gate);
			assert dist > 0;
			ox = (gate.x - current.x) / dist * pspeed;
			oy = (gate.y - current.y) / dist * pspeed;
			System.out.println("move toward the right side");
		}
		else if (!PlayUtilities.closetoWall(current) && !sweep) { // on the right side and hasn't bounced back yet
			// this is the change in movement based on the angle
			this.music = false;
			Global.isPlaying[id]=this.music;
			ox = pspeed * Math.sin(thetas[id] * Math.PI / 180);
			oy = pspeed * Math.cos(thetas[id] * Math.PI / 180);
		}	
		//I made this false intentionally to test the greedy algo
		//remove the false flag to test sweep algo
		else if(PlayUtilities.density()>Global.epsilon&&false){ // after bounced and turns on music;
			current=sweep(pipers,rats);
			//Point rachelPoint=RachelFunction();
			//Point burakPoint=BurakFuntion();
			 return current;
			
		}else if(pipers[id].x<=Global.GATE.x&&music&&pipers[id].y==Global.GATE.y){
			current=PlayUtilities.movePiperTo(pipers[id], new Point(Global.GATE.x-5,Global.GATE.y));
			return current;
		}else{
			//when density is low
			this.sweep=true;
			//when density is low it will execute this function
			this.music = true; // music turns on once pipers have reached close to wall
			Init.setMusicStatus(this.music);
			//if the density is low
			//move to the closest piper
			if(PlayUtilities.isWithinRadius(current,rats))
			{				
				this.music = false;
				Init.setMusicStatus(this.music);
				if(PlayUtilities.isPipersWithinRadius(pipers,current)>=0)
				{
					helper[id]=true;
					helper[PlayUtilities.isPipersWithinRadius(pipers,current)]=false;
					//ran away and become a helper!
				}
			}
			

			current=PlayUtilities.greedySearch(pipers,rats,id);
			//current=PlayUtilities.movePiperTo(current, Global.GATE);
			PlayUtilities.updateLuredRats(rats, pipers);
			//helperSearch(helper[],rats,pipers,id) how to change music status in PlayUtilities?
			if(helper[id])
			{			
				this.music = false;
				Init.setMusicStatus(this.music);
				current=PlayUtilities.greedySearch(pipers,rats,id);
				Point rat=PlayUtilities.getClosestRatNotLuredInTheRegion(pipers, rats, id);
				//current=PlayUtilities.movePiperTo(current,rat);
				if(PlayUtilities.distance(rat,current)<10)
				{
					helper[id]=false;
					this.music = false;
					Init.setMusicStatus(this.music);
				}
				return current;
			}
			if(PlayUtilities.unLuredRatsNumber(rats)<100 && helper[id])
			{
				//current = PlayUtilities.movePiperTo(current,PlayUtilities.getClosestPiper(pipers,current));
				Point closestPiper = PlayUtilities.getClosestPiper(pipers,current);
				current = PlayUtilities.movePiperTo(current,closestPiper);
				return current;
			}
		/*	if(PlayUtilities.unLuredRatsNumber(rats)==1 )
			{
			for(int i = 0;i<npipers;i++)
			{
			if(helper[i])
			{
				current=PlayUtilities.toGate(current);
			}
			}
			}*/
			return current;
			  
		}
		
		current.x += ox;
		current.y += oy;
		
		PlayUtilities.updateLuredRats(rats, pipers);
		return current;
	}
		

	
	public Point sweep(Point []pipers,Point []rats){
		this.music = true; 
		Init.setMusicStatus(this.music);
		Point current = pipers[id]; // Where the pipers are right now
		double ox = 0, oy = 0; // direction of the piper
				if(!this.sweep){
			this.sweep = true; // sweeping begins once the pipers have reached close to wall
			assignRats(rats);
		}
		
		ArrayList<Point> missingRats = ratsToCollect(pipers, rats); // rats that have to get collected
		System.out.println("number of missing rats = " + missingRats.size());
		if(missingRats.size() > 0){
			/*
			String print = "rats to collect are: ";
			for(int i = 0; i < missingRats.size(); i++)
				print += missingRats.get(i) + ", ";
			System.out.println(print);
			*/
			
			System.out.println("hasResponsibility for id=" + id + ":" + hasResponsibility(Global.assignedRats.get(id), missingRats));
			if(hasResponsibility(Global.assignedRats.get(id), missingRats)){
				current = toRat(current, missingRats);
				
			}
			else
				current = toGate(current);
			
			if(missingRats.size() == 1){
				for(int i = 0; i < npipers; i++){
					if(isResponsibility(Global.assignedRats.get(i), missingRats.get(0)))
						System.out.println("the THE MISSING RAT IS PIPER:" + i+ "'S RESPONSIBILITY.");
				}
			}
			
		}
		else{
			
			
			
			current = toGate(current);
			
		}
		
	
		current.x += ox;
		current.y += oy;
	return current;
		
	}
	
	public Point toRat(Point current, ArrayList<Point> missingRats){
		System.out.println("IN TO RAT");
		double ox = 0, oy = 0;
		Point gate = new Point(dimension/2, dimension/2);
		Point rat = furthestRat(gate, missingRats);
		
		// if both the x and y of the piper are farther than 1 from the rat, change both
		if(Math.abs(rat.x - current.x) > 1 && Math.abs(rat.y - current.y) > 1 ){
			double dist = PlayUtilities.distance(current, rat);
			assert dist > 0;
			
			ox = (rat.x - current.x) / dist * mpspeed;
			oy = (rat.y - current.y) / dist * mpspeed;
		}
		// if the x of the piper is farther than 1 from the rat, change x
		else if(Math.abs(rat.x - current.x) > 1){
			double dist = PlayUtilities.distance(current, rat);
			assert dist > 0;
			
			ox = (rat.x - current.x) / dist * mpspeed;
		}
		// if the y of the piper is farther than 1 from the rat, change y
		else{ // if  (Math.abs(rat.y - current.y) > 1)
			double dist = PlayUtilities.distance(current, rat);
			assert dist > 0;
			
			oy = (rat.y - current.y) / dist * mpspeed;
		}
		
		Point next = new Point(current.x + ox, current.y + oy);
		return next;	
	}
	
	public Point furthestRat(Point gate, ArrayList<Point> missingRats){
		int ratID = 0;
		ArrayList<Point> currentRats = Global.assignedRats.get(id);
		for(int i = 0; i < missingRats.size(); i++){
			if(isResponsibility(currentRats, missingRats.get(i)) && PlayUtilities.distance(gate, missingRats.get(i)) > PlayUtilities.distance(gate, missingRats.get(ratID)))
				ratID = i;
		}
		
		return missingRats.get(ratID);
	}
	
	public boolean hasResponsibility(ArrayList<Point> currentRats, ArrayList<Point> missingRats){
		System.out.println("in hasResponsibility()");
		for(int i = 0; i < currentRats.size(); i++){
			for(int j = 0; j < missingRats.size(); j++){
				if(currentRats.get(i).equals(missingRats.get(j)))
					return true;
			}
		}
		return false;
	}
	
	public boolean isResponsibility(ArrayList<Point> currentRats, Point rat){
		for(int i = 0; i < currentRats.size(); i++){
			if(currentRats.get(i).equals(rat))
				return true;
		}
		return false;
	}
	
	public Point toGate(Point current){
		double ox = 0, oy = 0;
		Point gate = new Point(dimension/2, dimension/2);
		// if both the x and y of the piper are farther than 1 from the gate, change both
		// !finishround signifies that the piper is not ready to move towards the target yet
		if(Math.abs(gate.x - current.x) > 1 && Math.abs(gate.y - current.y) > 1 && !finishround){
			double dist = PlayUtilities.distance(current, gate);
			assert dist > 0;
			
			ox = (gate.x - current.x) / dist * mpspeed;
			oy = (gate.y - current.y) / dist * mpspeed;
		}
		// if the x of the piper is farther than 1 from the gate, change x
		else if(Math.abs(gate.x - current.x) > 1  && !finishround){
			double dist = PlayUtilities.distance(current, gate);
			assert dist > 0;
			
			ox = (gate.x - current.x) / dist * mpspeed;
		}
		// if the y of the piper is farther than 1 from the gate, change y
		else if(Math.abs(gate.y - current.y) > 1  && !finishround){
			double dist = PlayUtilities.distance(current, gate);
			assert dist > 0;
			
			oy = (gate.y - current.y) / dist * mpspeed;
		}
		// now the piper is close to the gate, so it can move towards the target
		else if(Math.abs(target.x - current.x) > 1){
			finishround = true; // change finishround to false to show that the piper has finished sweeping its region and can move to the left side
			double dist = PlayUtilities.distance(current, target);
			assert dist > 0;
			
			ox = (target.x - current.x) / dist * mpspeed;
		}
		
		Point next = new Point(current.x + ox, current.y + oy);
		return next;
			
	}
	
	// assigns the rats to all of the pipers based on region
	public void assignRats(Point[] rats) {
		for(int j = 0; j < rats.length; j++){
			int responsiblePiper = getPiperResponsibilityID(rats[j]);
			Global.assignedRats.get(responsiblePiper).add(rats[j]);
		}
	}
	
	// returns the ID of the piper who has the responsibility for the rat
	public int getPiperResponsibilityID(Point ratLoc){
		double ratDegree = Math.toDegrees(Math.atan( (ratLoc.x - dimension/2)/(dimension/2 - ratLoc.y))) + 90;
		int piperID = 0;
		for (int i=0; i< npipers; i++) {
			if(ratDegree >= thetas[i] - (180 / (2 * npipers)) && ratDegree < thetas[i] + (180 / (2 * npipers))){
				piperID = i;
				return piperID;
			}
		}
		return piperID;
	}
	
	public ArrayList<Point> ratsToCollect(Point[] pipers, Point[] rats){
		ArrayList<Point> ratsToCollect = new ArrayList<Point>();
		for(int i = 0; i < rats.length; i++){
			Point closestPiper = getClosestMusicPiper(pipers, rats, i);
			double dist = PlayUtilities.distance(rats[i], closestPiper);
			if(dist > 10) ratsToCollect.add(rats[i]);
		}
	
		return ratsToCollect;
	}
	
	
	boolean allRatsCollected(Point[] pipers, Point[] rats){
		if(ratsToCollect(pipers, rats).size() == 0)	
			return true;
		return false;
	}
	
	
	public Point getClosestMusicPiper(Point[] pipers, Point[] rats, int ratId) {
		int minpiper = -1;
		double mindist = Double.MAX_VALUE;
		for (int i = 0; i < Global.npipers; ++i) {
			if (this.music) {
				double d = PlayUtilities.distance(rats[ratId], pipers[i]);
				if (d < mindist && d > 10) { // ignore overlapping pipers?
					mindist = d;
					minpiper = i;
				}
			}
		}
		return pipers[minpiper];
	}

	
}
