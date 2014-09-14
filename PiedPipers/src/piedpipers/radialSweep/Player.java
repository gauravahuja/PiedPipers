package piedpipers.radialSweep;

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
	
	static int theta_low = 0;
	static int theta = 0;
	static int phi = 0;
	static int current_state = 0;
	static Random r = new Random();
	
	static Point destination = new Point();

	public void init()
	{

	}

	public void init_me() {
		theta_low = (180*id)/npipers;
		phi = 180/npipers;
	    current_state = 0;
	    music = false;
	    destination.x = dimension/2;
	    destination.y = dimension/2;
	    System.out.printf("Intializing Piper: %d, Theta_low = %d\n", id, theta_low);
	
	}

	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
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
				new_state = 3;					
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

	public Point get_destination(int angle)
	{
		if (angle == 0)
			angle = 1;
		if (angle == 180)
			angle = 179;
		double x, y;
		if (angle>=45 && angle <= 135)
		{
			y = dimension/2 * (1 - Math.tan((angle - 90)*Math.PI/180));
			x = dimension;
		}
		else
		{
			if(angle > 135)
			{
				y = 0;
				angle = 180-angle;
			}
			else
				y = dimension;

			x = dimension/2 *(1+Math.tan(angle*Math.PI/180));
		}

		return new Point(x, y);


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
				destination.x = dimension/2+2;
				destination.y = dimension/2;
				break;
			case 2:
				theta = theta_low + r.nextInt(phi);
				if (theta > 180)
					theta = 180;
				destination = get_destination(theta);
				break;
			case 3:
				destination.x = dimension/2+2;
				destination.y = dimension/2;
				music = true;
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

	public Point move(Point[] pipers, Point[] rats) {
		
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
		
		if (dist <= 0.5)
		{
			int new_state = get_new_state();
			apply_new_state(new_state);
		}

		Point o = calc_offset(current, destination);
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
