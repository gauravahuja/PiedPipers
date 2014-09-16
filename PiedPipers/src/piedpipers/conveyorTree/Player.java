package piedpipers.conveyorTree;

import java.util.*;
import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
	static int npipers;
	static int nrats;
	static double PIPER_INFLUENCE = 10.0;

	static double pspeed = 0.49;
	static double mpspeed = 0.09;
	static boolean start_belt = false;

	static Point target = new Point();
	static boolean finishround = true;
	static boolean initi = false;

	static int current_state = 0;
	static Random r = new Random();

	static Point destination = new Point();
	
	private Point[] pipers;
	private Point previousParentLocation;

	public void init()
	{

	}

	public void init_me() {
		current_state = 0;
		music = false;
		
		if (id == 0) {
			destination.x = dimension/2.0 - 5.0;
			destination.y = dimension/2.0;
		}
		else {
			apply_new_state(-10);
		}
		
		System.out.printf("Intializing Piper: %d\n", id);

	}

	static double distance(Point a, Point b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	public int get_new_state()
	{
		int new_state = -1;
		
		if (id == 0)
			return -2;
		
		switch(current_state)
		{
			case 1:
				// Moving to home location
				new_state = 2;
				break;
			case 2:
				// Waiting at home location
				if (start_belt && (leafNode(id) || bothChildrenArrived(id))) {
					new_state = 3;
				} else {
					new_state = 2;
				}
				break;
			case 3:
				// Moving to parent
				if (distance(homeLocation(id / 2), pipers[id / 2]) > 0.5 || id == 1)
					//Wait until parent starts to leave, although this condition does not test leaving!
					new_state = 1;
				else
					new_state = 3;
				new_state=1;
				break;
				
			case -10:
				// Moving to center
				new_state = -9;
				break;
			case -9:
				new_state = 1;
				
		}
		return new_state;
	}
	
	public void apply_new_state(int new_state)
	{
		switch(new_state)
		{
			case 1:
				// Moving to home location
				destination = homeLocation(id);
				music = false;
				break;
			case 2:
				// Waiting at home location
				music = true;
				break;
			case 3:
				// Moving to parent
				destination = homeLocation(id / 2);
				previousParentLocation = destination;
				music = true;
				break;
				
			case -2:
				// Rooted magnet node 0
				destination.x = dimension/2.0 - 5.0;
				destination.y = dimension/2.0;
				music = true;
				break;
				
			case -10:
				// Moving to center
				destination.x = dimension/2 - 0.5;
				destination.y = dimension/2;
				music = false;
				break;
			case -9:
				// Moving to center
				destination.x = dimension/2 + 0.5;
				destination.y = dimension/2;
				music = false;
				break;
		}
		current_state = new_state;
	}
	
	private boolean leafNode(int id)
	{
		return ((id * 2) > npipers - 1);
	}
	
	private boolean bothChildrenArrived(int id)
	{
		return ((distance(pipers[id], pipers[id * 2]) < 1.5) && (distance(pipers[id], pipers[(id * 2) + 1]) < 1.5));
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

	public boolean all_at_home(Point[] pipers)
	{
		boolean all_at_home = true;
		int i = 1;
		for (; i < npipers; i++)
		{
			if (distance(pipers[i], homeLocation(i)) > 0.5)
			{
				System.out.printf("[ALL_AT_HOME] Piper: %d not at home\n", i);
				all_at_home = false;
				break;
			}
		}
		if (i==npipers)
			System.out.printf("[ALL_AT_HOME] All Pipers at home\n");
		return all_at_home;
	}

	public Point move(Point[] pipers, Point[] rats, boolean[] pipermusic) {

		npipers = pipers.length;
		nrats = rats.length;
		if (!initi) {
			this.init_me();
			initi = true;
		}
		
		this.pipers = pipers;

		Point current = pipers[id];
		double dist = distance(current, destination);
		
				System.out.printf("[WHEREABOUT] Piper: %d, State: %d, Current: (%f, %f), Destination:(%f, %f), Dist: %f\n",
					id,
					current_state,
					current.x, current.y,
					destination.x, destination.y,
					dist
					);
		if (!start_belt)
		{
			if(all_at_home(pipers))
			{
				start_belt = true;
				System.out.printf("Starting Belt\n");
			}
		}
				
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

	private Point homeLocation(int piperId) {
		Point p = new Point();
		double theta = Math.PI / 4;
		switch (piperId)
		{
			case 0:
				p.x = (dimension / 2.0);
				p.y = (dimension / 2.0);
				break;
			case 1:
				p.x = (3.0 / 4.0) * dimension;
				p.y = dimension / 2.0;
				break;
			case 2:
				p.x = (3.0 / 4.0) * dimension;
				p.y = (3.0 / 4.0) * dimension;
				break;
			case 3:
				p.x = (3.0 / 4.0) * dimension;
				p.y = dimension / 4.0;
				break;
			case 4:
				p.x = (3.0 / 4.0) * dimension - (dimension / 4.0) * Math.cos(theta);
				p.y = (3.0 / 4.0) * dimension + (dimension / 4.0) * Math.sin(theta);
				break;
			case 5:
				p.x = (3.0 / 4.0) * dimension + (dimension / 4.0) * Math.cos(theta);
				p.y = (3.0 / 4.0) * dimension + (dimension / 4.0) * Math.sin(theta);
				break;
			case 6:
				p.x = (3.0 / 4.0) * dimension - (dimension / 4.0) * Math.cos(theta);
				p.y = (dimension / 4.0) - (dimension / 4.0) * Math.sin(theta);
				break;
			case 7:
				p.x = (3.0 / 4.0) * dimension + (dimension / 4.0) * Math.cos(theta);
				p.y = (dimension / 4.0) - (dimension / 4.0) * Math.sin(theta);
				break;
			default:
				p.x = 0;
				p.y = 0;
				break;
		}
		return p;
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
