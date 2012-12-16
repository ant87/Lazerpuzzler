package com.example.lazerpuzzler;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;

class Panel extends View implements OnTouchListener, Observer
{
	private static final int[] colours = new int[] {Color.YELLOW,Color.GREEN,0xFFf0f0f0, Color.CYAN,Color.MAGENTA,Color.WHITE};

	
	static int test1 = 0;
	


	private static final int REFLECTOR_UNSELECTED_COLOUR = Color.CYAN;
	private static final int REFLECTOR_SELECTED_COLOUR = Color.RED;

	private int reflectorColour = REFLECTOR_UNSELECTED_COLOUR;


	int events = 0;

	int a = 0xFFFF0000;
	int b = 0xFF00FF00;
	int c = 0xFF0000FF;

	GradientDrawable backgroundPaint;

	Paint targetDefaultBlurPaint = new Paint();
	Paint targetSelectedPaint = new Paint();
	Paint targetHitPaint = new Paint();
	

	Paint targetDefaultPaint = new Paint();

	// GradientDrawable reflectorPaint;

	// final Paint paint = new Paint();

	Paint reflectorPaint = new Paint();
	Paint reflectorEdgePaint = new Paint();
	Paint axisPaint = new Paint();
	Paint rayBlurPaint = new Paint();
	Paint raySolidPaint  = new Paint();
	Paint textPaint = new Paint();

	public static Point pRef;

	static int x;

	LightModel light;

	 Thread thread = null;
	    SurfaceHolder surfaceHolder;
	    volatile boolean running = false;
	    
	    Drawable myIcon;
	    
	    ColorMatrix matrix=  new ColorMatrix(new float[]{
	    		   1,1,1,1,1
	              ,0,0,0,0,0
	              ,0,0,0,0,0,
	              0.5f,0.5f,0.5f,0.5f,0.5f,});;// = new ColorMatrix();
	    ColorMatrixColorFilter cmcf;
	    
	   
	    



	public Panel(Context context, LightModel light)
	{
		super(context);
		// parent = (Observer)context;
		setOnTouchListener(this);
		
		this.light = light;
		int axis = light.AXIS_LENGTH;
	//	surfaceHolder = getHolder();
		setBackgroundColor(Color.DKGRAY);

	

		backgroundPaint = new GradientDrawable(
				GradientDrawable.Orientation.TL_BR, new int[] { Color.BLACK,
						Color.DKGRAY });
		backgroundPaint.setShape(GradientDrawable.RECTANGLE);
		
		targetDefaultPaint.setColor(Color.BLUE);
		targetDefaultPaint.setAntiAlias(true);
		
		
		targetDefaultPaint.setStyle(Paint.Style.STROKE);
		targetDefaultPaint.setStrokeWidth(1);
		
		targetDefaultBlurPaint.setColor(Color.BLUE);
		targetDefaultBlurPaint.setStyle(Paint.Style.STROKE)	;
		targetDefaultBlurPaint.setStrokeWidth(axis/15);
		targetDefaultBlurPaint.setMaskFilter(new BlurMaskFilter(axis/20, BlurMaskFilter.Blur.NORMAL));

		//targetDefaultBlurPaint.setMaskFilter(new BlurMaskFilter(axis/20, BlurMaskFilter.Blur.NORMAL));
		
		/*= new GradientDrawable(
				GradientDrawable.Orientation.BR_TL, new int[] { Color.BLACK,
						0xFF7F7FFF });*/
		
		targetSelectedPaint.setColor(Color.GREEN );
		targetSelectedPaint.setStyle(Paint.Style.STROKE)	;
		targetSelectedPaint.setStrokeWidth(axis/15);
		targetSelectedPaint.setMaskFilter(new BlurMaskFilter(axis/20, BlurMaskFilter.Blur.NORMAL));
		/* = new GradientDrawable(
				GradientDrawable.Orientation.BR_TL, new int[] { Color.BLACK,
						Color.GREEN });*/
		

		//targetHitPaint.setColor(Color.RED );
		//targetHitPaint.setMaskFilter(new BlurMaskFilter(axis/20, BlurMaskFilter.Blur.NORMAL));
		targetHitPaint.setColorFilter(new ColorMatrixColorFilter(matrix));
		
		/*new GradientDrawable(
				GradientDrawable.Orientation.BR_TL, new int[] { Color.BLACK,
						Color.RED });*/

		textPaint.setColor(Color.RED);
		// targetHitPaint.setShape(GradientDrawable.RECTANGLE);

		// setBackgroundColor(Color.argb(255, 240, 240, 240));
		
		rayBlurPaint.setStrokeWidth(axis/12);
		
		rayBlurPaint.setMaskFilter(new BlurMaskFilter(axis/15, BlurMaskFilter.Blur.NORMAL));
		
		reflectorEdgePaint.setStrokeWidth(axis/15);
		reflectorEdgePaint.setColor(Color.WHITE);
		reflectorEdgePaint.setMaskFilter(new BlurMaskFilter(axis/20, BlurMaskFilter.Blur.NORMAL));
		
		
		myIcon = getResources().getDrawable( R.drawable.ic_launcher );
		
		
		cmcf = new ColorMatrixColorFilter(matrix);
		
	}
	
	
	
	

