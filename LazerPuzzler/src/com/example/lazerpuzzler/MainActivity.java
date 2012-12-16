package com.example.lazerpuzzler;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

public class MainActivity extends Activity
{

	Panel pv;
	public static int x = -10;
	public static final int PAUSE = 0;
	public static final int CREATE = 1;
	public static final int RESUME = 2;
	public static final int DESTROY = 3;
	public static final int START = 4;
	public static final int STOP = 4;
	public static int event;

	// public static final int MENU_MOVE = 0;
	public static final int MENU_REFLECTOR = 0;
	public static final int MENU_RAY = 1;
	public static final int MENU_TARGET = 2;
	public static final int MENU_PREV = 3;
	public static final int MENU_NEXT = 4;
	public static final int MENU_DELETE = 5;
	public static final int MENU_SAVE = 6;


	LightGame game;
	LightModel light;

	RelativeLayout fl;
	Handler handler;

	public MainActivity()
	{
		x++;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);

		// Reflector.deleteAllReflectors();
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		handler = new Handler();

		System.out.println("hello world");
		
		light = LightModel.getInstance();
		
		
		//AG can setaxislength and setboundingrect be done together?
		
		Display display = getWindowManager().getDefaultDisplay();
		light.setAxisLength(display.getWidth()/8);

		pv = new Panel(this, light);
		
		light.setBoundingRect(pv);
		
		
		
		
		
		
	
		game = LightGame.getInstance();
		game.setContext(this);
	
		game.setActivity(this);
		game.setPanel(pv);
		game.copyAssets();
		// final LightActivity aThis = this;

		light.addObserver(pv);

		if (savedInstanceState == null)
			game.load();
		initComponents();
		
		

