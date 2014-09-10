package piedpipers.quadSweep;

import java.util.*;

import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
	static double WALK_DIST = 10.0; // <10m, rats walk with music piper

	static int npipers;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;
        static double speed;

	
	static Point target = new Point();
	static int[] thetas;
	static boolean droppedOff = true;
	static boolean initi = false;

        //Represent y-coord of bottom and top of quadrant
        static double quadBottom, quadTop;
	
	public void init() {
		thetas = new int[npipers];
	}

	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	// Return: the next position
	// my position: pipers[id]

	public Point move(Point[] pipers, // positions of pipers
			Point[] rats) { // positions of the rats
		npipers = pipers.length;
		Point gate = new Point(dimension/2, dimension/2);
		if (!initi) {
                  //init() is called by PiedPipers.java before npipers is set,
                  //so I have to set quadTop and quadBottom here.
                  quadTop = dimension / npipers * (id+1);
                  quadBottom = dimension / npipers * id;
                  this.init();initi = true;
		}
		Point current = pipers[id];
		double ox = 0, oy = 0;
                //Go to fence from left side of field
		if (getSide(current) == 0 && droppedOff) {
                  this.music = false;
                  target = gate;
                  System.out.println("move toward the right side");
		} 
                //Go to middle of quadrant on right side of field
                else if (!closetoWall(current) && droppedOff) {
                  target = new Point(dimension,(quadTop+quadBottom)/2);
                  System.out.println("moving to quadrant");
		}
                //Sweep quadrant
                else if (withinQuadrant(current) && freeRatsInQuadrant(rats,current)){
                  droppedOff = false;
                  this.music = true;
                  target = getAverageRatCoord(rats);
                  System.out.println("sweeping for rats");
                }
                //Head back towards gate
		else if (!droppedOff && getSide(current) != 0) {
                  target = gate;
                  System.out.println("move toward the left side");
		}
                //Move onto left side to drop off rats
                else {
                  target = new Point(gate.x - WALK_DIST, gate.y);
                  if (distance(target,current) < mpspeed){
                    this.music = false;
                    droppedOff = true;
                  }
                  System.out.println("dropping off rats");
                }

                //Set speed of piper based on music playing
                if (this.music)
                  speed = mpspeed;
                else
                  speed = pspeed;

                //Move towards the target at fastest speed
                double dist = distance (current,target);
                assert dist > 0;
                ox = (target.x - current.x) / dist * speed;
                oy = (target.y - current.y) / dist * speed;
		
		current.x += ox;
		current.y += oy;
		return current;
	}
        //Get average of x and y coord for all rats in our quadrant
        Point getAverageRatCoord(Point[] rats){
          double xsum = 0.0;
          double ysum = 0.0;
          int ratcount = 0;
          for (Point rat : rats){
            if (withinQuadrant(rat)){
              xsum += rat.x;
              ysum += rat.y;
              ratcount++;
            }
          }
          return new Point(xsum/ratcount, ysum/ratcount);

        }
        //Check if a rat is in our quadrant (and not under our spell)
        boolean freeRatsInQuadrant(Point[] rats, Point current){
          for (Point rat : rats){
            if (withinQuadrant(rat) && distance(current,rat) > WALK_DIST)
              return true;
          }
          return false;
        }
        //Check if Piper is inside his quadrant
        boolean withinQuadrant(Point current){
          if (current.x > dimension/2 && current.y < quadTop && current.y > quadBottom)
            return true;
          return false;
        }
	boolean closetoWall (Point current) {
		boolean wall = false;
		if (Math.abs(current.x-dimension)<pspeed) {
			wall = true;
		}
		if (Math.abs(current.y-dimension)<pspeed) {
			wall = true;
		}
		if (Math.abs(current.y)<pspeed) {
			wall = true;
		}
		return wall;
	}
	int getSide(double x, double y) {
		if (x < dimension * 0.5)
			return 0;
		else if (x > dimension * 0.5)
			return 1;
		else
			return 2;
	}
	int getSide(Point p) {
		return getSide(p.x, p.y);
	}

}
