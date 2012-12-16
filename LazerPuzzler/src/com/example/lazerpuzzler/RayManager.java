/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.lazerpuzzler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

import com.example.lazerpuzzler.ReflectorManager.Reflector;
import com.example.lazerpuzzler.TargetManager.Target;

/**
 * 
 * @author 123ewall
 */
public class RayManager extends Observable implements Moveable
{

	/**
	 * 
	 */

	public static int raymanCount = 0;
	private static final int MOVE_RAY_SOURCE = 0;
	private static final int MOVE_RAY_ANGLE = 2;
	private static final int NO_ACTION = 3;

	public static final int MAX_RAND = 5;
	private static final int MAX_REFLECTIONS = 20;

	private static final int SOURCE_RADIUS = 30;

	static int fg = 0;

	ReflectorManager refMan;
	TargetManager targetMan;

	private View boundingRect;

	static private SourceRay selectedRay = null;
	private boolean cycleSelected = false;

	private int selectionNumber = 0;

	private static boolean edited = false;

	private static final RayManager instance = new RayManager();


	private RayManager()
	{

		raymanCount++;
	}

	public static RayManager getInstance()
	{
		return instance;
	}

	public synchronized void cycleSelection(boolean rotate, boolean sourceSelected)
	{

		int size = sourceRayList.size();
		if (size == 0)
			return;
		if (selectionNumber >= size)
		{

			selectionNumber = 0;

		}

		SourceRay r = sourceRayList.get(selectionNumber);

		selectionNumber++;

		if (sourceSelected)
		{
			r.setAction(MOVE_RAY_SOURCE);
		} else
		{
			r.setAction(MOVE_RAY_ANGLE);
		}

		cycleSelected = true;
	}

	public boolean deleteSelected()
	{
		if (selectedRay != null)
		{
			sourceRayList.remove(selectedRay);
			return true;
		} else
		{
			return false;
		}

	}

	abstract class Ray extends Observable

	{
		/**
		 * 
		 */

		public int rayType;

		Point source;
		Point p2 = new Point();
		double angle;

		ArrayList<Ray> rayList = null;
		private Ray reflectedRay = null;

		public int count;
		public int reflectedRayInd;
		
		int id =0;

		ReflectorManager.Reflector reflectedBy = null;
		ReflectorManager.Reflector refEnd = null;

		// Point closestP = null;
		ReflectorManager.Reflector refClosestPrev = null;

		

		public Ray(Point s, Point alongLine)
		{
			source = s;
		
			setAngle(alongLine);

		}

		private Ray(Point s, double angle)
		{
			source = s;
			// setAngle(alongLine);
			this.angle = angle;

		}
		
		public int getId()
		{
			return id;
		}

		@Override
		public String toString()
		{
			String by, ref;
			if (reflectedBy != null)
				by = Double.toString(reflectedBy.getAngle());
			else
				by = "null";

			if (refEnd != null)
				ref = Double.toString(refEnd.getAngle());
			else
				ref = "null";

			return "Ray at angle: " + angle + "created from " + by + "reflected by " + ref;
		}

	

		private void setReflectedRay(Point s, double ang, ReflectorManager.Reflector refBy, int id)
		{
			int x, y;
			p2 = s;

			y = (int) (50 * Math.sin(ang));
			x = (int) (50 * Math.cos(ang));

			if (rayList.size() <= MAX_REFLECTIONS)
			{

				if (reflectedRay == null)
				{

					reflectedRay = createReflectedRay(s, new Point(s.x + x, s.y + y), refBy,
							rayList, id);
					reflectedRayInd = rayList.indexOf(reflectedRay);

					// reflected = true;
				} else
				{
					reflectedRay.setPosition(s, new Point(s.x + x, s.y + y), refBy);
				}
			} else
			{
				if (reflectedRay != null)
					reflectedRay.setPosition(s, new Point(s.x + x, s.y + y), refBy);
			}

		}