		/************************************************************************/

	}
	
	

	Handler getHandler()
	{
		return handler;
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		menu.add(Menu.NONE, MENU_REFLECTOR, 1, "ref");
		menu.add(Menu.NONE, MENU_RAY, 2, "ray");
		menu.add(Menu.NONE, MENU_TARGET, 3, "target");
		menu.add(Menu.NONE, MENU_PREV, 4, "prev");
		menu.add(Menu.NONE, MENU_NEXT, 5, "next");
		menu.add(Menu.NONE, MENU_DELETE, 6, "delete");
		menu.add(Menu.NONE, MENU_SAVE, 7, "save");
		menu.add(Menu.NONE, 8, 8, "del sel");
		menu.add(Menu.NONE, 9, 9, "Dev");
		menu.add(Menu.NONE, 10, 10, "Play");
		menu.add(Menu.NONE, 11, 11, "Free");
		menu.add(Menu.NONE, 12, 12, "Copy to SD Card");

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
		case MENU_REFLECTOR:
			if (game.getMode() != LightGame.PLAY)
			{
				LightModel.addReflector();
			}
			return true;
		case MENU_RAY:
			if (game.getMode() != LightGame.PLAY)
			{
				light.addRay();
				pv.invalidate();
			}
			return true;
		case MENU_TARGET:
			if (game.getMode() != LightGame.PLAY)
			{
				light.createTarget();
				pv.invalidate();
			}
			return true;
		case MENU_PREV:

			game.prevLevel();
			pv.invalidate();

			return true;
		case MENU_NEXT:
			if (game.getMode() != LightGame.PLAY)
			{
				game.nextLevel(true);

				// Panel.testflag = true;
				pv.invalidate();

			}

			return true;
		case MENU_DELETE:

			if (game.getMode() != LightGame.PLAY)
			{
				light.deleteAll();
				light.setEdited(true);
				pv.invalidate();
			}

			return true;
		case MENU_SAVE:
			light.setEdited(false);
			game.save();
			pv.invalidate();
			return true;

		case 8:
			light.deleteSelected();
			pv.invalidate();
			return true;
		case 9:

			game.setGameMode(LightGame.SET_LEVEL);

			initComponents();
			game.load();
			pv.invalidate();
			return true;
		case 10:

			game.setGameMode(LightGame.PLAY);

			initComponents();
			game.load();
			pv.invalidate();
			return true;

		case 11:

			game.setGameMode(LightGame.FREE);

			initComponents();
			game.load();
			pv.invalidate();
			return true;
		case 12:

			game.copyfilesToSdCard();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void levelComplete()
	{

		game.levelComplete();

	}

	private void initComponents()
	{
		fl = new RelativeLayout(this);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);

		if (pv.getParent() != null)
		{

			((RelativeLayout) pv.getParent()).removeView(pv);
		}
		fl.addView(pv, params);

		/******* buttonRefSelect ***********************/
		Button buttonRefSelect = new Button(this);
		buttonRefSelect.setId(10);
		buttonRefSelect.setText("Ref");
		buttonRefSelect.setMaxWidth(light.AXIS_LENGTH);
		buttonRefSelect.setMinWidth(light.AXIS_LENGTH);

		buttonRefSelect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v)
			{
				light.cycleRefSelection();
				pv.invalidate();
			}
		});

		params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

		fl.addView(buttonRefSelect, params);

		if (game.getMode() != LightGame.PLAY)
		{

			/*********** buttonRaySelect ******************/
			Button buttonRaySelect = new Button(this);
			buttonRaySelect.setId(11);
			buttonRaySelect.setText("Ray");
			buttonRaySelect.setMaxWidth(light.AXIS_LENGTH);
			buttonRaySelect.setMinWidth(light.AXIS_LENGTH);

			buttonRaySelect.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v)
				{
					light.cycleRaySelection(false);
					pv.invalidate();
				}
			});

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, buttonRefSelect.getId());
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

			fl.addView(buttonRaySelect, params);

			/*********** button Source Select ******************/
			Button buttonSourceSelect = new Button(this);
			buttonSourceSelect.setId(12);
			buttonSourceSelect.setText("Souce");
			buttonSourceSelect.setMaxWidth(light.AXIS_LENGTH);
			buttonSourceSelect.setMinWidth(light.AXIS_LENGTH);

			buttonSourceSelect.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v)
				{
					light.cycleRaySelection(true);
					pv.invalidate();
				}
			});

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, buttonRaySelect.getId());
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

			fl.addView(buttonSourceSelect, params);

			/*********** button select target ******************/
			Button buttonTargetSelect = new Button(this);
			buttonTargetSelect.setId(13);
			buttonTargetSelect.setText("Target");
			buttonTargetSelect.setMaxWidth(light.AXIS_LENGTH);
			buttonTargetSelect.setMinWidth(light.AXIS_LENGTH);

			buttonTargetSelect.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v)
				{
					light.cycleTargetSelection();
					pv.invalidate();
				}
			});

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, buttonSourceSelect.getId());
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

			fl.addView(buttonTargetSelect, params);

			/**************** buttonDeleteSeleted **********************************/
			/*
			 * Button buttonDeleteSeleted =new Button(this);
			 * buttonDeleteSeleted.setId(14);
			 * buttonDeleteSeleted.setText("Del");
			 * buttonDeleteSeleted.setMinimumWidth(100); //
			 * buttonDeleteSeleted.setAlpha((float) 0.5);
			 * 
			 * 
			 * buttonDeleteSeleted.setOnClickListener(new View.OnClickListener()
			 * { public void onClick(View v) { pv.deleteSeleted(); } });
			 * 
			 * 
			 * params =new
			 * RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
			 * LayoutParams.WRAP_CONTENT);
			 * params.addRule(RelativeLayout.BELOW,buttonTargetSelect.getId() );
			 * params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
			 * RelativeLayout.TRUE);
			 * 
			 * fl.addView(buttonDeleteSeleted, params);
			 */

			/*********** buttonRotate ******************/
			ToggleButton buttonRotate = new ToggleButton(this);
			buttonRotate.setId(15);
			buttonRotate.setText("Rot");
			buttonRotate.setMaxWidth(light.AXIS_LENGTH);
			buttonRotate.setMinWidth(light.AXIS_LENGTH);

			buttonRotate.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v)
				{
					light.setRotateMode(((ToggleButton)v).isChecked());
				}
			});

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, buttonTargetSelect.getId());
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

			fl.addView(buttonRotate, params);
			
			/*********** buttonTargetId ******************/
			Button buttonTargetId = new Button(this);
			buttonTargetId.setId(16);
			buttonTargetId.setText("ID");
			buttonTargetId.setMaxWidth(light.AXIS_LENGTH);
			buttonTargetId.setMinWidth(light.AXIS_LENGTH);

			buttonTargetId.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v)
				{
					light.setTargetId();
					pv.invalidate();
				}
			});

			params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, buttonRotate.getId());
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

			fl.addView(buttonTargetId, params);


			/*********** buttonReset ******************/
			/*
			 * Button buttonReset =new Button(this); buttonReset.setId(16);
			 * buttonReset.setText("Reset"); buttonReset.setMinimumWidth(100);
			 * 
			 * buttonReset.setOnClickListener(new View.OnClickListener() {
			 * public void onClick(View v) { game.resetGame(); pv.invalidate();
			 * } });
			 * 
			 * params =new
			 * RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
			 * LayoutParams.WRAP_CONTENT);
			 * params.addRule(RelativeLayout.RIGHT_OF,buttonRotate.getId() );
			 * params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
			 * RelativeLayout.TRUE);
			 * 
			 * 
			 * 
			 * fl.addView(buttonReset, params);
			 */

		}

		setContentView(fl);
		/*****************************************************/
	}

}