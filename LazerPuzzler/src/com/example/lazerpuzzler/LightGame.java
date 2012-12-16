package com.example.lazerpuzzler;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Point;

/**
 * 
 * @author User
 */
public class LightGame
{
	public static final int FREE = 0;
	public static final int SET_LEVEL = 1;
	public static final int PLAY = 2;

	GameData gameData = new GameData();

	Calendar currentTime;



	public static boolean levelLoaded = false;

	LightModel light;
	// JPanel gui;

	private static String rayFileName;
	private static String refFileName;
	private static String targetFileName;
	private static String fileName;

	private static final String RAYS_SIM_FILE_NAME = "rays_sim";
	private static final String REFS_SIM_FILE_NAME = "refs_sim";
	private static final String TARGETS_SIM_FILE_NAME = "targets_sim";
	private static final String SIM_FILE_NAME = "sim";
	
	private static final String RAYS_FILE_NAME = "rays";
	private static final String REFS_FILE_NAME = "refs";
	private static final String TARGETS_FILE_NAME = "targets";
	
	private static final String FILE_NAME = "level";

	private static final LightGame instance = new LightGame();

	Activity activity;

	Context context;

	Panel pv;

	public void setContext(Context context)
	{
		this.context = context;
	}

	public void setPanel(Panel pv)
	{
		this.pv = pv;
	}