		private void setPosition(Point s, Point alongLine, ReflectorManager.Reflector refBy)
		{
			source.x = s.x;
			source.y = s.y;
			// pointAlongLine = alongLine;
			setAngle(alongLine);
			// //System.out.println("ant 1 "+ reflectedBy.getAngle());

			reflectedBy = refBy;

			// Is this ray being moved by selected reflector
			/*if (reflectedBy == refMan.getSelectedReflector())
			{
				
				boolean contains = false;
				for(Point rp:reflectedBy.getReflectionPoints())
				{
					if (rp == source);
					contains = true;
				}
				
				if (contains == false)
					reflectedBy.addReflectionPoint(source);
				
			}*/
		}

		// AG this should be in a utilities class
		public boolean withinBounds(Point p)
		{
			int top, bottom, right, left;

			boolean inBounds;

			if (source.x >= p2.x)
			{

				right = source.x;
				left = p2.x;
			} else
			{
				right = p2.x;
				left = source.x;
			}

			if (source.y >= p2.y)
			{
				top = source.y;
				bottom = p2.y;
			} else
			{
				top = p2.y;
				bottom = source.y;
			}

			if ((p.x <= right + 3) && (p.x >= left - 3) && (p.y <= top + 3) && (p.y >= bottom - 3))
			{
				inBounds = true;

			} else
			{
				inBounds = false;
			}
			return inBounds;
		}

		public void checkReflectors(int id)
		{

			CopyOnWriteArrayList<Reflector> refList = refMan.getReflectorList();

			ReflectorManager.Reflector ref;
			Point p;
			Point closestP = null;

			ReflectorManager.Reflector refClosest = null;
			double closestDist = -1;

			for (int i = 0; i < refList.size(); i++)
			{
				// //System.out.println("reflector "+i+" getReflected");
				ref = refList.get(i);// .getReflected(this);

				if (ref != reflectedBy || reflectedBy == null)
				{
					p = ref.intersectsAt(this.getP1(), this.getNonRelectedP2());
					// Panel.p =p;
					if (p != null)
					{

						double x = Math.abs(p.x - source.x);
						double y = Math.abs(p.y - source.y);

						double c = /* (int)Math.round */(Math.sqrt((x * x) + (y * y)));

						if (closestDist == -1)
						{
							closestDist = c;
							refClosest = ref;
							closestP = p;
						} else if (c < closestDist)
						{
							closestDist = c;
							refClosest = ref;
							closestP = p;
						}

					}
				}
			}// end of for loop

			if ((refClosest != refClosestPrev) && (refClosestPrev != null))
			{
				deleteReflections();
			}
			refClosestPrev = refClosest;

			if (refClosest != null)
			{
				if (closestDist < 0.1)
				{
					deleteReflections();
					// refClosest = null;/*AG ??*/
					p2 = closestP;
					// reflected = false;
				} else
				{

					// //System.out.println("zxcvbnm "+refClosest.getAngle());

					double ang;

					if (closestP != null)
						ang = refClosest.getReflected(angle);
					else
						ang = -1;

					setReflectedRay(closestP, ang, refClosest, id);

					p2 = closestP;// refClosest.intersectsAt(this.getP1(),this.getNonRelectedP2());
					// reflected = true;
				}

			} else
			{
				deleteReflections();
				int length = 5000;
				p2 = new Point();
				p2.x = source.x + (int) (length * Math.cos(Math.toRadians(angle)));
				p2.y = source.y + (int) (length * Math.sin(Math.toRadians(angle)));
				// reflected = false;

			}
			refEnd = refClosest;

			/*************************/
			CopyOnWriteArrayList<Target> targetList = TargetManager.getTargetList();
			int radius = TargetManager.getInstance().getRadius();

			for (int z = 0; z < targetList.size(); z++)
			{

				// targetHit = false;

				TargetManager.Target target = targetList.get(z);

				if (withinBounds(target))
				{

					if (angle >= 88 && angle <= 92) // ||
					// (angle>=268 && angle<=272))
					{

						if ((source.x <= target.x + radius)
								&& (source.x >= target.x - radius)
								&& (source.y < target.y))
						{

							target.setHit(id);

							continue;

						} 
						
					} else if (angle >= 268 && angle <= 272)
					{
						if ((source.x <= target.x + radius)
								&& (source.x >= target.x - radius)
								&& (source.y > target.y))
						{

							target.setHit(id);

							continue;

						}

					}

					// int opp1 = target.y-source.y;

					int adj = target.x - source.x;

					int opp = (int) (adj * Math.tan(Math.toRadians(angle)));

					int yTarget = (source.y + opp);

					if ((target.y <= (yTarget + radius) && target.y >= (yTarget - TargetManager.getInstance().getRadius())))
					{

						target.setHit(id);

						// updateParent("TARGET_HIT");

					}

				} 

			}

			/***********************************************/
		}

