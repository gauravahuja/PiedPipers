package piedpipers.predictionStrategyQuadFlip_handover;

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
  static boolean initi = false;

  static double quadrant_height = 0;
  static int current_state = 0;
  static final int FURTHEST_RAT_STATE = 1;

  static Point destination = new Point();

  private Point[] ratsNow;
  private Point[] pipers;
  private boolean[] piperMusic;

  private int[] thetas;

  private int targetRatIndex = 0;
  private int targetRatTicsRemaining = 0;

  /* TURN ON AND OFF QUADRANTS */
  public static boolean use_quad;

  public void init()
  {

  }

  public void init_me() {
    use_quad = true;
    quadrant_height = dimension/npipers;

    apply_new_state(0);

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
        new_state = 100;
        break;
      case 100:
        new_state = 1;
        break;
      case 1:
        new_state = 2;
        break;
      case 2:
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
      case 100:
        //Squeeze a bit past the gate to avoid illegal moves through the fence
        destination.x = dimension/2+2;
        destination.y = dimension/2;
        music = false;
        break;
      case 1:
        //Go to furthest predicted rat postion
        current_state = new_state;
        music = false;
        Point new_d = getFurthestPredictedRat();
        if (new_d == null)
        {
          use_quad = false;
          new_d = getFurthestPredictedRat();
          if (new_d == null)
          {
              destination.x = dimension/2.0 + 2;
              destination.y = dimension/2.0;
              new_state  = 100;
          }
        }
        else
        {
          destination = new_d;
        }
        
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

  private Point getFurthestPredictedRat()
  {
    // Returns furthest predicted rat within a quadrant
    // Used to determine starting location of a piper in a game.



    double ROOT_2 = 1.414;
    Point rightmost = new Point();
    Point gate = destination;//where we are now
    rightmost.x = dimension - MIN_PIPER_SEPARATION;
    rightmost.y = quadrant_height * id + quadrant_height/2;

    //If we're using quadrants, go to futhest rat from gate
    int cur_tick = (int) (distance(gate,rightmost) / pspeed);

    if (!use_quad)
      //Otherwise, just start looking as far across field as possible
      cur_tick = (int) (dimension/(pspeed * ROOT_2));

    //Keep track of furthest rat we find
    Point max_Rat = null;
    double max_distance = 0;

    //
    for (; cur_tick > 0; cur_tick -= 1 )
    {
      Point[] futureRatPositions = Predict.getFutureRatPositions(cur_tick, dimension, this.ratsNow, this.thetas, this.pipers, this.piperMusic);

      ArrayList<Point> ratsInRange = ratsInRange(this.pipers[id], cur_tick, futureRatPositions);
      if (ratsInRange.size() > 0){
        for (int i = 0; i < ratsInRange.size() ; i ++){
          double d = distance(ratsInRange.get(i),this.pipers[id]);
          if (d > max_distance){ 
            max_Rat = ratsInRange.get(i);
            max_distance = d;
          }
        }
        break;
      }
    }
    this.targetRatTicsRemaining = cur_tick;
    return max_Rat;

    

  }
  private Point getClosestPredictedRat()
  {
    // Returns closest predicted rat
    // Checks if a rat will be within 1 m of PIPER_INFLUENCE in 1 tic
    // If not, checks if a rat will be within 2 m in 2 tic...
    // Once yes, returns that rat's location

    int cur_tick = 1;
    int tick_increment = 1;
    boolean flag = false;

    int limit = dimension * 10;
    if (use_quad)
      limit = limit / 2;

    for (; cur_tick < limit; cur_tick += 1 )
    {
      //Point[] futureRatPositions = Predict.getFutureRatPositions(i, dimension, this.ratsNow, piedpipers.sim.Piedpipers.thetas, this.pipers, this.piperMusic);
      Point[] futureRatPositions = Predict.getFutureRatPositions(cur_tick, dimension, this.ratsNow, this.thetas, this.pipers, this.piperMusic);

      ArrayList<Point> ratsInRange = ratsInRange(this.pipers[id], cur_tick, futureRatPositions);
      if (ratsInRange.size() > 0) {
        if (use_quad && !quadrantContainsPoint(id, this.ratsNow[targetRatIndex])){
          cur_tick = 1;
          use_quad = false;
          flag = true;
          limit = dimension * 10;
          continue;
        }
        this.targetRatTicsRemaining = cur_tick;
        if (flag)
          use_quad = true;
        return ratsInRange.get(0);
      }
    }

    return null;
  }

  private ArrayList<Point> ratsInRange(Point current, int radius, Point[] rats)
  {
    ArrayList<Point> ratsInRange = new ArrayList<Point>();
    double rat_distance;
    double speed;


    if (piperMusic[id])
      speed = mpspeed;
    else
      speed = pspeed;
    

    for(int i = 0; i < rats.length; i++)
    {
      if(rats[i].x < dimension / 2.0 || rats[i].y < 0) {
        continue;
      }
      rat_distance = helper.distance(current,rats[i]);

      // Check if the rat is within range of where the piper can be in "radius" ticks
      if(rat_distance < (radius * speed) + PIPER_INFLUENCE)
      {
        if ((!use_quad && rat_distance > PIPER_INFLUENCE) || quadrantContainsPoint(id, rats[i])) {
          this.targetRatIndex = i;
          ratsInRange.add(rats[i]);
          if (current_state != FURTHEST_RAT_STATE){
            return ratsInRange; // Just returning closest one for faster computation
          }
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

    		// System.out.printf("[WHEREABOUT] Piper: %d, State: %d, Current: (%f, %f), Destination:(%f, %f), Dist: %f\n",
    		// 	id,
    		// 	current_state,
    		// 	current.x, current.y,
    		// 	destination.x, destination.y,
    		// 	dist
    		// 	);

    // If chasing a target, ensure that target is still going to be there
    if (current_state == 2) {
      Point closetPiper = null;
      double min_distance = dimension;
      for (int i = 0; i < id; i++)
      {
        if(piperMusic[i] == true)
        {
          double d = helper.distance(current, pipers[i]);
          if (d < min_distance)
          {
            closetPiper = pipers[i];
            min_distance = d;
          }
        }
      }
      if (min_distance < PIPER_INFLUENCE - 3)
      {
        apply_new_state(1);
        o = calc_offset(current, destination);
        current.x += o.x;
        current.y += o.y; 
        return current;
      }

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
