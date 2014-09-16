package piedpipers.group9;

import java.util.Iterator;
import java.util.Map;

import piedpipers.sim.Point;

/**/

public class PlayUtilities {
	
	public static void updateGlobal(Point[] pipers, Point[] rats){
		Global.rats=rats;
		Global.pipers=pipers;
	}
	
	//square point number
	// return square in the middle of the sweep field
	public Point moveInSquare(int pointNumber){
		
		switch(pointNumber){
			case 0:
				return new Point(2.0*(double)Global.dimension/3.0,(double)Global.dimension/3.0);
			case 1:
				return new Point(2.0*(double)Global.dimension/3.0,2.0*(double)Global.dimension/3.0);
			case 2:
				return new Point(5.0*(double)Global.dimension/6.0,2.0*(double)Global.dimension/3.0);
			case 3:
				return new Point(5.0*(double)Global.dimension/6.0,(double)Global.dimension/3.0);
			default:
				return null;
		}
	
	}
	
	public static int getSide(Point p) {
		return getSide(p.x, p.y);
	}
	public static int getSide(double x, double y) {
		if (x < Global.dimension * 0.5)
			return 0;
		else if (x > Global.dimension * 0.5)
			return 1;
		else
			return 2;
	}
	
	// detect whether the player has achieved the requirement
	public static double density() {
			int nrats=0;
			for (int i = 0; i < Global.nrats; ++i) {
				if (getSide(Global.rats[i]) == 1)
					nrats++;
			}
			return (double)nrats/((double)Global.dimension*(double)Global.dimension/2.0);
	}
	
	public static Point movePiperTo(Point from, Point to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double speed=1.0;
        if(!Global.musicStatusCurrentPlayer){
        	speed=5.0;
        }
        double d = vectorLength(dx, dy);
        if (d <= speed * Global.TIMEUNIT)
            return to;
        else {
            double scale = (speed * Global.TIMEUNIT - Global.SMALLDISTANCE)/ d;
            double x = from.x + scale * dx;
            double y = from.y + scale * dy;
            
            if (x > Global.dimension)
                x = Global.dimension;
            if (y > Global.dimension)
                y = Global.dimension;
            
            return new Point(x, y);
        } 
    }
	
	public static Point moveQuickPiperTo(Point from, Point to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double speed=1.0;
        if(!Global.musicStatusCurrentPlayer){
        	speed=5.0;
        }
        double d = vectorLength(dx, dy);
        if (d <= speed * Global.TIMEUNIT)
            return to;
        else {
            double scale = (speed * Global.TIMEUNIT - Global.SMALLDISTANCE)/ d;
            double x = from.x + scale * dx;
            double y = from.y + scale * dy;
            
            if (x > Global.dimension)
                x = Global.dimension;
            if (y > Global.dimension)
                y = Global.dimension;
            
            return new Point(x, y);
        } 
    }
	
	
	 public static double vectorLength(double dx, double dy) {
	        return Math.sqrt(dx * dx + dy * dy);
	 }

	 public static double distance(Point a, Point b) {
	        return Math.sqrt((a.x-b.x) * (a.x-b.x) +
	                         (a.y-b.y) * (a.y-b.y));
	 }


	public static boolean closetoWall (Point current) {
			boolean wall = false;
			if (Math.abs(current.x-Global.dimension)<Player.pspeed) {
				wall = true;
			}
			if (Math.abs(current.y-Global.dimension)<Player.pspeed) {
				wall = true;
			}
			if (Math.abs(current.y)<Player.pspeed) {
				wall = true;
			}
			return wall;
		}
		

    // use geometry to compute whether the sheep is on the line defined by dog movement or not
    public static boolean onTheLine(Point rat, Point piper, Point lastRoundPiper) {
        double a = distance(rat, piper);
        double b = distance(rat, lastRoundPiper);
        double c = distance(piper, lastRoundPiper);
        double s = (a + b + c) / 2;
        double area = Math.sqrt(s*(s-a)*(s-b)*(s-c));
        double distance = 2 * area / a;
        if (distance <= Global.SMALLDISTANCE)
            return true;
        return false;
    }
    
