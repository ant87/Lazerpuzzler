/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.lazerpuzzler;

import java.io.Serializable;
import java.util.Observable;
import java.util.concurrent.CopyOnWriteArrayList;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * @author Owner
 */
public class TargetManager implements Moveable
{

	/**
	 * 
	 */
	
	public static final int MAX_ID = 5;
	
	static int targetmanCount =0;
	
	private static final long serialVersionUID = 1L;

	private static final TargetManager instance = new TargetManager();

	private int selectionNumber = 0;

	private boolean cycleSelected = false;
//AG  Add stuff to do with bounding rect
	private transient View boundingRect;

	private TargetManager()
	{
		targetmanCount++;
	}

	public static TargetManager getInstance()
	{
		return instance;
	}

	public boolean isCycleSelected()
	{
		return cycleSelected;
	}
	
	public boolean isEdited()
	{
		//AG implement this
		return false;
	}

	public synchronized void cycleSelection()
	{

		int size = targetList.size();
		if (size == 0)
			return;
		if (selectionNumber >= size)
		{

			selectionNumber = 0;

		}

		selectedTarget = targetList.get(selectionNumber);

		selectionNumber++;

		cycleSelected = true;
	}

	public boolean deleteSelected()
	{

		if (selectedTarget != null)
		{

			targetList.remove(selectedTarget);
			return true;
		} else
		{
			return false;
		}

	}

	class Target extends Point
	{
		/**
		 * 
		 */

		private boolean hit;
		boolean posChanged = false;
		private int id =-1;

		Target(int x, int y)
		{
			super(x, y);
		}
		
		Target(int x, int y, int id)
		{
			super(x, y);
			this.id = id;
		}
		
		
// AG todo : implement the changed stuff
		public boolean positionChanged()
		{
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void setId(int id)
		{
			if (id> MAX_ID)
				id = -1;
			this.id = id;
		}
		
		public int getId()
		{
return id;		
}
		
		public boolean isSelected()
		{

			return selectedTarget == this;
		}

		public boolean isHit()
		{
			return hit;
		}

		public void setHit(int rayId)
		{
			if((id == -1) ||(rayId == id))
			{
				hit = true;
			}
			
		}
		
		public void setHit()
		{
			hit = true;
		}

	}

	static private Target selectedTarget;
	
	//AG  should this be static?
	private static CopyOnWriteArrayList<Target> targetList = new CopyOnWriteArrayList<Target>();

	private int radius = 12;


	public static void createTarget(int x, int y)
	{
		targetList.add(instance.new Target(x,y));
	}
	
	public static void createTarget(int x, int y, int id)
	{
		targetList.add(instance.new Target(x,y, id));
	}

	public static CopyOnWriteArrayList<Target> getTargetList()
	{
		return targetList;
	}
	
	public int getRadius()
	{
		return radius;
	}
	
	public void setRadius(int radius)
	{
		this.radius = radius;
	}


	/*public void updateParent(Object o)
	{
		setChanged();
		notifyObservers(o);
	}*/

	public boolean isAllHit()
	{
		if (targetList.size() == 0)
			return false;
		boolean tempAllhit = true;
		for (Target t : targetList)
		{
			if (t.hit == false)
				tempAllhit = false;
		}

		return tempAllhit;
	}

	public void deleteAllTargets()
	{
		for(Target t :targetList)
		{
			if(t == null)
				System.out.println("asd null");
			else
				System.out.println("naaaiii null");
				
		}
					
		targetList.removeAll(targetList);

	}

	public void resetTargets()
	{

		for (Target t : targetList)
		{
			t.hit = false;
		}

	}

	public void targetMouseDragged(MotionEvent evt)
	{

		if (selectedTarget == null)
		{
			return;
		}

		// Point p = evt.getPoint();
		selectedTarget.x = (int) evt.getX();
		selectedTarget.y = (int) evt.getY();

	}

	Target getSelectedTarget()
	{
		return selectedTarget;
	}

	boolean targetMousePressed(MotionEvent evt)
	{
		
		if (cycleSelected == false)
		{
			
			int x = (int) evt.getX();
			int y = (int) evt.getY();

			for (int z = 0; z < targetList.size(); z++)
			{
	
				Target target = targetList.get(z);
	
				if (x <= target.x + (radius+5) && x >= target.x - (radius+5) && y >= target.y - (radius+5)
						&& y <= target.y + (radius+5))
				{
					selectedTarget = target; 
	
				}
			}
		}
		// int yMouse = evt.getPoint().y;
		return (selectedTarget != null);
	}

	public void targetMouseReleased(MotionEvent evt, final int axis)
	{

		cycleSelected = false;

		if (selectedTarget == null)
			return;
		int x = (int) evt.getX();
		int y = (int) evt.getY();

		if (axis > 0)
		{

			x = (int) (Math.round((x) / (0.5 * axis)) * (0.5 * axis));// +
																		// (int)((0.5*axis)/2);
			y = (int) (Math.round((y) / (0.5 * axis)) * (0.5 * axis));
		}

		selectedTarget.x = x;
		selectedTarget.y = y;

		selectedTarget = null;
	}

	void setTargetList(CopyOnWriteArrayList<Target> tl)
	{
		TargetManager.targetList = tl;
	}

	public boolean positionChanged()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setBoundingRect(View boundingRect)
	{
		this.boundingRect = boundingRect;

	}

}