		void deleteReflections()
		{

			if (reflectedRay != null)
			{
				int i = reflectedRayInd;
				ListIterator<Ray> it = rayList.listIterator(i);

				while (it.hasNext() == true)
				{
					ReflectedRay delRay = (ReflectedRay) it.next();
					delRay.reflectedBy.removeReflectionPoint(delRay.source);
					it.remove();

				}
				reflectedRay = null;

			}
		}


		public Point getP2()
		{
			return p2;
		}

		public Point getNonRelectedP2()
		{

			int length = 5000;
			p2 = new Point();
			p2.x = source.x + (int) (length * Math.cos(Math.toRadians(angle)));
			p2.y = source.y + (int) (length * Math.sin(Math.toRadians(angle)));

			return p2;

		}

		public Point getP1()
		{
			return source;
		}

		public void setPointAlongLine()
		{
		}

		public double getAngle()
		{
			return angle;
		}

		public ArrayList<Ray> getRayList()
		{
			return rayList;
		}

		final public void setAngle(Point pointAlongLine)
		{

			int y = pointAlongLine.y - source.y;
			int x = pointAlongLine.x - source.x;

			double ang;
			ang = Math.atan((double) y / x);
			ang = Math.toDegrees(ang);
			ang = Math.abs(ang);

			if (y >= 0 && x >= 0)
			{
				angle = ang;
				// //System.out.println("1st quad");
			} else if (y <= 0 && x >= 0)
			{
				angle = 360 - ang;
				// //System.out.println("4th quad");
			} else if (y <= 0 && x <= 0)
			{
				angle = ang + 180;
				// //System.out.println("3rd quad");
			} else
			{
				angle = 180 - ang;
				// //System.out.println("2nd quad");
			}

		}

	}// end of class ray