    // pick a closest piper for a particular rat
    static Point getClosestPiper(int ratId, Point[] pipers, Point[] rats) {
        int minPiper = -1;
        double mindist = Double.MAX_VALUE;
        for (int i = 0; i < pipers.length; ++i) {
            double d = distance(rats[ratId], pipers[i]);
            if (d < mindist && d != 0) { // ignore overlapping piper
                mindist = d;
                minPiper = i;
            }
        }
        return pipers[minPiper];
    }
    
    // pick a closest piper for a particular piper
    static Point getClosestPiper(Point[] pipers, Point current) {
        int minPiper = -1;
        double mindist = Double.MAX_VALUE;
        for (int i = 0; i < pipers.length; ++i) {
            double d = distance(current, pipers[i]);
            if (d < mindist && d != 0) { // ignore overlapping piper
                mindist = d;
                minPiper = i;
            }
        }
        return pipers[minPiper];
    }

    // pick a closest piper for a particular rat
    static int getClosestPiperId(int ratId, Point[] pipers, Point[] rats) {
        int minPiper = -1;
        double mindist = Double.MAX_VALUE;
        for (int i = 0; i < pipers.length; ++i) {
            double d = distance(rats[ratId], pipers[i]);
            if (d < mindist && d != 0) { // ignore overlapping piper
                mindist = d;
                minPiper = i;
            }
        }
        return minPiper;
    }
    
    
    static boolean isRatUnderInfluence(int ratId, Point[] pipers,Point []rats) {
        for (int i = 0; i < pipers.length; ++i) {
            double d = distance(rats[ratId], pipers[i]);
            if (d <=10) { // ignore overlapping piper
               return true;
            }
        }
        return false;
    }
    
	public static Point greedySearch(Point[] pipers, Point[] rats,int id) {
		
				Point rat=getClosestRatNotLuredInTheRegion(pipers, rats, id);
				Point rtn=movePiperTo(pipers[id],rat);
				//Global.ratIsAssignedGreedy.put(pipers[id],rat);
				return rtn;
	}

/*	public static Point helperSearch(Point[] pipers, Point[] rats,int id,Boolean[] helper) {
		
	/*	Point rat=getClosestRatNotLuredInTheRegion(pipers, rats, id);
		Point rtn=movePiperTo(pipers[id],rat);
		//Global.ratIsAssignedGreedy.put(pipers[id],rat);
		return rtn;
		
		if(helper[id])
		{			
			//this.music = false;
			//Init.setMusicStatus(this.music);
			Point current=PlayUtilities.greedySearch(pipers,rats,id);
			Point rat=PlayUtilities.getClosestRatNotLuredInTheRegion(pipers, rats, id);
			if(PlayUtilities.distance(rat,current)<10)
			{
				return Point
				helper[id]=false;
				this.music = false;
				Init.setMusicStatus(this.music);
			}
		}
}*/
	//if all rats within 10m are within 4m at the same time
	public static	int isPipersWithinRadius(Point []pipers,Point current)
	{
		double dist = 0;
		for(int i = 0; i < pipers.length; i++){
				
				dist = PlayUtilities.distance(pipers[i], current);	
				//wanna know about the music status for pipers[i]
				if(dist < 6 && dist > 0) 
				{
					return i;
				}		
		}
		return -1;
	}
	
	public static Boolean isWithinRadius(Point current,Point[] rats)
	{
		double dist = 0;
		for(int i = 0; i < rats.length; i++){
				
				dist = PlayUtilities.distance(rats[i], current);
				//!!!!!!!!!!!!!!!!the number????
				// 7 left 9, 7.5 left 6 
				if(dist < 10 && dist > 7.5) 
				{
					return false;
				}
			
		}
		return true;
	}
	
