package org.esialb.edison.blocks;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

import org.eviline.core.Command;
import org.eviline.core.Configuration;
import org.eviline.core.Engine;
import org.eviline.core.EngineFactories;
import org.eviline.core.Field;
import org.eviline.core.ai.ForkJoinAIKernel;
import org.eviline.core.ai.NextFitness;
import org.eviline.core.ss.EvilBag7NShapeSource;

import mraa.I2c;
import mraa.I2cMode;

import org.esialb.edison.sfo.Menu;
import org.esialb.edison.sfo.MenuItem;
import org.esialb.edison.sfo.OledImage;
import org.esialb.edison.sfo.SFOled;
import org.esialb.edison.sfo.SFOled.Button;
import org.esialb.teensy.i2c.master.TeensyMaster;

public class Main {
	
	private static Configuration conf = new Configuration(
			EngineFactories.createIntegerFactory(null),
			EngineFactories.createIntegerFactory(1),
			EvilBag7NShapeSource.createFactory(3, 2, new ForkJoinAIKernel(new NextFitness())));
	private static Field field = new Field();
	private static Engine engine = new Engine(field, conf);
	static {
		engine.setHoldEnabled(true);
	}
	private static TeensyMaster teensyMaster; 
	private static EngineDraw draw;
	static {
		I2c i2c = new I2c(1);
		i2c.frequency(I2cMode.I2C_FAST);
		teensyMaster = TeensyMaster.newTeensyMaster(i2c);
		draw = new EngineDraw(engine, teensyMaster.gfx1);
	}

	public static void main(String[] args) throws Exception {
		SFOled.begin();
		
		teensyMaster.gfx0.fillRect(0, 0, 96, 64, Color.BLACK);
		
		Menu mainMenu = new Menu();
		mainMenu.add(new MenuItem("Play Blocks", new MenuItem.MenuAction() {
			@Override
			public boolean perform(Button button, MenuItem source) {
				return play();
			}
			
		}));
		mainMenu.add(new MenuItem("Sleep", new MenuItem.MenuAction() {
			@Override
			public boolean perform(Button button, MenuItem source) {
				return sleep();
			}
			
		}));
		mainMenu.add(new MenuItem("Quit", new MenuItem.MenuAction() {
			@Override
			public boolean perform(Button button, MenuItem source) {
				return quit();
			}
			
		}));
		mainMenu.show();
		System.exit(0);
	}
	
	private static boolean quit() {
		draw.clear();
		return true;
	}
	
	private static boolean sleep() {
		SFOled.createImage().paint();
		Map<Button, Boolean> bp = null;
		while(!(bp = SFOled.pressed(bp)).values().contains(true)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		while((bp = SFOled.pressed(bp)).containsValue(true))
			;
		return false;
	}
	
	private static boolean play() {
		engine.reset();
		
		Map<Button, Boolean> bp = null;
		Map<Button, Boolean> prevBp = null;
		Map<Button, Integer> held = new EnumMap<Button, Integer>(Button.class);

		draw.paint();
		SFOled.display();
		
		Command c = Command.NOP;
		while(!engine.isOver()) {
			if(engine.getShape() == -1) {
				while(engine.getShape() == -1)
					engine.tick(Command.NOP);
				draw.paint();
				SFOled.display();
			}
			bp = SFOled.pressed(bp);
			for(Button b : bp.keySet()) {
				if(bp.get(b))
					held.put(b, 1 + (held.containsKey(b) ? held.get(b) : 0));
				else
					held.put(b, 0);
			}
			if(held.get(Button.A) > 0 && held.get(Button.B) > 0) {
				while((bp = SFOled.pressed(bp)).containsValue(true))
					;
				Menu pauseMenu = new Menu();
				pauseMenu.add(new MenuItem("Resume"));
				pauseMenu.add(new MenuItem("Sleep", new MenuItem.MenuAction() {
					@Override
					public boolean perform(Button button, MenuItem source) {
						draw.clear();
						return sleep();
					}
					
				}));
				pauseMenu.add(new MenuItem("Quit to Menu"));
				if(pauseMenu.show() == 2) {
					draw.clear();
					return false;
				}
				draw.paint();
			} else if(held.get(Button.LEFT) == 1)
				c = Command.HARD_DROP;
			else if(held.get(Button.UP) == 1)
				c = Command.SHIFT_LEFT;
			else if(held.get(Button.UP) > 10)
				c = Command.AUTOSHIFT_LEFT;
			else if(held.get(Button.DOWN) == 1)
				c = Command.SHIFT_RIGHT;
			else if(held.get(Button.DOWN) > 10)
				c = Command.AUTOSHIFT_RIGHT;
			else if(held.get(Button.B) == 1)
				c = Command.ROTATE_RIGHT;
			else if(held.get(Button.A) == 1)
				c = Command.ROTATE_LEFT;
			else if(held.get(Button.RIGHT) == 1)
				c = Command.SHIFT_DOWN;
			else if(held.get(Button.RIGHT) > 10)
				c = Command.SOFT_DROP;
			else if(held.get(Button.SELECT) == 1)
				c = Command.HOLD;
			else
				c = Command.NOP;
			engine.tick(c);
			if(c != Command.NOP) {
				draw.paint();
				SFOled.display();
			}
			try {
				Thread.sleep(1000/60);
			} catch (InterruptedException e) {
			}
			Map<Button, Boolean> t = prevBp;
			prevBp = bp;
			bp = t;
		}
		
		draw.paint();
		SFOled.display();
		
		while((bp = SFOled.pressed(bp)).containsValue(true))
			;
		SFOled.awaitClick();
		
		draw.clear();
		return false;
	}
	
}
