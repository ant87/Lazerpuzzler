/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.lazerpuzzler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

import com.example.lazerpuzzler.RayManager.SourceRay;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * @author User
 */
public class LightModel extends Observable
{

	public int AXIS_LENGTH = 10;

	private boolean rotate;

	public boolean raysSelectable = true;
	public boolean rotateEnabled = true;
	public boolean targetSelectable = true;
	public boolean gridOn = false;
	public boolean deleteEnabled = true;
	public boolean showDebugText = false;

	Calendar currentTime;
	ReflectorManager refMan;// = new ReflectorManager();
	TargetManager targetMan;// = new TargetManager();
	RayManager rayMan;// = new RayManager();

	private static final LightModel instance = new LightModel();

	private LightModel()
	{

		rayMan = RayManager.getInstance();
		refMan = ReflectorManager.getInstance();
		targetMan = TargetManager.getInstance();
		rayMan.setReflectorManager(refMan);
		rayMan.setTargetManager(targetMan);
		
		resetTimer();
	}

	public static LightModel getInstance()
	{
		return instance;
	}
	
	public void setAxisLength(int axisLength)
	{
		AXIS_LENGTH = axisLength;
		targetMan.setRadius(AXIS_LENGTH/4);
	}

	public void setRotateMode(boolean rotate)
	{
		this.rotate = rotate;
	}
	
	public void setTargetId()
	{
		TargetManager.Target sel = targetMan.getSelectedTarget();
		
		if(sel !=null)
			sel.setId(sel.getId()+1);
	}

	public boolean isEdited()
	{
		return (rayMan.isEdited() || 
				refMan.isEdited());/* ||
				targetMan.isEdited());*/
	}

	public void mouseDragged(MotionEvent evt)
	{
		rayMan.rayMouseDragged(evt);

		if (isRaySelected())
		{
			// repaint();
			return;
		}

		if (gridOn)
		{
			if (refMan.reflectorMouseDragged(evt, AXIS_LENGTH) == true)
			{
				rayMan.checkReflectorsForAllSources();
			}
		} else
		{
			refMan.reflectorMouseDragged(evt, 0);
			rayMan.checkReflectorsForAllSources();
		}

		if (isReflectorSelected())
		{
			// repaint();
			// System.out.println("mouse dragged ref selected");
			return;
		}

		targetMan.targetMouseDragged(evt);
		rayMan.checkReflectorsForAllSources();

	}

	public void mousePressed(MotionEvent evt)
	{
		if (targetSelectable == true)
		{

			if (targetMousePressed(evt) == false)
			{

				if (reflectorMousePressed(evt, rotateEnabled) == false)
				{
					if (raysSelectable == true)
					{
						rayMousePressed(evt, deleteEnabled);
					}
				}
			}

		}
		else
		{
			if (reflectorMousePressed(evt, rotateEnabled) == false)
			{
				if (raysSelectable == true)
				{
					rayMousePressed(evt, deleteEnabled);
				}
			}
		}

	}

	

	public void setSourceRayList(ArrayList<SourceRay> rl, boolean createReflections)
	{
		rayMan.setSourceRayList(rl, createReflections);
	}

	public void resetAllPosChanged()
	{
		refMan.resetAllPosChanged();
		rayMan.resetAllPosChanged();
	}

	public void setEdited(boolean isEdited)
	{
		rayMan.setEdited(isEdited);
		refMan.setEdited(isEdited);
		// targetMan.setEdited(true);
	}

/*	@Override
	public void addObserver(Observer o)
	{
		super.addObserver(o);
		rayMan.addObserver(o);
		targetMan.addObserver(o);

	}*/

	public void setSim()
	{

		// sim =new Sim(getSourceRayList());
	}

	public void deleteAll()
	{

		// if (mode != PLAY)
		{
			rayMan.deleteAllRays();
			refMan.deleteAllReflectors();
			targetMan.deleteAllTargets();

		}
	}

	private void resetTimer()
	{
		currentTime = Calendar.getInstance();
	}

	public CopyOnWriteArrayList<ReflectorManager.Reflector> getReflectorList()
	{
		return refMan.getReflectorList();
	}

	public boolean isReflectorSelected()
	{

		return refMan.getSelectedReflector() != null;
	}

	public boolean isRaySelected()
	{
		return rayMan.getSelectedRay() != null;
	}

	public CopyOnWriteArrayList<TargetManager.Target> getTargetList()
	{
		return TargetManager.getTargetList();
	}

