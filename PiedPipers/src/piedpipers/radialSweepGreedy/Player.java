package piedpipers.radialSweep;

import java.util.*;
import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
	static int npipers;
	static int nrats;
	static double PIPER_INFLUENCE = 10.0;
	
	static double pspeed = 0.49;
	static double mpspeed = 0.09;
	static boolean initi = false;
	
	static int theta_low = 0;
	static int theta = 0;
	static int phi = 0;
	static int current_state = 0;
	
	static Point destination = new Point();
	static Point gate = new Point();

	public void init()
	{

	}

	public void init_me() {
		theta_low = (180*id)/npipers;
		phi = 180/npipers;
	    current_state = 0;
	    music = false;
	    gate.x = dimension/2;
	    gate.y = dimension/2;
	    destination.x = dimension/2-2;
	    destination.y = dimension/2;
	    System.out.printf("Intializing Piper: %d, Theta_low = %d, Phi = %d\n", id, theta_low, phi);
	
	}

	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	boolean withinArea(Point rat, int theta_low, int phi, int dimension)
	{
		int theta_1 = theta_low;
		int theta_2 = theta_low+phi;
		
		double dx = (double)(rat.x - gate.x);
		double dy = (double)(rat.y - gate.y);
		
		int theta_rat = (int)(Math.atan(Math.abs(dx/dy))*180.0/Math.PI);
		if (dy < 0)
		{
			theta_rat = 180 - theta_rat;
		}

		// System.out.printf("[withinArea] Piper: %d, Rat: (%f, %f), dx:  %f, dy: %f, theta_rat:%d, theta_1:%d, theta_2:%d, Side: %d\n",
		// 	id,
		// 	rat.x, rat.y,
		// 	dx, dy,
		// 	theta_rat,
		// 	theta_1, 
		// 	theta_2,
		// 	getSide(rat));

		if (getSide(rat) != 1)
		{
			return false;
		}
		if (theta_rat >= theta_1 && theta_rat <= theta_2)
		{
			return true;
		}
		return false;
	}

	boolean canHearMusic(Point rat, Point[] pipers, boolean[] piperMusic)
	{
		boolean mesmerized = false;
		for (int i = 0; i < npipers; i++)
		{
			if (piperMusic[i] == false)
				continue;

			if (distance(pipers[i], rat) < PIPER_INFLUENCE)
			{
				mesmerized = true;
				break;
			}

		}
		return mesmerized;
	}


	Point getClosestRatCoord(Point current, Point[] rats, Point[] pipers, boolean[] piperMusic)
	{
		double min_dist = 2*dimension;
		Point min_point = new Point(dimension*100, dimension*100);
		for (Point rat : rats)
		{
			if (withinArea(rat, theta_low, phi, dimension) && !canHearMusic(rat, pipers, piperMusic))
			{
				if(distance(current, rat) < min_dist)
				{
					min_point = rat;
					min_dist = distance(current, rat);
				}
			}
		}

		return min_point;
	}

	Point getFarthestRatCoord(Point current, Point[] rats, Point[] pipers, boolean[] piperMusic)
	{
		double max_dist = 0;
		Point max_point = current;
		int count = 0;
		for (Point rat : rats)
		{
			boolean a = withinArea(rat, theta_low, phi, dimension);
			boolean m = canHearMusic(rat, pipers, piperMusic) ;
			if(a == true)
				count += 1;
			if (a && !m)
			{
				if(distance(current, rat) > max_dist)
				{
					max_point = rat;
					max_dist = distance(current, rat);
				}
			}
			else
			{
				// System.out.printf("[getFarthestRatCoord][a=%b, m=%b] Piper: %d, Rat: (%f, %f), theta_low: %d, phi: %d\n",
				// 	a,m,
				// 	id,
				// 	rat.x, rat.y,
				// 	theta_low,
				// 	phi);
			}
		}
		// System.out.printf("[getFarthestRatCoord] Piper: %d, Rats in area: %d\n", id, count);

		return max_point;
	}
	
	public int get_new_state(Point current, Point[] rats, Point[] pipers, boolean[] piperMusic)
	{
		if (distance(current, destination) > 0.5)
		{
			return current_state;
		}
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
				Point closest = getClosestRatCoord(current, rats, pipers, piperMusic);
				double dr = distance(closest, current);
				double dg = distance(current, gate);
				System.out.printf("[WHEREABOUT-C] Piper: %d, Closet rat: (%f, %f), dr: %f, dg: %f\n",
					id,
					closest.x, closest.y,
					dr,
					dg);
				if (dg < dr)
				{
					new_state = 4;
				}
				else
				{
					destination = closest;
					new_state = 3;
				}
				break;
			case 4:
				new_state = 5;
				break;
			case 5:
				new_state = 0;
				break;
		}
		return new_state;
	}

	public void apply_new_state(int new_state, Point current, Point[] rats, Point[] pipers, boolean[] piperMusic)
	{
		if (current_state == new_state)
			return;
		switch(new_state)
		{
			case 0:
				destination.x = dimension/2-2;
				destination.y = dimension/2;
				music = false;
				break;
			case 1:
				destination.x = dimension/2+2;
				destination.y = dimension/2;
				music = false;
				break;
			case 2:
				destination = getFarthestRatCoord(gate, rats, pipers, piperMusic);
				// System.out.printf("[WHEREABOUT-D] Piper: %d, Farthest rat: (%f, %f)\n",
				// 	id,
				// 	destination.x, destination.y);
				music = false;
				break;
			case 3:
				Point temp = getClosestRatCoord(current, rats, pipers, piperMusic);
				if (temp.x <=dimension && temp.y <= dimension && temp.x >= 0 && temp.y >= 0)
					destination = temp;
				music = true;
				break;
			case 4:
				destination.x = dimension/2 + 2;
				destination.y = dimension/2;
				break;
			case 5:
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
		Point offset = new Point(0,0);
		if (music == false)
		{
			speed = pspeed;	
		}
		else
		{
			speed = mpspeed;
		}
		if (dist == 0)
			return offset;

		offset.x = (d.x - c.x)/dist * speed;
		offset.y = (d.y - c.y)/dist * speed;
		return offset;
	}

	public Point move(Point[] pipers, Point[] rats, boolean[] piperMusic) {
		
		npipers = pipers.length;
		nrats = rats.length;
		if (!initi) {
			this.init_me();
			initi = true;
		}
		
		Point current = pipers[id];
		double dist = distance(current, destination);

		System.out.printf("[WHEREABOUT-A] Piper: %d, State: %d, Current: (%f, %f), Destination:(%f, %f), Dist: %f\n",
			id,
			current_state,
			current.x, current.y,
			destination.x, destination.y,
			dist
			);
		
		int new_state = get_new_state(current, rats, pipers, piperMusic);
		apply_new_state(new_state, current, rats, pipers, piperMusic);

		Point o = calc_offset(current, destination);
		current.x += o.x;
		current.y += o.y;

		System.out.printf("[WHEREABOUT-B] Piper: %d, State: %d, Current: (%f, %f), Destination:(%f, %f), Offfset: (%f, %f)\n",
			id,
			current_state,
			current.x, current.y,
			destination.x, destination.y,
			o.x, o.y
			);
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