	public void setModel(LightModel mod)
	{
		light = mod;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDraw(final Canvas canvas)
	{
		super.onDraw(canvas);

		//System.out.println("ondraw " + test1++);
		Rect rect = canvas.getClipBounds();
		
		int height = rect.height();
		int width = rect.width();
		CopyOnWriteArrayList<ReflectorManager.Reflector> refList = light
				.getReflectorList();
		ArrayList<RayManager.SourceRay> sourceRayList = light
				.getSourceRayList();

		Panel.x++;

		backgroundPaint.setBounds(rect);

		backgroundPaint.draw(canvas);

		if (light.showDebugText)
		{
			canvas.drawText("Dev mode", 10, 10, textPaint);
			canvas.drawText("Level: " + LightGame.getInstance().getCurrentLevel(),
					10, 30, textPaint);

			canvas.drawText("Reflectors: " + refList.size(), 10, 60, textPaint);
			
			canvas.drawText("Rays: "
					+ RayManager.getInstance().getSourceRayList().size(), 10,
					80, textPaint);
			
			canvas.drawText("Targets: "
					+ TargetManager.getTargetList().size(), 10,
					100, textPaint);
			
			if (ReflectorManager.getInstance().getSelectedReflector() !=null)
			
			canvas.drawText("Ref points: "
					+ ReflectorManager.getInstance().getSelectedReflector().getReflectionPoints().size(), 10,
					150, textPaint);
			
		}

		int x = 0;

		if (light.gridOn)
		{
			axisPaint.setColor(Color.GRAY);
			while (x < width)
			{
				x += light.AXIS_LENGTH;
				canvas.drawLine(x, 0, x, height, axisPaint);
				if (x < height)
					canvas.drawLine(0, x, width, x, axisPaint);

			}
			// paint.setColor(Color.BLACK);
		}

		
		// System.out.println("targetlist size = "+ targetList.size());

		/******************************* draw rays ********************************/

		// canvas.setComposite(rayComposite);

		// canvas.setStroke(rayStroke);



		for (RayManager.SourceRay sourceRay : sourceRayList)
		{

			// SourceRay sourceRay = raySourceList.get(z);
			// LinkedList rayList = sourceRay.getRayList();
			

           
			int id = sourceRay.getId();
				

			for (RayManager.Ray r1 : sourceRay.getRayList())
			{
				
			
				
				
				
				if (sourceRay.isSelected())
				{
					rayBlurPaint.setColor(Color.RED);
			    	raySolidPaint.setColor(Color.RED);
				}
				else
				{
					rayBlurPaint.setColor(colours[id]);
				    raySolidPaint.setColor(colours[id]);
				}
				//rayPaint.setColor(0xFF00FF00);
			
				
				//rayBlurPaint.setMaskFilter(new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL));
				
				
				canvas.drawLine(r1.getP1().x, r1.getP1().y, r1.getP2().x,
						r1.getP2().y, rayBlurPaint);
				
				
				canvas.drawLine(r1.getP1().x, r1.getP1().y, r1.getP2().x,
						r1.getP2().y, raySolidPaint);
				
			
			/*	
				rayPaint.setStrokeWidth(1);
				
				rayPaint.setMaskFilter(new BlurMaskFilter(2, BlurMaskFilter.Blur.OUTER));
				
				
				if (sourceRay.isSelected())
					rayPaint.setColor(Color.RED);
				else if(id == 0)
					
					rayPaint.setColor(Color.WHITE);
				else
					rayPaint.setColor(0xFF00FF00);
				
				canvas.drawLine(r1.getP1().x, r1.getP1().y, r1.getP2().x,
						r1.getP2().y, rayPaint);
						*/
				/*Paint temp = new Paint();
				temp.setColor(Color.RED);
				 canvas.drawText("id "+sourceRay.getId(), r1.getP1().x+50, r1.getP1().y+10,temp );*/

				
			}

			canvas.drawCircle(sourceRay.getP1().x, sourceRay.getP1().y, light.AXIS_LENGTH/6,
					rayBlurPaint);
			
			canvas.drawCircle(sourceRay.getP1().x, sourceRay.getP1().y, light.AXIS_LENGTH/15,
					raySolidPaint);
			
			 id++;
			 
			
		}

		// canvas.setStroke(originalStroke);

		/*************************** draw reflectors **********************************************/

		// LinkedList rayList = Ray.getRayList();

		// g.drawString(Double.toString(rayList.size()),700,100);

		Point p1;
		Point p2;
		Point mid;
		Point snap;

		for (ReflectorManager.Reflector r1 : refList)
		{

			p1 = r1.getPoint1();
			p2 = r1.getPoint2();
			mid = r1.getMidPoint();
			snap = r1.getSnapPoint();


			if (r1.isSelected())
				reflectorColour = REFLECTOR_SELECTED_COLOUR;
			else
				reflectorColour = REFLECTOR_UNSELECTED_COLOUR;

			// if(i==0)
			// canvas.drawText("P1.x" +p1.x + "P1.y"+ p1.y, 300, 50, textPaint);

			// canvas.setComposite(refComposite);
			// canvas.setStroke(refStroke);

			// g.setColor(r.getColour());

			LinearGradient lg = new LinearGradient(p1.x, p1.y, p2.x, p2.y,
					Color.GRAY, reflectorColour, Shader.TileMode.MIRROR);
			
			reflectorPaint.setShader(lg);
			reflectorPaint.setStrokeWidth(2);
		//	reflectorPaint.setColorFilter(cmcf);
			//reflectorEdgePaint.setColorFilter(cmcf);

			canvas.drawLine(p1.x, p1.y, p2.x, p2.y, reflectorEdgePaint);

			canvas.drawCircle(mid.x, mid.y, 2, reflectorEdgePaint);
			
			canvas.drawLine(p1.x, p1.y, p2.x, p2.y, reflectorPaint);

			canvas.drawCircle(mid.x, mid.y, 2, reflectorPaint);
			
			;


			if ((r1.getAction() == ReflectorManager.MOVE_REFLECTOR)
					&& (light.gridOn == true))
			{

				canvas.drawCircle(mid.x, mid.y, 2, reflectorPaint);
				reflectorPaint.setColor(Color.LTGRAY);
				// g.drawOval(snap.x-2, height-(snap.y+2), 4, 4);

				// canvas.setStroke(dashedLineStroke);
				canvas.drawLine(mid.x, mid.y, snap.x, snap.y, reflectorPaint);
				// canvas.setStroke(originalStroke);
				reflectorPaint.setColor(reflectorColour);
			}

			if (r1.getAction() == ReflectorManager.MOVE_P1
					|| r1.getAction() == ReflectorManager.MOVE_P2)
			{
				canvas.drawCircle(mid.x, mid.y, 2, reflectorPaint);

			}

			// g.drawString(Double.toString(r.getAngle360()), (i + 1) * 50, (i +
			// 1) * 50);
			// g.drawString("p1",r.getPoint1().x, (int)(rect.getHeight() -
			// r.getPoint1().y ));
			// g.drawString("p2",r.getPoint2().x, (int)(rect.getHeight() -
			// r.getPoint2().y ));

		}
		
		/********************draw targets *********************/
		
		int radius = TargetManager.getInstance().getRadius();
		
		for (TargetManager.Target target : light.getTargetList())
		{
			
			int id = target.getId();
			
			if(id == -1)
			{
				targetDefaultBlurPaint.setColor(Color.WHITE);
				
			}
			else if(id < colours.length)
				
			{
				targetDefaultBlurPaint.setColor(colours[id]);
			
				
			}
			else
			{
				targetDefaultBlurPaint.setColor(Color.WHITE);
			}
			canvas.drawCircle(target.x,target.y,radius,targetDefaultPaint);
			
			canvas.drawCircle(target.x,target.y,radius,targetDefaultBlurPaint);

			if (target.isSelected())
			{
				

				canvas.drawCircle(target.x,target.y,radius+5,targetSelectedPaint);
				

				
			}

			if (target.isHit())
			{
				/*targetHitPaint.setShape(GradientDrawable.OVAL);
				targetHitPaint.setBounds(target.x - radius,
						target.y - radius, target.x
						+ radius, target.y
						+ radius);

				targetHitPaint.draw(canvas);*/
				
				//myIcon.setColorFilter(cmcf);
				canvas.drawCircle(target.x,target.y,radius,targetHitPaint);
			}
			/*myIcon.setBounds(target.x - (radius+5),
					target.y - (radius+5), target.x
					+ (radius+5), target.y
					+ (radius+5));
			//myIcon.setColorFilter(cmcf);
			myIcon.draw(canvas);
			myIcon.clearColorFilter();*/
			
			

		}
		
		/************ draw targets *************************/


	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height)
	{
		// TODO Auto-generated method stub
		System.out.println("surface changed");

	}

	
	
	

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent evt)
	{
		super.onKeyDown(keyCode, evt);
		System.out.println("key pressed is: " + keyCode);
		return false;
	}

