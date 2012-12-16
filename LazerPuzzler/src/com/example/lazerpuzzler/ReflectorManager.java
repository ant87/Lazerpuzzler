/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.lazerpuzzler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.graphics.Point;
import android.os.Parcel;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * @author 123ewall
 */
public class ReflectorManager implements Moveable, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */


	static int refManCount = 0;

	private int selectionNumber = 0;

	private boolean cycleSelected = false;

	private static final ReflectorManager instance = new ReflectorManager();

	private  boolean edited = false;

	private  View boundingRect = null;

	private ReflectorManager()
	{
		refManCount++;
	}

	public static ReflectorManager getInstance()
	{
		return instance;
	}

	public void setBoundingRect(View boundingRect)
	{
		this.boundingRect = boundingRect;
	}

	class Reflector implements Serializable, Comparable<Reflector>
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public  int count;

		 int action = NO_ACTION;
		private  int xMouseDiff = 0;
		private  int yMouseDiff = 0;

		Point p1, p2;
		// boolean reflectedRay = false;

		private double angle;

		private Point midPoint = new Point();
		private Point snapPoint = new Point();

		public int length;

		public  double[] randomValues = new double[MAX_RAND];

		public  int counter1 = 0;

		public  boolean reverse;

	    private Point prevPoint = null;
	    private double prevAngle = 0;
	    
		 boolean posChanged = false;

		private  ArrayList<Point> reflectionPoints = new ArrayList<Point>();

		// Rect rectangle = new Rect();

		private Reflector(Point mid, double ang, int length)
		{
			double opp, adj;

			angle = ang;
			this.midPoint = mid;
			this.length = length;
// AG fix this pointless as prevpoint will always be null at this point
			prevPoint = new Point(midPoint);
			prevAngle = angle;

			double angleRad = Math.toRadians(angle);

			opp = (length) * Math.sin(angleRad);
			adj = (length) * Math.cos(angleRad);
			
			p1  = new Point(midPoint.x + (int)( adj/2), midPoint.y + (int) (opp/2));

			p2 = new Point(midPoint.x - (int) (adj/2), midPoint.y - (int) (opp/2));

			// int diffY = Math.abs(p2.y-p1.y);
			// int diffX = Math.abs(p2.x-p1.x);

			// length = (int)Math.sqrt((diffY*diffY)+ (diffX*diffX));

			// int midPointY = p1.y+((p2.y-p1.y)/2);
			// int midPointX = p1.x+((p2.x-p1.x)/2);
			// midPoint = new Point(midPointX,midPointY);
			//midPoint = new Point((int) (p1.x + (adj / 2)), (int) (p1.y + (opp / 2)));
			snapPoint = new Point(midPoint);

		}

		private Reflector()
		{
			double opp, adj;

			p1 = new Point((int) (Math.random() * 1000) + length, (int) (Math.random() * 700)
					+ length);

			angle = (Math.random() * 180);

			length = (int) (Math.random() * 200);
			// this.p1 = p1;
			this.length = 200;

			prevPoint = new Point(midPoint);
			prevAngle = angle;
			posChanged = false;

			double angleRad = Math.toRadians(angle);

			opp = (length) * Math.sin(angleRad);
			adj = (length) * Math.cos(angleRad);

			p2 = new Point(p1.x + (int) adj, p1.y + (int) opp);

			midPoint = new Point((int) (p1.x + (adj / 2)), (int) (p1.y + (opp / 2)));

			for (int i = 0; i < MAX_RAND; i++)
			{
				randomValues[i] = Math.random();
				// //System.out.println("rand "+ randomValues[i]);
			}

			int bool = (int) Math.round(Math.random());
			if (bool == 0)
				reverse = false;
			else
				reverse = true;

		}

		public Point getMidPoint()
		{
			return midPoint;
		}

		@Override
		public Object clone()
		{
			Object cl = null;
			try
			{
				cl = super.clone();
			} catch (CloneNotSupportedException ex)
			{
				Logger.getLogger(Reflector.class.getName()).log(Level.SEVERE, null, ex);
			}

			return cl;
		}

		
		private void snapToGrid(int axis)
		{

			// x =(int) (Math.round( (x+(axis/2))/ axis )*axis);

			if (reflectionPoints.size() !=1)
			{
				snapPoint.x = (int) (Math.round((midPoint.x) / (0.5 * axis)) * (0.5 * axis));// +
																								// (int)((0.5*axis)/2);
				snapPoint.y = (int) (Math.round((midPoint.y) / (0.5 * axis)) * (0.5 * axis));//
			} else
			{
				snapPoint.x = (int) (Math.round((reflectionPoints.get(0).x) / (0.5 * axis)) * (0.5 * axis));// +
																									// (int)((0.5*axis)/2);
				snapPoint.y = (int) (Math.round((reflectionPoints.get(0).y) / (0.5 * axis)) * (0.5 * axis));//
			}
		
		

			setPositionMid(this.snapPoint);
		}

		public ArrayList<Point> getReflectionPoints()
		{
			return reflectionPoints;
		}

		public void addReflectionPoint(Point refPoint)
		{
			reflectionPoints.add(refPoint);
		}
		
		public void removeReflectionPoint(Point refPoint)
		{
		/*	for(int i=0; i<reflectionPoints.size();i++)
			{
				if(reflectionPoints.get(i) == refPoint)
				{
				
					reflectionPoints.remove(i);
					return;
				}
			}*/
			
		reflectionPoints.remove(refPoint);
		
		}

		public boolean isSelected()
		{
			return selectedRef == this;
		}

		public int getAction()
		{
			return action;
		}

		private void setAction(int action)
		{
			this.action = action;

			if (action == NO_ACTION)
			{

				System.out.println("selectedRef = no action");

				if (selectedRef == this)
				{
					selectedRef = null;
				}
			} else
			{
				System.out.println("selectedRef = this");
				if (selectedRef != null)
					selectedRef.action = NO_ACTION;
				selectedRef = this;
			}

		}

		public double getAngle()
		{
			return angle;
		}

		/*
		 * public double getAngle360() { return angle360; }
		 */

		public void setAngle()
		{
			int y = p2.y - p1.y;
			int x = p2.x - p1.x;

			setAngle(x, y);
		}

		public void setAngle(double ang)
		{
			angle = ang;

			int adj = (int) ((length / 2) * Math.cos(Math.toRadians(angle)));
			int opp = (int) ((length / 2) * Math.sin(Math.toRadians(angle)));

			p1.x = midPoint.x + adj;
			p1.y = midPoint.y + opp;

			p2.x = midPoint.x - adj;
			p2.y = midPoint.y - opp;
		}

		private void setAngle(int x, int y)
		{

			double ang;
			ang = Math.atan((double) y / x);
			ang = Math.toDegrees(ang);
			ang = Math.abs(ang);

			if (y >= 0 && x >= 0)
			{
				// angle360 = ang;
				angle = ang;
				// //System.out.println("1st quad");
			} else if (y <= 0 && x >= 0)
			{
				// angle360 = 360 - ang;
				angle = 180 - ang;
				// //System.out.println("4th quad");
			} else if (y <= 0 && x <= 0)
			{
				// angle360 = ang + 180;
				angle = ang;
				// //System.out.println("3rd quad");
			} else
			{
				// angle360 = 180 - ang;
				angle = 180 - ang;
				// //System.out.println("2nd quad");
			}

		}

		public void setPoints(Point p1, Point p2)
		{
			this.p1 = p1;
			this.p2 = p2;
			setAngle();
			// setAngle360();
		}

		public void setPoint1(int x, int y)
		{

			rotateP1(x, y);
			// rotateVarLengthP1(p1);
		}

		public void setPoint2(int x, int y)
		{
			rotateP2(x, y);
			// rotateVarLengthP2(p2);

		}

		public void rotateVarLengthP2(Point p2)
		{
			this.p2 = new Point(p2);

			int tempX = midPoint.x - p2.x;
			int tempY = midPoint.y - p2.y;
			p1.x = midPoint.x + tempX;
			p1.y = midPoint.y + tempY;
			setAngle();
		}

		public void rotateVarLengthP1(Point p1)
		{
			this.p1 = new Point(p1);

			int tempX = midPoint.x - p1.x;
			int tempY = midPoint.y - p1.y;
			p2.x = midPoint.x + tempX;
			p2.y = midPoint.y + tempY;
			setAngle();
		}

		private void rotateP1(int x, int y)
		{
			y = midPoint.y - y;
			x = midPoint.x - x;

			setAngle(x, y);

			double opp = (length / 2) * Math.sin(Math.toRadians(angle));
			double adj = (length / 2) * Math.cos(Math.toRadians(angle));

			this.p1.x = (int) (midPoint.x - adj);
			this.p1.y = (int) (midPoint.y - opp);

			this.p2.x = (int) (midPoint.x + adj);
			this.p2.y = (int) (midPoint.y + opp);

		}
		
		public int getLength()
		{
			return length;
		}

		private void rotateP2(int x, int y)
		{

			y = midPoint.y - y;
			x = midPoint.x - x;

			setAngle(x, y);
			// ang = angle360;

			// angle360 = 180 + ang;

			double opp = (length / 2) * Math.sin(Math.toRadians(angle));
			double adj = (length / 2) * Math.cos(Math.toRadians(angle));

			this.p2.x = (int) (midPoint.x - adj);
			this.p2.y = (int) (midPoint.y - opp);

			this.p1.x = (int) (midPoint.x + adj);
			this.p1.y = (int) (midPoint.y + opp);

		}

		public Point getPoint1()
		{
			return p1;
		}

		public Point getPoint2()
		{
			return p2;
		}

		public Point intersectsAt(Point a, Point b)
		{
			// This Line
			double a1 = (p2.y - p1.y);
			double b1 = (p1.x - p2.x);
			double c1 = (p2.x * p1.y - p1.x * p2.y);

			// Line to test against
			double a2 = (b.y - a.y);
			double b2 = (a.x - b.x);
			double c2 = (b.x * a.y - a.x * b.y);

			double denom = a1 * b2 - a2 * b1;

			// Check for parallel lines
			if (denom == 0)
			{
				return null;
			}

			// //System.out.println("denom != null");

			// Get the intersection point
			double intersectX = ((b1 * c2 - b2 * c1) / denom);
			double intersectY = ((a2 * c1 - a1 * c2) / denom);

			// return intersect;
			// InBoundingBox tests to see if the point is in the bounding box
			// for
			// a line segment. The point must be in both lines' bounding boxes
			// to
			// register and intersection.

			int top, bottom, right, left;

			if (p2.y >= p1.y)
			{
				top = p2.y;
				bottom = p1.y;
			} else
			{
				top = p1.y;
				bottom = p2.y;
			}

			if (p2.x >= p1.x)
			{

				right = p2.x;
				left = p1.x;
			} else
			{
				right = p1.x;
				left = p2.x;
			}

			boolean intersect1 = false;
			boolean intersect2 = false;
			// //System.out.println("Ref at angle : "+angle+" intersect  "+
			// intersect.x+" "+intersect.y);

			if ((intersectX <= (right)) && (intersectX >= (left)) && (intersectY <= (top))
					&& (intersectY >= (bottom)))
			{
				intersect1 = true;

			}

			if (b.x >= a.x)
			{

				right = b.x;
				left = a.x;
			} else
			{
				right = a.x;
				left = b.x;
			}

			if (b.y >= a.y)
			{
				top = b.y;
				bottom = a.y;
			} else
			{
				top = a.y;
				bottom = b.y;
			}

			if ((intersectX <= right) && (intersectX >= left) && (intersectY <= top)
					&& (intersectY >= bottom))
			{
				intersect2 = true;
				// //System.out.println("intersect within ray bound box");
				// return intersect;
			} else
			{
				// intersect2 = true;
				// //System.out.println("intersect NOT in ray bound box");

			}

			if ((intersect1 == true) && (intersect2 == true))
			{
				return new Point((int) Math.round(intersectX), (int) Math.round(intersectY));
			}

			return null;
		}

		public void setPositionP1(Point newP1)
		{
			int ydiff = p2.y - p1.y;
			int xdiff = p2.x - p1.x;
			this.p1 = new Point(newP1);
			p2.x = p1.x + xdiff;
			p2.y = p1.y + ydiff;

			double angold = angle;
			setAngle();
			assert (angold == angle);

			int midPointY = p1.y + ((p2.y - p1.y) / 2);
			int midPointX = p1.x + ((p2.x - p1.x) / 2);
			midPoint = new Point(midPointX, midPointY);

		}

		public void setPositionMid(Point newMid)
		{
			midPoint = newMid;
			// System.out.println("midpoint "+ midPoint.x+" "+ midPoint.y);
			p1.x = midPoint.x + (int) ((length / 2) * Math.cos(Math.toRadians(angle)));
			p1.y = midPoint.y + (int) ((length / 2) * Math.sin(Math.toRadians(angle)));

			p2.x = midPoint.x - (int) ((length / 2) * Math.cos(Math.toRadians(angle)));
			p2.y = midPoint.y - (int) ((length / 2) * Math.sin(Math.toRadians(angle)));

		}

		public boolean withinBoundingCircle(Point p)
		{
			int a = Math.abs(p.x - midPoint.x);
			int b = Math.abs(p.y - midPoint.y);

			return Math.sqrt((a * a) + (b * b)) < length / 2;
		}

		public boolean withinBounds(Point p)
		{
			int top, bottom, right, left;

			boolean inBounds;

			if (p1.x >= p2.x)
			{

				right = p1.x;
				left = p2.x;
			} else
			{
				right = p2.x;
				left = p1.x;
			}

			if (p1.y >= p2.y)
			{
				top = p1.y;
				bottom = p2.y;
			} else
			{
				top = p2.y;
				bottom = p1.y;
			}

			if ((p.x <= right + 10) && (p.x >= left - 10) && (p.y <= top + 10)
					&& (p.y >= bottom - 10))
			{
				inBounds = true;

			} else
			{
				inBounds = false;
			}

			return inBounds;
		}

		public double getReflected(double ang)
		{

			double inc, norm;

			ang = ang + 90;

			inc = angle - ang;

			norm = angle + 90;

			ang = Math.toRadians(norm + inc);

			return ang;
		}

		private void resetPosChanged()
		{
			posChanged = false;
			prevPoint = new Point(midPoint);
			prevAngle = angle;
		}

		public Point getSnapPoint()
		{
			return snapPoint;
		}

		public void deleteReflector()
		{
			reflectorList.remove(this);
		}

		private void androidReflectorMousePressed1(int x, int y, boolean rotate,
				boolean rotateEnabled)
		{

			setAction(NO_ACTION);

			if (withinBoundingCircle(new Point(x, y)))
			{

				if (rotate && rotateEnabled)
				{
					setAction(MOVE_P1);
					System.out.println("set action move p1");
				} else
				{
					setAction(MOVE_REFLECTOR);
					System.out.println("set action move ref");
					xMouseDiff = x - midPoint.x;
					yMouseDiff = y - midPoint.y;

				}

			}

		}

		private void reflectorMousePressed1(int x, int y, boolean button1, boolean rotateEnabled,
				boolean deleteEnabled)
		{

			setAction(NO_ACTION);

			// System.out.println("p1.x "+ p1.x+ " p1.y "+ p1.y);
			// System.out.println(" x "+ x+ " y "+ y);
			// System.out.println(" button1 :  "+ button1);

			if (x < (p1.x + 10) && x > (p1.x - 10) && y < ((p1.y) + 10) && y > ((p1.y) - 10))
			{
				if (button1)
				{
					if (rotateEnabled)
					{
						setAction(MOVE_P1);
						System.out.println("set action move p1");
					} else
					{
						setAction(MOVE_REFLECTOR);
						System.out.println("set action move ref");

					}
				} else
				{
					setAction(SHOW_REFLECTOR_MENU);
				}

				return;
			}
			if (x < (p2.x + 10) && x > (p2.x - 10) && y < ((p2.y) + 10) && y > ((p2.y) - 10))
			{
				if (button1)
				{
					if (rotateEnabled)
					{
						setAction(MOVE_P2);
						System.out.println("set action move p2");
					} else
					{
						setAction(MOVE_REFLECTOR);
						System.out.println("set action move ref 2");
					}
				} else
				{
					setAction(SHOW_REFLECTOR_MENU);
				}

				return;
			}

			int adj = x - p1.x;

			if (withinBounds(new Point(x, y)))
			{

				if (angle > 89.0 && angle < 91.0)
				{
					// System.out.println("in1...adj "+adj+"y "+y+" p1.y"+p1.y);
					// if(adj<5 && adj >-5) // AG ??
					{

						if (button1)
						{

							setAction(MOVE_REFLECTOR);
							System.out.println("MOVE_REFLECTOR 3");
							xMouseDiff = x - midPoint.x;
							yMouseDiff = y - midPoint.y;

							// System.out.println("x mouse1d "+xMouseDiff);
						} else
						{
							setAction(SHOW_REFLECTOR_MENU);
							if (deleteEnabled)
							{
								deleteReflector();
							}
						}

						// repaint();
						return;

					}
				}

				else
				{
					int opp = (int) (adj * Math.tan(Math.toRadians(angle)));

					int yTarget = (p1.y + opp);
					// yTarget = (int)rect.getHeight() - yTarget;
					// //System.out.println("yTarget "+yTarget);
					// //System.out.println("y "+y);

					if (y < (yTarget + 10) && y > (yTarget - 10))
					{
						if (button1)
						{
							// action = MOVE_REFLECTOR;
							setAction(MOVE_REFLECTOR);
							System.out.println("MOVE_REFLECTOR 4");

							xMouseDiff = x - midPoint.x;
							yMouseDiff = y - midPoint.y;

							// System.out.println("x mouse1d "+xMouseDiff);
						} else
						{
							setAction(SHOW_REFLECTOR_MENU);
							if (deleteEnabled)
							{
								deleteReflector();
							}
						}

						// repaint();
						return;
					}

				}

			}

		}

		public boolean positionChanged()
		{
			return posChanged;
		}

		public int describeContents()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		public void writeToParcel(Parcel dest, int flags)
		{

			dest.writeParcelable(p1, flags);
			dest.writeParcelable(p2, flags);

		}

		public int compareTo(Reflector another)
		{
			if(this.midPoint.x < another.midPoint.x)
				return -1;
			else if(this.midPoint.x == another.midPoint.x)
			    return 1;
			else
				return 0;
		}

		

	}

	/* end of class reflector */

	public static final int NO_ACTION = 0;
	public static final int MOVE_P1 = 1;
	public static final int MOVE_P2 = 2;
	public static final int SHOW_REFLECTOR_MENU = 3;
	public static final int MOVE_REFLECTOR = 4;

	// private Rectangle rect;

	static int counter = 0;

	private  static CopyOnWriteArrayList<Reflector> reflectorList = new CopyOnWriteArrayList<Reflector>();
	// private Point intersect = new Point();

	// protected double angle360;

	static private Reflector selectedRef;

	public static final int MAX_RAND = 5;

	private boolean simOn = false;

	Timer timer;

	public boolean delete = false;

	public void reflectorMousePressed(MotionEvent evt, boolean rotationEnabled,
			boolean deleteEnabled)
	{

		int x = (int) evt.getX();
		int y = (int) evt.getY();
		// boolean button1 = (evt.getButton() == MotionEvent.BUTTON1);

		Reflector ref;
		for (int i = 0; i < reflectorList.size(); i++)
		{
			ref = (reflectorList.get(i));
			ref.reflectorMousePressed1(x, y, true, rotationEnabled, deleteEnabled);
		}
	}

	public synchronized void cycleSelection(boolean rotate)
	{

		int size = reflectorList.size();
		
		if (size == 0)
			return;
		if (selectionNumber >= size)
		{

			selectionNumber = 0;

		}
		Reflector[] rArray = new Reflector[0];
		rArray = reflectorList.toArray(rArray);
		
		Arrays.sort(rArray);
		
		reflectorList.clear();
		reflectorList.addAll(Arrays.asList((Reflector[])rArray));

		Reflector r = reflectorList.get(selectionNumber);

		selectionNumber++;
		if (rotate)
			r.setAction(ReflectorManager.MOVE_P1);
		else
			r.setAction(ReflectorManager.MOVE_REFLECTOR);

		cycleSelected = true;
	}

	public boolean androidReflectorMousePressed(MotionEvent evt, boolean rotate,
			boolean rotationEnabled)
	{

		int x = (int) evt.getX();
		int y = (int) evt.getY();
	
		if (cycleSelected == true)
		{

			selectedRef.xMouseDiff = x - selectedRef.midPoint.x;
			selectedRef.yMouseDiff = y - selectedRef.midPoint.y;

		} else
		{
			for (Reflector ref : reflectorList)
			{

				ref.androidReflectorMousePressed1(x, y, rotate, rotationEnabled);
				if (selectedRef !=null)
					break;
			}
		}

		return selectedRef != null;
	}

	public boolean reflectorMouseDragged(MotionEvent evt, final int axis)
	{
		if (selectedRef == null)

			return false;

		int x, y;
		int halfAxis = axis/2;

		x = (int) evt.getX();
		y = (int) evt.getY();

		Point oldMidPoint = new Point();
		oldMidPoint.x = selectedRef.midPoint.x;
		oldMidPoint.y = selectedRef.midPoint.y;
		// improve this
		// Point p = new Point(x, height - y);

		if (selectedRef.action == MOVE_P1)
		{

			selectedRef.setPoint1(x, y);
			// updateParent();
			// checkReflectorsForAllSources();
			// repaint();
			return true;

		}

		if (selectedRef.action == MOVE_P2)
		{

			selectedRef.setPoint2(x, y);
			// updateParent();
			// repaint();
			return true;

		}

		if (selectedRef.action == MOVE_REFLECTOR)
		{

			// Rect r = LightModel.rectangle;

			int newX = x - selectedRef.xMouseDiff;

			int newY = y - selectedRef.yMouseDiff;

			if (newX < boundingRect.getLeft()+halfAxis)
				newX = boundingRect.getLeft()+halfAxis;
			else if (newX > boundingRect.getRight()-halfAxis)
				newX = boundingRect.getRight()-halfAxis;

			if (newY < boundingRect.getTop()+halfAxis)
				newY = boundingRect.getTop()+halfAxis;
			else if (newY > boundingRect.getBottom()-10)
				newY = boundingRect.getBottom()-10;

			selectedRef.setPositionMid(new Point(newX, newY));

			// if(axis!=0)
			// selectedRef.snapToGrid(axis);

			// check if reflector has moved
			if (oldMidPoint.equals(selectedRef.midPoint) == false)
			{

				if (selectedRef.reflectionPoints.size() != 1)
				{
					selectedRef.snapPoint.x = (int) (Math.round((selectedRef.midPoint.x)
							/ (0.5 * axis)) * (0.5 * axis));// +
															// (int)((0.5*axis)/2);
					selectedRef.snapPoint.y = (int) (Math.round((selectedRef.midPoint.y)
							/ (0.5 * axis)) * (0.5 * axis));//
				} else
				{
					selectedRef.snapPoint.x = (int) (Math.round((selectedRef.reflectionPoints.get(0).x)
							/ (0.5 * axis)) * (0.5 * axis));// +
															// (int)((0.5*axis)/2);
					selectedRef.snapPoint.y = (int) (Math.round((selectedRef.reflectionPoints.get(0).y)
							/ (0.5 * axis)) * (0.5 * axis));//
				}

				return true;
			} else
			{
				return false;
			}

		}
		return false;

	}

	public void reflectorMouseReleased(MotionEvent evt, final int axis, double roundToNearest)
	{

		cycleSelected = false;
		if (selectedRef != null)
		{
			assert (selectedRef.action == ReflectorManager.NO_ACTION);

			// ReflectorManager.parent.update(selectedRef, y);

			if (selectedRef.action != MOVE_REFLECTOR)
			{
				int temp = (int) ((selectedRef.angle + (roundToNearest / 2)) / roundToNearest);
				selectedRef.setAngle(temp * roundToNearest);
			}
			// AG improve conditional stuff
			
			selectedRef.snapToGrid(axis);
			selectedRef.posChanged = !((selectedRef.prevPoint.equals(selectedRef.midPoint)) &&
				                          	(selectedRef.prevAngle == selectedRef.angle	));
			
			
			if (selectedRef.posChanged == true)
			{
				edited = true;

				

			}

		//	System.out.println("prev " + selectedRef.prevPoint + " mid " + selectedRef.midPoint);

			// RayManager.getInstance().checkReflectorsForAllSources();

			// selectedRef.midPoint.translate(100,0);
			selectedRef.setAction(NO_ACTION);
			// ReflectorManager.parent.update(selectedRef,null);
			// System.out.println("relesaded");

			// repaint();
			return;

		}

	}

	public CopyOnWriteArrayList<Reflector> getReflectorList()
	{
		return reflectorList;
	}
	
	public static void createReflector( Point mid, double ang, int axis)
	{
		// Reflector ref = new Reflector(p1,p2);
		Reflector ref = ReflectorManager.getInstance().new Reflector(mid, ang, (int) Math.sqrt(2 * (axis * axis)));

		reflectorList.add(ref);
		ref.count = counter;
		counter++;

		// RayManager.getInstance().checkReflectorsForAllSources();

	}

	

	
	/*
	 * public Rectangle getSurfacRect() { return rect; }
	 */

	public void deleteAllReflectors()
	{
		reflectorList.removeAll(reflectorList);

	}

	public void resetAllPosChanged()
	{
		for (Reflector r : reflectorList)
			r.resetPosChanged();
	}

	Reflector getSelectedReflector()
	{
		System.out.println("selected ref = " + selectedRef);
		return selectedRef;
	}

	public void reflectorMouseReleased(MotionEvent evt)
	{
		cycleSelected = false;
		if (selectedRef != null)
		{
			assert (selectedRef.action == ReflectorManager.NO_ACTION);

			selectedRef.setAction(NO_ACTION);

		}

	}

	public boolean isCycleSelected()
	{
		return cycleSelected;
	}

	public void setEdited(boolean edited)
	{
		this.edited = edited;
	}

	public boolean isEdited()
	{
		return edited;
	}

	public boolean deleteSelected()
	{
		if (selectedRef != null)
		{
			reflectorList.remove(selectedRef);
			return true;
		} else
		{
			return false;
		}
	}

	
	

	

}