	public static boolean isThereOtherPiper(Point[] pipers, int id) {
		// TODO Auto-generated method stub
		Point current=pipers[id];
		for(int i=0;i<pipers.length;i++){
			
			if(!pipers[i].equals(current)){
				if(distance(current,pipers[i])<=10){
					
						return true;
					
				}
			}
			
		}
		return false;
	}

	public static boolean isAssigned(Point rat){
		
		for(int i=0;i<Global.pipers.length;i++){
			
			if(Global.ratIsAssignedGreedy.containsKey(Global.pipers[i])&&((Point)Global.ratIsAssignedGreedy.get(Global.pipers[i])).equals(rat)){
				return true;
			}
		}
		
		return false;
	}
	
	public static Point getClosestRat(Point[]pipers, Point []rats, int id){
			Point piper=pipers[id];
			Point closest=null;
			double minDist=Double.MAX_VALUE;
			for(Point r:rats){
				if(minDist>distance(piper, r)&&distance(piper, r)>10.00){
					closest=r;
					minDist=distance(piper,r);
				}
			}
			return closest;
	}
	
	public static Point getClosestRatNotLuredInTheRegion(Point[]pipers, Point []rats, int id){
		Point piper=pipers[id];
		
		Point closest=null;
		double minDist=Double.MAX_VALUE;
		for(int i=0;i<rats.length;i++){
			Point r=rats[i];
			if(PlayUtilities.getSide(r)!=0&&isRatAndPiperAtSameRegion(id, r)&&minDist>distance(piper, r)&&distance(piper, r)>10.00){
				closest=r;
				minDist=distance(piper,r);
			}
		}
		
		if(closest==null){
			for(int i=0;i<rats.length;i++){
				Point r=rats[i];
				if(PlayUtilities.getSide(r)!=0&&!isRatUnderInfluence(i, pipers, rats)&&minDist>distance(piper, r)&&distance(piper, r)>10.00){
					closest=r;
					minDist=distance(piper,r);
				}
			}
			
			
		}
		if(closest==null){
			closest=new Point(Global.GATE.x,Global.GATE.y);
		}
		
		return closest;
   }
	
	public static Point toGate(Point current)
	{
		Point gate=new Point(Global.GATE.x,Global.GATE.y);
		double dist = PlayUtilities.distance(current, gate);
		assert dist > 0;
		double mpspeed = 0.09;
		double ox = (gate.x - current.x) / dist * mpspeed;
		double oy = (gate.y - current.y) / dist * mpspeed;
		current.x+=ox;
		current.y+=oy;
		return current;
	}
	
	
	public static boolean isRatAndPiperAtSameRegion(int id, Point rat){
		double angle=Math.toDegrees(Math.atan((rat.x-Global.GATE.x)/(rat.y-Global.GATE.y)));
		
		
		if(angle<0)
			angle*=-1;
		else
			angle=(180-angle);
		
	
		
		if(id==0&&angle>=Global.thetas[id]){
				return true;
		}else if(id==Global.npipers-1&&angle<Global.thetas[id-1]){
				return true;
		}else{
			if(id!=Global.npipers-1&&angle>=Global.thetas[id]&&angle<Global.thetas[id-1]){
				return true;
			}
			
		}
		
		return false;
	}

	
	public static void updateLuredRats(Point []rats, Point [] pipers){
		
			for(int i=0;i<rats.length;i++){
				if(distance(rats[i],getClosestPiper(i, pipers, rats))<=10){
					Global.isRatLured[i]=true;
				}
			}
		
	}
	
	public static int unLuredRatsNumber(Point []rats){
		int count = 0;
		for(int i=0;i<rats.length;i++){
				if(Global.isRatLured[i]) count++;
			}
		return rats.length-count;
		}
	

	public static void updateOldRatsAndPipers(int idPiper, Point[] rats) {
		// TODO Auto-generated method stub
		Global.oldPipers[idPiper]=Global.pipers[idPiper];
		Global.oldRats=rats;
	}
	
	public static void setIsPlaying(int id, boolean music)
	{	
		Global.isPlaying[id]=music;
		
	}
}	