	class ReflectedRay extends Ray
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private ReflectedRay(Point s, Point alongLine, ArrayList<Ray> rayList, int id)
		{
			super(s, alongLine);
			
			this.id = id;

			this.rayList = rayList;

		}

	}

	public class SourceRay extends Ray implements Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 4209360273818925922L;

		private int action;

		private double[] randomValues = new double[MAX_RAND];

		int counter1 = 0;
		boolean reverse = false;

		private boolean posChanged = false;
		private Point prevSource = null;
	    private double prevAngle = 0;

		private SourceRay(Point s, double angle)
		{
			super(s, angle);

			rayList = new ArrayList<Ray>();

			;

			for (int i = 0; i < MAX_RAND; i++)
			{
				randomValues[i] = Math.random();
				// //System.out.println("rand : "+randomValues[i]);
			}
			prevSource = new Point(source);
			prevAngle = angle;
			posChanged = false;


		//	edited = true;

		}
		
		public void setId(int id)
		{
			this.id = id;
		}
		
		private void resetPosChanged()
		{
			posChanged = false;
			prevSource = new Point(source);
			prevAngle = angle;
		}

		public void updateRay(boolean createReflections)
		{

			if (createReflections)
			{

				targetMan.resetTargets();
				checkReflectorsForOneSource();
				// targetMan.updateTargets();
			} else
			{
				setRayWithNoReflect(100);
			}

			// edited = true;
			// <editor-fold defaultstate="collapsed" desc="comment">

			// </editor-fold>

		}

		public double[] getRandomValues()
		{
			return randomValues;
		}

		private void setAction(int action)
		{
			this.action = action;
			if (action == NO_ACTION)
			{

				if (selectedRay == this)
				{
					selectedRay = null;
				}

			} else
			{

				selectedRay = this;
			}

		}

		public boolean isSelected()
		{
			return (this == selectedRay);
		}

		public boolean positionChanged()
		{
			return posChanged;
		}
		
		
		public void checkReflectorsForOneSource()
		{
			for (int i = 0; i < rayList.size(); i++)
			{
				Ray r;
				r = rayList.get(i);
				r.checkReflectors(this.id);
			}

			updateParent(null);

		}

		public void deleteRay()
		{

			sourceRayList.remove(this);

		}

		private void setRayWithNoReflect(int length)
		{

			p2 = new Point();
			p2.x = source.x + (int) (length * Math.cos(Math.toRadians(angle)));
			p2.y = source.y + (int) (length * Math.sin(Math.toRadians(angle)));

		}

		public void remove()
		{
			sourceRayList.remove(this);
		}

		

		private void rayMousePressed(int x, int y, boolean deleteEnabled)
		{

			action = NO_ACTION;

			int adj = x - source.x;
			int opp;

			int yTarget;

			if ((x < (source.x + SOURCE_RADIUS)) && (x > (source.x - SOURCE_RADIUS))
					&& (y < (source.y + SOURCE_RADIUS)) && (y > (source.y - SOURCE_RADIUS)))
			{

				setAction(MOVE_RAY_SOURCE);

			} else
			{

				// adj = x-ray.getP1().x;

				if (angle > 89.0 && angle < 91.0)
				{
					if (adj < 10 && adj > -10 && y > source.y)
					{

						setAction(MOVE_RAY_ANGLE);

						// return this;
					}
				} else if (angle > 269.0 && angle < 271.0)
				{
					if (adj < 10 && adj > -10 && y < source.y)
					{
						action = MOVE_RAY_ANGLE;
						// System.out.println("MOVE_RAY_ANGLE");

						setAction(MOVE_RAY_ANGLE);

						// return this;
					}
				} else
				{
					opp = (int) (adj * Math.tan(Math.toRadians(angle)));

					yTarget = (source.y + opp);

					if (((angle > 90 && angle < 270) && adj < 0)
							|| ((angle > 270 || angle < 90) && adj > 0))
					{
						if (y < (yTarget + 20) && y > (yTarget - 20))
						{

							setAction(MOVE_RAY_ANGLE);

						}
					}

				}
			}

		}


	}// end of class sourceray

	public static final int SOURCE_RAY = 0;
	public static final int REFLECTED_RAY = 1;

	public ArrayList<SourceRay> sourceRayList = new ArrayList<SourceRay>();

	public int counterX = 0;

	public void createSourceRay(double angle, int axis, boolean createReflections)
	{

		// int col = (int)(Math.random()*5);

		/*
		 * int defaultColou; switch(col) { case 0: { defaultColou = Color.GREEN;
		 * break; } case 1: { defaultColou = Color.YELLOW; break; } case 2: {
		 * defaultColou = Color.CYAN; break; } default: { defaultColou =
		 * Color.BLUE; break; }
		 * 
		 * }
		 */

		int noOfXPoints;
		int noOfYPoints;

		if (axis > 0)
		{
			noOfXPoints = boundingRect.getWidth() / axis;
			noOfYPoints = boundingRect.getHeight() / axis;
		} else
		{
			noOfXPoints = 10;
			noOfYPoints = 10;

		}

		int xPoint = (sourceRayList.size() % (noOfXPoints - 2)) + 1;// (boundingRect.width()/axis);
		int yPoint = (sourceRayList.size() / (noOfXPoints - 2)) + 1;

		if (axis > 0)
		{
			if (yPoint < noOfYPoints)
			{
				createSourceRay(new SourceRay(new Point(xPoint * axis, yPoint * axis), 45),
						createReflections);
			} else
			{
				// System.exit(1);
			}
		} else
		{
			createSourceRay(new SourceRay(new Point(10, (10 * (sourceRayList.size() + 1)) + 100),
					angle), createReflections);
		}
// newly created ray, so the game has been edited
		edited = true;
	}

	public void loadSourceRay(Point p, double ang, boolean createReflections)
	{
		createSourceRay(new SourceRay(p, ang), createReflections);
		
		edited = false;

	}

	public void setSourceRayList(ArrayList<SourceRay> rl, boolean createReflections)
	{

		for (SourceRay sr : rl)
		{

			// sr.setParent(parent);
			if (sr != null)
				// ; // AG
				createSourceRay(sr, createReflections);
		}

	}

	private static Ray createSourceRay(SourceRay sRay, boolean createReflections)
	{

		// SourceRay ray = new SourceRay(s, alongLine);
		sRay.reflectedBy = null;
		sRay.rayType = SOURCE_RAY;
		/************************* AG Should not need this **********************/
		if (sRay.rayList != null)
			sRay.rayList = new ArrayList<RayManager.Ray>();

		sRay.rayList.add(sRay);

		instance.sourceRayList.add(sRay);
		
		sRay.setId(instance.sourceRayList.indexOf(sRay));

		sRay.count = RayManager.getInstance().counterX;

		instance.counterX++;
		// instance.counterY++;

		sRay.updateRay(createReflections);// checkReflectorsForOneSource();

		return sRay;
	}

	public void deleteAllRays()
	{
		// int i=0;

		// ListIterator<SourceRay> it = sourceRayList.listIterator(i);
		/*
		 * while(it.hasNext()) { it.next(); it.remove();
		 * 
		 * }
		 */

		sourceRayList.removeAll(sourceRayList);

	}

	public boolean isCycleSelected()
	{
		return cycleSelected;
	}

	public boolean rayMousePressed(MotionEvent evt, boolean deleteEnabled)
	{
		if (cycleSelected == false)
		{
			SourceRay sr;
			for (int i = 0; i < sourceRayList.size(); i++)
			{

				sr = sourceRayList.get(i);
				sr.rayMousePressed((int) evt.getX(), (int) evt.getY(), deleteEnabled);
				if (selectedRay != null)
					break;
			}

		}

		return (selectedRay != null);

	}

	public void rayMouseDragged(MotionEvent evt)
	{
		if (selectedRay == null)
		{
			// System.out.println("selectedray = null");
			return;
		}
		// int height = (int) rect.getHeight();

		int x = (int) evt.getX();
		int y = (int) /* height - */evt.getY();
		// improve this

		// p.translate(0, (height - p.y)-p.y);

		if (selectedRay.action == MOVE_RAY_SOURCE)
		{
			;

			if (x < boundingRect.getLeft())
				x = boundingRect.getLeft();
			else if (x > boundingRect.getRight())
				x = boundingRect.getRight();

			if (y < boundingRect.getTop())
				y = boundingRect.getTop();
			else if (y > boundingRect.getBottom())
				y = boundingRect.getBottom();

			selectedRay.source.x = x; // = p;
			selectedRay.source.y = y;
			selectedRay.updateRay(true);
			// repaint(); updateGui

		} else if (selectedRay.action == MOVE_RAY_ANGLE)
		{
			selectedRay.setAngle(new Point(x, y));
			selectedRay.updateRay(true);

		} else
		{
			System.err.println("unknown action");
		}

		updateParent(evt);
	}

	public void rayMouseReleased(MotionEvent evt, boolean createReflections, final int axis,
			double roundAngToNearest)
	{

		cycleSelected = false;
		if (selectedRay != null)
		{

			int x = (int) evt.getX();
			int y = (int) evt.getY();

			if (selectedRay.action == MOVE_RAY_SOURCE)
			{

				// System.out.println("x:"+x);
				// System.out.println("y:"+y);

				x = (int) (Math.round((x) / (0.5 * axis)) * (0.5 * axis));// +
				// (int)((0.5*axis)/2);
				y = (int) (Math.round((y) / (0.5 * axis)) * (0.5 * axis));//

				// System.out.println("x::::"+x);
				// System.out.println("y::::"+y);

				// evt.getPoint().translate(x, y);

				// Point p = evt.getPoint();
				selectedRay.source.x = x;
				selectedRay.source.y = y;

				// Reflector.parent.update(selectedRef,null);

				// selectedRef.midPoint.translate(100,0);
				// selectedTarget.setAction(NO_ACTION);
				// Reflector.parent.update(selectedRef,null);
			} else
			{
				int temp = (int) ((selectedRay.angle + (roundAngToNearest / 2)) / roundAngToNearest);
				selectedRay.angle = temp * roundAngToNearest;
			}

			selectedRay.posChanged = !(selectedRay.prevSource.equals(selectedRay.source)&&
				                    	(selectedRay.prevAngle == selectedRay.angle	));
			if (selectedRay.posChanged == true)
				edited = true;

			selectedRay.updateRay(createReflections);
			// System.out.println("relesaded");
			// updateParent(evt);

			selectedRay.setAction(NO_ACTION);
		}
	}

	public void resetAllPosChanged()
	{
		for (SourceRay s : sourceRayList)
			s.resetPosChanged();
	}

	public void rayMouseReleased(android.view.MotionEvent evt, boolean createReflections)
	{

		cycleSelected = false;
		if (selectedRay != null)
		{

			int x = (int) evt.getX();
			int y = (int) evt.getY();

			if (selectedRay.action == MOVE_RAY_SOURCE)
			{

				// System.out.println("x:"+x);
				// System.out.println("y:"+y);

				// evt.getPoint().translate(x, y);

				// Point p = evt.getPoint();
				selectedRay.source.x = x;
				selectedRay.source.y = y;
				selectedRay.updateRay(createReflections);
				// Reflector.parent.update(selectedRef,null);

				// selectedRef.midPoint.translate(100,0);
				// selectedTarget.setAction(NO_ACTION);
				// Reflector.parent.update(selectedRef,null);
			}
			// System.out.println("relesaded");

			selectedRay.setAction(NO_ACTION);
		}
	}

	public SourceRay getSelectedRay()
	{
		return selectedRay;
	}

	static Ray createReflectedRay(Point s, Point alongLine, ReflectorManager.Reflector refBy,
			ArrayList<Ray> rayList, int id)
	{

		Ray ray = instance.new ReflectedRay(s, alongLine, rayList, id);
		ray.reflectedBy = refBy;
		ray.rayType = REFLECTED_RAY;

		rayList.add(ray);

		ray.count = RayManager.getInstance().counterX;
		// System.out.println("Ray: "+ray.count+" created" );

		instance.counterX++;
		// instance.counterY++;
		
		refBy.addReflectionPoint(ray.source);

		return ray;
	}

	public void setTargetManager(TargetManager targetMan)
	{
		this.targetMan = targetMan;
	}

	public void setReflectorManager(ReflectorManager refMan)
	{
		this.refMan = refMan;
	}

	public ArrayList<SourceRay> getSourceRayList()
	{
		return sourceRayList;
	}

	public void updateParent(Object o)
	{
		setChanged();
		notifyObservers(o);
	}

	public void setEdited(boolean edited)
	{
		RayManager.edited = edited;
	}

	public boolean isEdited()
	{
		return edited;
	}

	void checkReflectorsForAllSources()
	{

		System.out.print("checkReflectorsForAllSources() " + fg++);
		targetMan.resetTargets();

		for (SourceRay r : getSourceRayList())
		{

			r.checkReflectorsForOneSource();
		}

		updateParent(this);
	}

	

	public void setBoundingRect(View boundingRect)
	{
		this.boundingRect = boundingRect;

	}

}
