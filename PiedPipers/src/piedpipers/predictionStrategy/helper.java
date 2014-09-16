package piedpipers.predictionStrategy;

import java.util.*;
import piedpipers.sim.Point;

public class helper
{
    static int PIPER_INFLUENCE = 10; //10m Influence radius
    static double RAT_SPEED = 0.1; // Speed 0.1m/tick = 1m/s, walking speed for rats

    public static double distance(Point a, Point b) 
    {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    // up side is 0
    // bottom side is 1
    // at the fence 2
    public static int getSide(double x, double y, int dimension) {
        if (x < dimension * 0.5)
            return 0;
        else if (x > dimension * 0.5)
            return 1;
        else
            return 2;
    }

    public static int getSide(Point p, int dimension) {
        return getSide(p.x, p.y, dimension);
    }

    public static boolean hitTheFence(double x1, double y1, double x2, double y2, int dimension) {
        // on the same side
        if (getSide(x1, y1, dimension) == getSide(x2, y2, dimension))
            return false;

        // one point is on the fence
        if (getSide(x1, y1, dimension) == 2 || getSide(x2, y2, dimension) == 2)
            return false;

        // compute the intersection with (50, y3)
        // (y3-y1)/(50-x1) = (y2-y1)/(x2-x1)

        double y3 = (y2 - y1) / (x2 - x1) * (dimension/2 - x1) + y1;

        assert y3 >= 0 && y3 <= dimension;

        // pass the openning?
        double OPEN_LEFT = dimension/2-1;
        double OPEN_RIGHT = dimension/2+1;
   
        if (y3 >= OPEN_LEFT && y3 <= OPEN_RIGHT) 
            return false;
        else {
            return true;
        }
    }
    
}