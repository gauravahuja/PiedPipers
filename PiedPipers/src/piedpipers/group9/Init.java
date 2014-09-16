package piedpipers.group9;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import piedpipers.sim.Point;

/*
 * Place all Init methods here
 * 
 * */

public  class Init{
	
	
	public static int[] initThetas() {
		// TODO Auto-generated method stub
		int [] thetas=new int[Global.npipers];
		int theta=0;
		
		thetas = new int[Global.npipers];
		for (int i=0; i< Global.npipers; i++) {
			theta = (180 * i)/ (Global.npipers) + (180)/ (2 * Global.npipers);
			thetas[i]=theta;
			System.out.println("THETAS EQUAL : " + thetas[i]);
		}
		//different thetas
		theta=0;
		Global.initThetas=true;
		int j=Global.npipers-2;
		for(int i=0;i<Global.npipers-1;i++){
			theta+=180.0/Global.npipers;
			Global.thetas[j]=theta;
			j--;
			
		}
		return thetas;
	}

	//initialize Gloabl variables to give access to other classes
	public static void initGlobal(Point[] pipers, Point[] rats,int dimension) {
		Global.pipers=pipers;
		Global.rats=rats;
		Global.npipers=pipers.length;
		Global.nrats=rats.length;
		Global.dimension=dimension;
		Global.ratIsAssignedGreedy=new HashMap();
		initOldRatsAndPipers(rats, pipers);
		Global.isPlaying=new boolean[Global.npipers];
		Arrays.fill(Global.isPlaying,false);
		Global.thetas=new int[Global.npipers-1];
		Global.initGlobal=true;
	}

	public static void initAssignRats() {
		// TODO Auto-generated method stub
		Global.assignedRats = new ArrayList<ArrayList<Point>>();
		for(int i = 0; i < Global.npipers; i++){
			Global.assignedRats.add(new ArrayList<Point>());
		}
		
	}
	
	public static void initEpsilonDensity(){
		//when remaing rats less than 10 percent of the initial rats
		Global.epsilon=(double)Global.nrats/(Global.dimension*Global.dimension)/10;
		Global.initEpsiolon=true;
	}


	

	public static void setMusicStatus(boolean music){
		Global.musicStatusCurrentPlayer=music;
	}
	
	public static void initGate(){
		Global.GATE=new Point(Global.dimension/2,Global.dimension/2);
	}

	public static void initLuredRats() {
		// TODO Auto-generated method stub
		Global.isRatLured=new boolean[Global.nrats];
		Arrays.fill(Global.isRatLured, false);
		
	}
	public static void initOldRatsAndPipers(Point [] rats,Point[]pipers){
		Global.oldPipers=new Point[rats.length];
		Global.oldRats=new Point[pipers.length];
	}
}