	public static void addReflector()
	{
		ReflectorManager.getInstance().setEdited(true);
		createReflector(new Point((ReflectorManager.getInstance()
				.getReflectorList().size()
				* instance.AXIS_LENGTH)+(instance.AXIS_LENGTH/2), 100), 45, instance.AXIS_LENGTH);
	}

	public static void createReflector(Point mid, double ang, int axis)
	{
		ReflectorManager.createReflector(mid, ang, axis);
		RayManager.getInstance().checkReflectorsForAllSources();
	}
	
	public static void loadReflector(Point mid, double ang, int axis)
	{
		createReflector(mid, ang, axis);
		ReflectorManager.getInstance().setEdited(false);
	}
	

	public void cycleRefSelection()
	{
		refMan.cycleSelection(rotate);
	}

	public void cycleRaySelection(boolean sourceSelected)
	{
		rayMan.cycleSelection(rotate, sourceSelected);
	}

	public void cycleTargetSelection()
	{
		targetMan.cycleSelection();
	}

	public void deleteSelected()
	{
		if (rayMan.deleteSelected() == false)
		{
			if (refMan.deleteSelected() == false)
				targetMan.deleteSelected();
		}

	}

	public boolean checkAllTargetsHit()
	{
		return targetMan.isAllHit();
	}

	public boolean lightScenarioSet()
	{
		if ((refMan.getReflectorList().size() > 0) && (rayMan.getSourceRayList().size() > 0)
				&& (targetMan.getTargetList().size() > 0))
		{
			return true;
		} else
		{
			return false;
		}
	}

	public void addRay()
	{
		if (gridOn)
		{
			createSourceRay(0, AXIS_LENGTH, true);
		} else
		{
			createSourceRay(0, 0, true);
		}
	}

	public void setBoundingRect(View boundingRect)
	{
		refMan.setBoundingRect(boundingRect);
		rayMan.setBoundingRect(boundingRect);
		targetMan.setBoundingRect(boundingRect);
	}

	public void targetMouseReleased(MotionEvent evt)
	{
		targetMan.targetMouseReleased(evt, 0);
	}

	public void targetMouseReleased(MotionEvent evt, final int axis)
	{
		targetMan.targetMouseReleased(evt, axis);
	}

	public void rayMouseReleased(MotionEvent evt, boolean createReflections, final int axis,
			double roundAngToNearest)
	{
		rayMan.rayMouseReleased(evt, createReflections, axis, roundAngToNearest);
	}

	public void rayMouseReleased(MotionEvent evt, boolean createReflections)
	{
		rayMan.rayMouseReleased(evt, createReflections);
	}

	public void reflectorMouseReleased(MotionEvent evt, final int axis, double roundToNearest)
	{
		refMan.reflectorMouseReleased(evt, axis, roundToNearest);
		rayMan.checkReflectorsForAllSources();
	}

	public void reflectorMouseReleased(MotionEvent evt)
	{
		refMan.reflectorMouseReleased(evt);
		rayMan.checkReflectorsForAllSources();
	}

	public boolean isTargetSelected()
	{
		return targetMan.getSelectedTarget() != null;
	}

	public void rayMouseDragged(MotionEvent evt, boolean createReflections)
	{
		rayMan.rayMouseDragged(evt);
	}

	public void createTarget()
	{
		TargetManager.createTarget(10, 30 + (TargetManager.getTargetList().size() * 50));
	}
	
	/*public void createTarget(int x, int y)
	{
		TargetManager.createTarget(x,y);
	}*/
	
	public void createTarget(int x, int y, int id)
	{
		TargetManager.createTarget(x,y, id);
	}

	public void createSourceRay(double angle, int axis, boolean createReflections)
	{
		rayMan.createSourceRay(angle, axis, createReflections);
	}
	
	public void loadSourceRay(Point p, double angle, boolean createReflections)
	{
		rayMan.loadSourceRay(p, angle, createReflections);
	}

	public boolean targetMousePressed(MotionEvent evt)
	{
		return targetMan.targetMousePressed(evt);
	}

	public boolean reflectorMousePressed(MotionEvent evt, boolean rotationEnabled)
	{
		return refMan.androidReflectorMousePressed(evt, rotate, rotationEnabled);
	}

	public boolean rayMousePressed(MotionEvent evt, boolean deleteEnabled)
	{
		return rayMan.rayMousePressed(evt, deleteEnabled);
	}

	public ArrayList<RayManager.SourceRay> getSourceRayList()
	{
		return rayMan.getSourceRayList();
	}

}
