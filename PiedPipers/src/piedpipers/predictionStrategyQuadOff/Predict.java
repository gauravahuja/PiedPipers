package piedpipers.predictionStrategyQuadOff;

import java.util.*;
import piedpipers.sim.Point;
import piedpipers.predictionStrategyQuadOff.helper;


public class Predict
{
  public static Point[] getFutureRatPositions(int ticks, int dimension, Point[] ratsNow, int[] ratThetas, Point[] pipers, boolean[] piperMusic)
  {
    Point[] ratsFuture = new Point[ratsNow.length];

    for (int i = 0; i < ratsNow.length; i++)
    {
      boolean ratFree = true;

      for (int j = 0; j < pipers.length; j++)
      {
        if (ratsNow[i].x < dimension/2 || (helper.distance(ratsNow[i], pipers[j]) <= helper.PIPER_INFLUENCE && piperMusic[j] == true))
        {
          ratFree = false;
          break;
        }

      }

      if (ratFree == true)
      {
        ratsFuture[i] = moveRat(ticks, ratsNow[i], ratThetas[i], dimension);
      }
      else
      {
        ratsFuture[i] = new Point(-1, -1);
      }
    }
    return ratsFuture;
  }

  static Point moveRat(int ticks, Point ratNow, int theta, int dimension) 
  {
    double ox = 0, oy = 0;
    double d = helper.RAT_SPEED*ticks;

    // non of above cases, just random wandering as rats
    ox = d * Math.sin(theta * Math.PI / 180);
    oy = d * Math.cos(theta * Math.PI / 180);

    Point npos = updatePosition(ratNow, ox, oy, theta, dimension);
    return npos;
  }

  static Point updatePosition(Point now, double ox, double oy, int thetaNow, int dimension) 
  {
    double nx = now.x + ox, ny = now.y + oy;

    // hit the left fence
    if (nx < 0) {
      // System.err.println("RAT HITS THE LEFT FENCE!!!");
      // move the point to the left fence
      Point temp = new Point(0, now.y);
      // how much we have already moved in x-axis?
      double moved = 0 - now.x;
      // how much we still need to move
      // BUT in opposite direction
      double ox2 = -(ox - moved);
      //Random random = new Random();

      //int theta = random.nextInt(360);
      int theta = -thetaNow;
      thetaNow = theta;
      return updatePosition(temp, ox2, oy, thetaNow, dimension);
    }
    // hit the right fence
    if (nx > dimension) {
      // System.err.println("RAT HITS THE RIGHT FENCE!!!");
      // move the point to the right fence
      Point temp = new Point(dimension, now.y);
      double moved = (dimension - now.x);
      double ox2 = -(ox - moved);

      int theta = -thetaNow;
      thetaNow = theta;
      return updatePosition(temp, ox2, oy, thetaNow, dimension);
    }
    // hit the up fence
    if (ny < 0) {
      // System.err.println("RAT HITS THE UP FENCE!!!");
      // move the point to the up fence
      Point temp = new Point(now.x, 0);
      double moved = 0 - now.y;
      double oy2 = -(oy - moved);
      //Random random = new Random();

      int theta = 180-thetaNow;
      thetaNow = theta;
      return updatePosition(temp, ox, oy2, thetaNow, dimension);
    }
    // hit the bottom fence
    if (ny > dimension) {
      // System.err.println("RAT HITS THE BOTTOM FENCE!!!");
      Point temp = new Point(now.x, dimension);
      double moved = (dimension - now.y);
      double oy2 = -(oy - moved);
      //Random random = new Random();
      int theta = 180-thetaNow;
      thetaNow = theta;
      return updatePosition(temp, ox, oy2, thetaNow, dimension);
    }
    assert nx >= 0 && nx <= dimension;
    assert ny >= 0 && ny <= dimension;
    // hit the middle fence
    if (helper.hitTheFence(now.x, now.y, nx, ny, dimension)) {
      // System.err.println("SHEEP HITS THE CENTER FENCE!!!");
      // System.err.println(nx + " " + ny);
      // System.err.println(ox + " " + oy);
      // move the point to the fence
      Point temp = new Point(dimension/2, now.y);
      double moved = (dimension/2 - now.x);
      double ox2 = -(ox - moved);
      //Random random = new Random();
      int theta = -thetaNow;
      thetaNow = theta;
      return updatePosition(temp, ox2, oy, thetaNow, dimension);
    }
    // otherwise, we are good
    return new Point(nx, ny);
  }   
}