	public void levelComplete()
	{
		if (gameData.mode != PLAY)
			return;

		AlertDialog.Builder okDialog = new AlertDialog.Builder(activity);

		okDialog.setMessage(
				"completed level: "
						+ (gameData.level + 1)
						+ " in "
						+ (Calendar.getInstance().getTimeInMillis() - currentTime.getTimeInMillis())
						/ 1000 + " seconds").setCancelable(true)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id)
					{
						nextLevel(false);
						resetTimer();
						// pv.allTargetsHit = false;
						pv.invalidate();
					}
				});
		AlertDialog alert = okDialog.create();
		alert.show();

	}

	public void resetGame()
	{
		// AG improve this, we dont need to save eerything just gamed data
		gameData.gameHighestLevel = 0;
		gameData.level = 0;
		save();
		load();
	}

	private LightGame()
	{
		// this.gui = gui;
		light = LightModel.getInstance();
		resetTimer();
		// setGameMode(mode);
	}

	public void setActivity(Activity act)
	{
		activity = act;
	}

	public static LightGame getInstance()
	{
		return instance;
	}

	public long getElapsedTime()
	{
		return (Calendar.getInstance().getTimeInMillis() - currentTime.getTimeInMillis()) / 1000;
	}

	private void resetTimer()
	{
		currentTime = Calendar.getInstance();
	}

	public int getCurrentLevel()
	{
		if (gameData.mode == PLAY)
			return gameData.level;
		else
			return gameData.setLevel;
	}

	public void save()
	{
		FileOutputStream fos = null;
		ObjectOutputStream out = null;

		if ((light.getTargetList().size() == 0) && (light.getReflectorList().size() == 0)
				&& (light.getSourceRayList().size() == 0))
		{
			// return;
		}
		if (gameData.mode != PLAY)
		{

			ArrayList<RayManager.SourceRay> raySourceList = light.getSourceRayList();
			CopyOnWriteArrayList<ReflectorManager.Reflector> reflectorList = light
					.getReflectorList();
			CopyOnWriteArrayList<TargetManager.Target> targetList = TargetManager.getTargetList();

			for (RayManager.SourceRay sr : raySourceList)
			{
				sr.deleteReflections();
			}

			try
			{
				fos = new FileOutputStream(context.getFilesDir().getPath().toString() +"/"+ fileName
						+ gameData.setLevel + ".txt");
				DataOutputStream dout = new DataOutputStream(fos);

				dout.writeByte(raySourceList.size());
				for (RayManager.SourceRay sr : raySourceList)
				{
					dout.writeByte(sr.source.x/(light.AXIS_LENGTH/2));
					dout.writeByte(sr.source.y/(light.AXIS_LENGTH/2));
					dout.writeDouble(sr.angle);
					
					/* spare */
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
				}
				
				dout.writeByte(reflectorList.size());
				for (ReflectorManager.Reflector ref : reflectorList)
				{
					dout.writeByte(ref.getMidPoint().x/(light.AXIS_LENGTH/2));
					dout.writeByte(ref.getMidPoint().y/(light.AXIS_LENGTH/2));
					//dout.writeInt(ref.getLength());
					dout.writeDouble(ref.getAngle());
					
					/* spare */
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
				}
				
				dout.writeByte(targetList.size());
				for (TargetManager.Target t : targetList)
				{
					dout.writeByte(t.x/(light.AXIS_LENGTH/2));
					dout.writeByte(t.y/(light.AXIS_LENGTH/2));
					dout.writeByte(t.getId());
					
					/* spare */
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);
					dout.writeByte(0xFF);

				}
				dout.close();
				// out.writeObject(raySourceList);
				// out.close();
				System.out.println("level  saved");
			} catch (IOException ex)
			{
				ex.printStackTrace();
				System.out.println("level not saved");
			}

			

			
		} else if (gameData.mode == PLAY)
		{
			try
			{
				fos = new FileOutputStream(context.getFilesDir().getPath().toString() +"/"+ "game.txt");
				out = new ObjectOutputStream(fos);
				// out.writeInt(gameData.gameHighestLevel);
				out.writeObject(gameData);
				out.close();

				System.out.println("game saved");
			} catch (IOException ex)
			{
				ex.printStackTrace();
				System.out.println("game NOT saved");
			}
		}

		// edited = false;
		// light.resetAllPosChanged();

	}

	public final void load()
	{

		light.deleteAll();
		light.setEdited(false);
	

		FileInputStream fis;// = null;
		ObjectInputStream in;// = null;

		if (levelLoaded == false)
		{
			try
			{
				fis = new FileInputStream(context.getFilesDir().getPath().toString() +"/"+ "game.txt");
				in = new ObjectInputStream(fis);

				gameData = (GameData) in.readObject();
				in.close();
			} catch (IOException ex)
			{
				gameData.gameHighestLevel = 0;
				gameData.setLevel = 0;
				gameData.level = 0;
				//setGameMode(PLAY);
				setGameMode(PLAY);

			} catch (ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			levelLoaded = true;
			setGameMode(gameData.mode);

			if (gameData.mode == PLAY)
				gameData.level = gameData.gameHighestLevel;
		}

		int levelToLoad;

		if (gameData.mode == PLAY)
		{
			levelToLoad = gameData.level;
		} else
		{
			levelToLoad = gameData.setLevel;
		}
		try
		{
			fis = new FileInputStream(context.getFilesDir().getPath().toString() +"/"+ fileName
					+ levelToLoad + ".txt");
			// in = new ObjectInputStream(fis);

			// ll = (LinkedList)in.readObject();
			DataInputStream dis = new DataInputStream(fis);
			
			int numOfRays = dis.readByte();	

			for (int x = 0; x < numOfRays; x++)
			{
				int sourceX = dis.readByte()*(light.AXIS_LENGTH/2);
				int sourceY = dis.readByte()*(light.AXIS_LENGTH/2);
				double angle = dis.readDouble();
				
				/* spare */
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				
				

				light.loadSourceRay(new Point(sourceX, sourceY), angle, true);
			}

			int numOfRefs = dis.readByte();

			for (int x = 0; x < numOfRefs; x++)
			{
				int midX = dis.readByte()*(light.AXIS_LENGTH/2);
				int midY = dis.readByte()*(light.AXIS_LENGTH/2);
				//int length = 100;// dis.readInt();
				double angle = dis.readDouble();
				
				/* spare */
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				
	

				LightModel.loadReflector(new Point(midX, midY), angle, light.AXIS_LENGTH);
			}
			
			
			int numOfTargets = dis.readByte();

			for (int x = 0; x < numOfTargets; x++)
			{
				int tX = dis.readByte()*(light.AXIS_LENGTH/2);
				int tY = dis.readByte()*(light.AXIS_LENGTH/2);
				int id = dis.readByte();
				
				/* spare */
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();
				 dis.readByte();

				light.createTarget(tX, tY, id);
				
			}

			dis.close();
		}

		catch (EOFException ex)
		{
			ex.printStackTrace();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
		

	}

	public int getMode()
	{
		return gameData.mode;
	}

	public void prevLevel()
	{
		// save();
		if (gameData.mode == PLAY)
		{
			if (gameData.level > 0)
				gameData.level--;
		} else
		{
			if (gameData.setLevel > 0)
				gameData.setLevel--;
		}

		resetTimer();

		load();

	}

	public void nextLevel(boolean buttonPress)
	{

		if (gameData.mode == PLAY)
		{
			// if (level < LAST_LEVEL)
			resetTimer();
			{
				if (buttonPress)
				{
					if (gameData.level < gameData.gameHighestLevel)
					{
						gameData.level++;
						load();
					}
				} else
				{

					gameData.level++;
					load();

					if (gameData.level > gameData.gameHighestLevel)
					{
						gameData.gameHighestLevel = gameData.level;
					}

					if (!light.lightScenarioSet())
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						builder.setMessage("Game Completed").setCancelable(true)
								.setPositiveButton("Ok", null);
						AlertDialog alert = builder.create();
						alert.show();

					}
					save();

				}
			}

		} else
		{

			if (light.isEdited() == true)
			{

				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage("Save changes?").setCancelable(true)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id)
							{
								save();
								light.setEdited(false);
								light.resetAllPosChanged();
								gameData.setLevel++;
								load();
								pv.invalidate();
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id)
							{
								gameData.setLevel++;
								light.setEdited(false);
								light.resetAllPosChanged();

								load();
								pv.invalidate();
							}
						}).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id)
							{
								/* Do nothing */
							}
						});
				AlertDialog alert = builder.create();
				alert.show();

			} else
			{
				gameData.setLevel++;
				load();
			}

		}
	}

	private static class GameData
	{
		int gameHighestLevel;
		int setLevel;
		int level;
		int mode;
	}

	public void setGameMode(int mode)
	{
		gameData.mode = mode;

		switch (mode)
		{
		case FREE:
			light.raysSelectable = true;
			light.rotateEnabled = true;
			light.targetSelectable = true;
			light.gridOn = false;
			light.deleteEnabled = true;
			light.showDebugText = false;
			rayFileName = RAYS_SIM_FILE_NAME;
			refFileName = REFS_SIM_FILE_NAME;
			targetFileName = TARGETS_SIM_FILE_NAME;
			fileName = SIM_FILE_NAME;
			break;

		case SET_LEVEL:
			light.raysSelectable = true;
			light.rotateEnabled = true;
			light.targetSelectable = true;
			light.gridOn = true;
			light.deleteEnabled = true;
			light.showDebugText = true;
			// pv.enableInfoDisplay(true);
			rayFileName = RAYS_FILE_NAME;
			refFileName = REFS_FILE_NAME;
			targetFileName = TARGETS_FILE_NAME;
			fileName = FILE_NAME;
			break;

		case PLAY:

			light.raysSelectable = false;
			light.rotateEnabled = false;
			light.targetSelectable = false;
			light.gridOn = true;
			light.showDebugText = false;
			rayFileName = RAYS_FILE_NAME;
			refFileName = REFS_FILE_NAME;
			targetFileName = TARGETS_FILE_NAME;
			fileName = FILE_NAME;

			break;

		}
		// nextAllowed();
		// update(null,null);
	}

	public void copyAssets()
	{
		AssetManager assetManager = context.getAssets();
		String[] files = null;
		try
		{
			files = assetManager.list("");

			for (String s : files)
				System.out.println("ASSET FILES " + s);
		} catch (IOException e)
		{
			System.out.println("Failed to get asset file list.");
		}
		for (String filename : files)
		{
			InputStream in = null;
			OutputStream out = null;
			try
			{
				File file = new File(context.getFilesDir().getPath().toString() +"/"+ filename);
				if (file.exists())
					continue;

				in = assetManager.open(filename);
				out = new FileOutputStream(file);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;

			} catch (IOException e)
			{
				System.out.println("Failed to get asset file list" + filename);
			}

		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1)
		{
			out.write(buffer, 0, read);
		}
	}

	public void copyfilesToSdCard()
	{
		copyfileToSdCard(getRayFileName());
		copyfileToSdCard(getRefFileName());
		copyfileToSdCard(getTargetFileName());
	}

	private void copyfileToSdCard(String fileName)
	{

		try
		{
			InputStream in = new FileInputStream(context.getFilesDir().getPath().toString()
					+ fileName);

			OutputStream out = new FileOutputStream("sdcard/" + fileName);

			copyFile(in, out);
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch (FileNotFoundException ex)
		{
			System.out.println(ex.getMessage() + " in the specified directory.");

		} catch (IOException e)
		{
			System.out.println(e.getMessage());
		}

	}

	private String getRefFileName()
	{
		return refFileName + gameData.setLevel + ".txt";
	}

	private String getRayFileName()
	{
		return rayFileName + gameData.setLevel + ".txt";
	}

	private String getTargetFileName()
	{
		return targetFileName + gameData.setLevel + ".txt";
	}
	
	private String getFileName()
	{
		return fileName + gameData.setLevel + ".txt";
	}

}
