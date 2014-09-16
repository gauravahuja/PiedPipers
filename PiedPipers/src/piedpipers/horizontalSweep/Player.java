package piedpipers.horizontalSweep;

import java.util.*;
import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
	static int npipers;
	static int nrats;
	static double PIPER_INFLUENCE = 10.0;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;
	
	static Point target = new Point();
	static boolean finishround = true;
	static boolean initi = false;
	
	static double xl;
	static double xr;
	static double yb;
	static double yt;
	static double quadrant_height = 0;
	static int current_state = 0;
	static boolean check_for_rats = false;
	
	static Point destination = new Point();

	public void init()
	{

	}

	public void init_me() {
		quadrant_height = dimension/npipers;

	    xl = dimension/2 + PIPER_INFLUENCE;
	    xr = dimension - PIPER_INFLUENCE;
	    yt = quadrant_height*id + quadrant_height/2;
	    yb = quadrant_height*id + quadrant_height - PIPER_INFLUENCE;

	    current_state = 0;
	    this.music = false;
	    destination.x = dimension/2;
	    destination.y = dimension/2;

	    System.out.printf("Intializing Piper: %d\n", id);
	    System.out.println("Quadrant Height: " + quadrant_height);	
	    System.out.printf("(xl=%f, yt=%f)->(xr=%f, yb=%f)\n", xl, yt, xr, yb);
	}

	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	public boolean all_rats_mesmerized(Point[] pipers, Point[] rats)
	{
		double [] distances = new double[nrats];
		for (int r = 0; r < nrats; r++)
		{
			distances[r] = dimension;
			if(getSide(rats[r]) == 0)
				distances[r] = 0;
			else
			{
				for(int p = 0; p < npipers; p++)
				{
					distances[r] = Math.min(distance(pipers[p], rats[r]), distances[r]);

				}
			}
			
		}
		
		boolean all_rats_m = true;
		for (int r = 0; r < nrats; r++)
		{
			if (distances[r] >= PIPER_INFLUENCE)
			{
				all_rats_m = false;
				break;
			}
		}

		if (all_rats_m)
			return true;
		else
			return false;
	}
	
	public int get_new_state()
	{
		int new_state = -1;
		switch(current_state)
		{
			case 0:
				new_state = 1;
				break;
			case 1:
				new_state = 2;
				break;
			case 2:
				new_state = 1;					
				break;
			case 3:
				new_state = 4;
				break;
			case 4:
				new_state = 0;
				break;
		}
		return new_state;
	}

	public void apply_new_state(int new_state)
	{
		switch(new_state)
		{
			case 0:
				destination.x = dimension/2;
				destination.y = dimension/2;
				music = false;
				break;
			case 1:
				destination.x = xr;
				destination.y = yt;
				break;
			case 2:
				destination.x = xl;
				destination.y = yt;
				music = true;
				check_for_rats = true;
				break;
			case 3:
				destination.x = dimension/2;
				destination.y = dimension/2;
				check_for_rats = false;
				break;
			case 4:
				destination.x = dimension/2 - 5;
				destination.y = dimension/2;
				break;
		}
		current_state = new_state;
	}

	public Point calc_offset(Point c, Point d)
	{
		double dist = distance(c, d);
		double speed = 0;
		Point offset = new Point();
		if (music == false)
		{
			speed = pspeed;	
		}
		else
		{
			speed = mpspeed;
		}

		offset.x = (d.x - c.x)/dist * speed;
		offset.y = (d.y - c.y)/dist * speed;
		return offset;
	}

	public Point move(Point[] pipers, Point[] rats, boolean[] pipermusic) {
		
		npipers = pipers.length;
		nrats = rats.length;
		if (!initi) {
			this.init_me();
			initi = true;
		}
		
		Point current = pipers[id];
		double dist = distance(current, destination);

		System.out.printf("[WHEREABOUT] Piper: %d, State: %d, Current: (%f, %f), Destination:(%f, %f), Dist: %f\n",
			id,
			current_state,
			current.x, current.y,
			destination.x, destination.y,
			dist
			);
		
		Point o;
		if (dist > 0.5)
		{
			o = calc_offset(current, destination);			
		}
		else
		{
			int new_state = get_new_state();
			apply_new_state(new_state);
			o = calc_offset(current, destination);
		}
		if (check_for_rats && all_rats_mesmerized(pipers, rats))
		{
			apply_new_state(3);
			o = calc_offset(current, destination);
		}
		current.x += o.x;
		current.y += o.y;
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
