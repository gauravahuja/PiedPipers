package piedpipers.group9;

import java.util.ArrayList;

import java.util.Map;

import piedpipers.sim.Point;

/*
 * Place all global variables are here
 * 
 * */

public  class Global{
	public static boolean initGlobal=false;
	public static boolean initThetas=false;
	public static boolean initEpsiolon=false;
	public static int npipers;
	public static int dimension;
	public static int nrats;
	//make sure move at Play is called before you utilize rats and pipers.
	public static Point []rats;
	public static Point []pipers;
	
	public static int DEFAULT_PIPERS = 1;
	static int DEFAULT_RATS = 10;
	static boolean initAssignedRats=false;
	static ArrayList<ArrayList<Point>> assignedRats;
	
	//enter rat as a key value
	public static Map<Point,Point> ratIsAssignedGreedy;
	
	static double WALK_DIST = 10.0; // <10m, rats walk with music piper
	static double STOP_DIST = 2.0; // <2m, rats stop

	static double WALK_SPEED = 1; // 1m/s, walking speed for rats
	static double MUSICPIPER_SPEED = 1; // 1m/s, walking speed for music piper
	static double PIPER_SPEED = 5; // 5m/s, walking speed for no music piper

	public static double TIMEUNIT = 0.1;
	static double OPEN_LEFT=Global.dimension/2+1; // left side of center opening
	static double OPEN_RIGHT=Global.dimension/2-1; // right side of center opening
	public static Point GATE=null;
	public static final double SMALLDISTANCE = 0.001;
	public static double epsilon=0.015;
	//public abstract void initGlobal(Point[] pipers, Point[] rats, int dimension);
	//public abstract void updateGlobal(Point[] pipers, Point[] rats);
	//public abstract void initGlobal(Point[] pipers, Point[] rats, int dimension);
	public static boolean initAssignRatsGreedy=false;
	
	public static boolean []isRatLured=null;
	public static Point[] oldRats=null;
	public static Point[] oldPipers=null;
	public static boolean [] isPlaying=null;
	public static int [] thetas;
	
	
	public static boolean musicStatusCurrentPlayer=false;
	
}