	public boolean onTouch(View arg0, MotionEvent evt)
	{
		System.out.println("on touch");

		super.onTouchEvent(evt);
		// boolean captured = false;

		switch (evt.getAction())
		{
		case MotionEvent.ACTION_MOVE:
			events = 2;
			System.out.println("action move");
			light.mouseDragged(evt);
			invalidate();

			break;

		case MotionEvent.ACTION_DOWN:
			System.out.println("action down");
			events = 1;

			
	       light.mousePressed(evt);
			
	

			invalidate();
			break;

		case MotionEvent.ACTION_UP:
			events = 3;
			System.out.println("action up");

			if (light.gridOn)
			{

				light.reflectorMouseReleased(evt, light.AXIS_LENGTH,
						22.5);

				light.rayMouseReleased(evt, true,
						light.AXIS_LENGTH, 22.5);
				light.targetMouseReleased(evt, light.AXIS_LENGTH);
			} else
			{
				light.reflectorMouseReleased(evt);

				light.rayMouseReleased(evt, true);
				light.targetMouseReleased(evt);
			}

			if (light.checkAllTargetsHit())
			{

				((MainActivity) getContext()).levelComplete();

			}

			invalidate();
			break;

		}

		return true;

	}

	public void update(Observable o, Object arg)
	{

		invalidate();

	}
	
	
}


