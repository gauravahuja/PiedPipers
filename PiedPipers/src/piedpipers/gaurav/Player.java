package piedpipers.gaurav;

import java.util.*;

import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
	static int npipers;
	static int nrats;
	static double PIPER_INFLUENCE = 10.0;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;
	
	static Point target = new Point();
	static int[] thetas;
	static boolean finishround = true;
	static boolean initi = false;
	
	static double xl;
	static double xr;
	static double yb;
	static double yt;
	static double quadrant_height = 0;
	static int state = 0;
	
	static Point destination = new Point();

	public void init()
	{

	}

	public void init_all() {
		thetas = new int[npipers];
		quadrant_height = dimension/npipers;

	    xl = dimension/2 + PIPER_INFLUENCE;
	    xr = dimension - PIPER_INFLUENCE;
	    yb = quadrant_height*id + PIPER_INFLUENCE;
	    yt = quadrant_height*id + quadrant_height - PIPER_INFLUENCE;

	    destination.x = dimension/2;
	    destination.y = dimension/2;

	    System.out.println("Intializing");
	    System.out.println("Dimension: " + dimension);
	    System.out.println("Quadrant Height: " + quadrant_height);	
	    System.out.printf("(xl=%f, yb=%f)->(xr=%f, yt=%f)\n", xl, yb, xr, yt);
	    System.out.printf("Destination: (%f, %f)\n", destination.x, destination.y);
	}

	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	public Point move(Point[] pipers, Point[] rats) {
		npipers = pipers.length;
		nrats = rats.length;
		System.out.println("Piper: " + id + " Init?: " + initi);
		Point gate = new Point(dimension/2, dimension/2);
		Point current = pipers[id];
		double ox = 0, oy = 0;

		if (!initi) {
			this.init_all();
			initi = true;
		}
		double dist = distance(current, destination);


		System.out.printf("Piper: %d, (xl=%f, yb=%f)->(xr=%f, yt=%f)\n", id, xl, yb, xr, yt);
		System.out.printf("Piper: %d, State: %d\n", id, state);
		System.out.printf("Piper: %d, Current: (%f, %f)\n", id, current.x, current.y);
		System.out.printf("Piper: %d, Destination: (%f, %f)\n", id, destination.x, destination.y);
		System.out.printf("Piper: %d, Dist: %f\n", id, dist);

		if (dist > 0.5)
		{
			double speed = 0;
			if (this.music == false)
			{
				speed = pspeed;
				
			}
			else
			{
				speed = mpspeed;
			}

			ox = (destination.x - current.x)/dist * speed;
			oy = (destination.y - current.y)/dist * speed;	
		}
		else
		{
			System.out.printf("Piper: %d, State Space Change, Old Destination: (%f, %f)\n", id, destination.x, destination.y);
			switch(state)
			{
				case 0:
					destination.x = xr;
					destination.y = yb;
					state += 1;
					break;
				case 1:
					this.music = true;
					destination.x = xl;
					destination.y = yb;
					state += 1;
					break;
				case 2:
					this.music = true;
					destination.x = xr;
					destination.y = yb;
					state -= 1;
					break;
			}
			System.out.printf("Piper: %d, State Space Change, New Destination: (%f, %f)\n", id, destination.x, destination.y);
		}
		current.x += ox;
		current.y += oy;
		return current;
	}
	boolean closetoWall (Point current) {
		boolean wall = false;
		if (Math.abs(current.x-dimension)<PIPER_INFLUENCE) {
			wall = true;
		}
		if (Math.abs(current.y-dimension)<PIPER_INFLUENCE) {
			wall = true;
		}
		if (Math.abs(current.y)<PIPER_INFLUENCE) {
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
