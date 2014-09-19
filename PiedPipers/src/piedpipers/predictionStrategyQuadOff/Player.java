package piedpipers.predictionStrategyQuadOff;

import java.util.*;
import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
  static int npipers;
  static int nrats;
  static double PIPER_INFLUENCE = 10.0;

  static double MIN_PIPER_SEPARATION = 40.0;

  static double pspeed = 0.4999;
  static double mpspeed = 0.0999;

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

  private Point[] ratsNow;
  private Point[] pipers;
  private boolean[] piperMusic;

  private int[] thetas;

  private int targetRatIndex = 0;
  private int targetRatTicsRemaining = 0;

  /* TURN ON AND OFF QUADRANTS */
  static boolean USE_QUADRANTS = true;

  // The maximum number of calculations to run per Piper per turn
  // Will skip distances evenly over range of board
  static int MAX_DISTANCE_CALCULATIONS = 250;

  private boolean droppedOffRats = false;

  public void init()
  {

  }

  public void init_me() {
    quadrant_height = dimension/npipers;

    apply_new_state(0);

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
        // Check if we're going out to get stragglers
        if(droppedOffRats) {
          new_state = 2;
        } else {
          new_state = 1;
        }
        break;
      case 1:
        new_state = 2;
        break;
      case 2:
        Point closest = getClosestPredictedRat();
        if(closest != null) {
          destination = closest;
          new_state = 2;
        } else {
          new_state = 3;
        }
        break;
      case 3:
        droppedOffRats = true;
        new_state = 4;
        break;
      case 4:
        new_state = 5;
        break;
      case 5:
        new_state = 7;
        break;
      case 7:
        new_state = 7;
        if(!emptyQuadrant(this.ratsNow)) {
          new_state = 0;
          break;
        }
        break;
    }
    return new_state;
  }

  public void apply_new_state(int new_state)
  {
    switch(new_state)
    {
      case 0:
        // Target gate at the beginning
        destination.x = dimension/2;
        destination.y = dimension/2;
        music = false;
        break;
      case 1:
        // Target initial positions on the right side
        destination.x = dimension - PIPER_INFLUENCE;
        destination.y = PIPER_INFLUENCE + ((dimension - PIPER_INFLUENCE * 2.0) / (npipers - 1)) * id;
        music = false;
        break;
      case 2:
        // Target closest predicted rat
        // Destination handled in get_new_state()
        music = true;
        break;

        // Reset cases
      case 3:
        destination.x = dimension/2.0 + 10.0;
        destination.y = dimension/2.0;
        music = true;
        break;
      case 4:
        destination.x = dimension / 2.0 - 10.0;
        destination.y = dimension / 2.0;
        music = true;
        break;
      case 5:
        destination.x = dimension / 2.0 - 10.0;
        destination.y = dimension / 2.0 + 10.0;
        music = true;
        break;

    }
    current_state = new_state;
  }

  private Point getClosestPredictedRat()
  {
    // Returns closest predicted rat
    // Checks if a rat will be within 1 m of PIPER_INFLUENCE in 1 tic
    // If not, checks if a rat will be within 2 m in 2 tic...
    // Once yes, returns that rat's location

    for (int i = 1; i < dimension * 10; i += dimension / MAX_DISTANCE_CALCULATIONS)
    {
      //Point[] futureRatPositions = Predict.getFutureRatPositions(i, dimension, this.ratsNow, piedpipers.sim.Piedpipers.thetas, this.pipers, this.piperMusic);
      Point[] futureRatPositions = Predict.getFutureRatPositions(i, dimension, this.ratsNow, this.thetas, this.pipers, this.piperMusic);

      ArrayList<Point> ratsInRange = ratsInRange(this.pipers[id], i, futureRatPositions);
      if (ratsInRange.size() > 0) {
        this.targetRatTicsRemaining = i;
        return ratsInRange.get(0);
      }
    }

    return null;
  }

  private ArrayList<Point> ratsInRange(Point current, int radius, Point[] rats)
  {
    ArrayList<Point> ratsInRange = new ArrayList<Point>();
    
    if (emptyQuadrant(rats))
      USE_QUADRANTS=false;

    for(int i = 0; i < rats.length; i++)
    {
      if(rats[i].x < dimension / 2.0 || rats[i].y < 0) {
        continue;
      }

      // Check if the rat is within range of where the piper can be in "radius" ticks
      if((helper.distance(current, rats[i]) < (radius * mpspeed) + PIPER_INFLUENCE))
      {
        if (!USE_QUADRANTS || quadrantContainsPoint(id, rats[i])) {
          this.targetRatIndex = i;
          ratsInRange.add(rats[i]);
          return ratsInRange; // Just returning closest one for faster computation
        }
      }
    }
    return ratsInRange;
  }

  //Return true if this piper's quadrant doesn't have any free rats
  public boolean emptyQuadrant(Point[] rats){
    for (int i = 0; i < rats.length; i++){
      if (quadrantContainsPoint(id, rats[i]) && !withinRangeOfPiper(rats[i]))
        return false;
    }
    return true;
  }

  // Returns true if the quadrant for the piper with piperId contains point
  public boolean quadrantContainsPoint(int piperId, Point point)
  {
    if (point.x < dimension / 2.0 || point.x > dimension) {
      return false;
    }

    double topY = quadrant_height * piperId;
    double bottomY = topY + quadrant_height;

    return (point.y > topY && point.y < bottomY);
  }

  // Returns true if Point is within MIN_PIPER_SEPARATION of any piper
  public boolean withinRangeOfPiper(Point point)
  {
    for(int i = 0; i < this.pipers.length; i++)
    {
      if(helper.distance(this.pipers[i], point) < MIN_PIPER_SEPARATION) {
        return true;
      }
    }

    return false;
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

  public Point move(Point[] pipers, Point[] rats, boolean[] pipermusic, int[] thetas) {

    npipers = pipers.length;
    nrats = rats.length;
    Point o;

    this.ratsNow = rats;
    this.pipers = pipers;
    this.piperMusic = pipermusic;
    this.thetas = thetas;

    if (!initi) {
      this.init_me();
      initi = true;
    }

    this.targetRatTicsRemaining--;

    Point current = pipers[id];
    double dist = distance(current, destination);

    		//System.out.printf("[WHEREABOUT] Piper: %d, State: %d, Current: (%f, %f), Destination:(%f, %f), Dist: %f\n",
    		//	id,
    		//	current_state,
    		//	current.x, current.y,
    		//	destination.x, destination.y,
    		//	dist
    		//	);

    // If chasing a target, ensure that target is still going to be there
    if (current_state == 2) {
      Point targetRatLocation = Predict.getFutureRatPositions(this.targetRatTicsRemaining, dimension, this.ratsNow, this.thetas, this.pipers, this.piperMusic)[targetRatIndex];

      if (distance(targetRatLocation, destination) > 1.0) {
        apply_new_state(get_new_state());
        o = calc_offset(current, destination);
        current.x += o.x;
        current.y += o.y;
        return current;	
      }
    }		

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
