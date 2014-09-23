package piedpipers.predictionStrategyQuadFlip;

import java.util.*;
import piedpipers.sim.Point;

public class Player extends piedpipers.sim.Player {
  static int npipers;
  static int nrats;
  static double PIPER_INFLUENCE = 10.0;

  static double MIN_PIPER_SEPARATION = 10.0;

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
  static double quadrant_width = 0;
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
  public static boolean use_quad;

  // The maximum number of calculations to run per Piper per turn
  // Will skip distances evenly over range of board
  static int MAX_DISTANCE_CALCULATIONS = 250;

  private boolean droppedOffRats = false;

  public void init()
  {

  }

  public void init_me() {
    use_quad = true;
    quadrant_height = dimension/npipers;
    quadrant_width = quadrant_height/2;

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
        new_state = 1;
        break;
      case 1:
        new_state = 2;
        break;
      case 2:
        if (emptyQuadrant(this.ratsNow)){
          use_quad = false;
        }
        if (!use_quad){
          use_quad = true;
          if (emptyQuadrant(this.ratsNow))
            use_quad = false;
        }
        Point closest = getClosestPredictedRat();
        if(closest != null) {
          destination = closest;
          new_state = 2;
        }
        else {
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
        // Target gate at the beginning
        destination.x = dimension - MIN_PIPER_SEPARATION;
        destination.y = (quadrant_height * id) + quadrant_height/2;
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

    int cur_tick = 1;
    int tick_increment = 1;

    for (; cur_tick < dimension * 10; cur_tick += tick_increment )
    {
      //Point[] futureRatPositions = Predict.getFutureRatPositions(i, dimension, this.ratsNow, piedpipers.sim.Piedpipers.thetas, this.pipers, this.piperMusic);
      Point[] futureRatPositions = Predict.getFutureRatPositions(cur_tick, dimension, this.ratsNow, this.thetas, this.pipers, this.piperMusic);

      ArrayList<Point> ratsInRange = ratsInRange(this.pipers[id], cur_tick, futureRatPositions);
      if (ratsInRange.size() > 0) {
        this.targetRatTicsRemaining = cur_tick;
        return ratsInRange.get(0);
      }
      //If our quadrant is empty in the next tick_increment, increase tick_increment to look further into
      //the future, faster //TODO: TEST THIS AGAINST NOT DOING A CHECK AGAINST EMPTINESS
      if (cur_tick > 20 && emptyQuadrant(futureRatPositions)){ //TODO: TEST TO FIND NUMBER TO REPLACE 20
        tick_increment += tick_increment;//TODO: TEST TO SEE IF WE SHOULD INCREMENT TICK BY MORE THAN ITSELF
      }
    }

    return null;
  }

  private ArrayList<Point> ratsInRange(Point current, int radius, Point[] rats)
  {
    ArrayList<Point> ratsInRange = new ArrayList<Point>();
    double rat_distance;
    

    for(int i = 0; i < rats.length; i++)
    {
      if(rats[i].x < dimension / 2.0 || rats[i].y < 0) {
        continue;
      }
      rat_distance = helper.distance(current,rats[i]);

      // Check if the rat is within range of where the piper can be in "radius" ticks
      if(rat_distance < (radius * mpspeed) + PIPER_INFLUENCE)
      {
        if ((!use_quad && rat_distance > PIPER_INFLUENCE) || quadrantContainsPoint(id, rats[i])) {
          this.targetRatIndex = i;
          ratsInRange.add(rats[i]);
          return ratsInRange; // Just returning closest one for faster computation
        }
      }
    }
    return ratsInRange;
  }

  public int getHighestPiperCloseTo(Point point,int radius){
    for (int i = npipers-1; i > -1; i--)
      if (withinRangeOfPiper(i,point,radius))
        return i;
    return -1;
  }

  public boolean withinRangeOfMultiplePipers( Point point, int radius){
    int count = 0;
    for (int i = 0; i < npipers; i ++){
      if (withinRangeOfPiper(i,point,radius))
        count++;
      if (count >= 2)
        return true;
    }
    return false;
    
  }

  //Return true if this piper's quadrant doesn't have any free rats
  public boolean emptyQuadrant(Point[] rats){
    for (int i = 0; i < rats.length; i++){
      if (quadrantContainsPoint(id, rats[i]) && !withinRangeOfPiper(rats[i])){
        return false;
      }
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

  // Returns true if Point is within MIN_PIPER_SEPARATION of a given piper
  public boolean withinRangeOfPiper(int pid, Point point, int radius)
  {
    if(helper.distance(this.pipers[pid], point) < (radius * mpspeed) + MIN_PIPER_SEPARATION) 
        return true;
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
      if (distance(targetRatLocation, destination) > 1.0 ) {
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
