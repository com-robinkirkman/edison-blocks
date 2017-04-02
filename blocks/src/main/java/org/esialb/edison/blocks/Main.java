package org.esialb.edison.blocks;

import java.util.EnumMap;
import java.util.Map;

import org.eviline.core.Command;
import org.eviline.core.Configuration;
import org.eviline.core.Engine;
import org.eviline.core.EngineFactories;
import org.eviline.core.Field;
import org.eviline.core.ss.EvilBag7NShapeSource;

import org.esialb.edison.sfo.Menu;
import org.esialb.edison.sfo.MenuItem;
import org.esialb.edison.sfo.OledImage;
import org.esialb.edison.sfo.SFOled;
import org.esialb.edison.sfo.SFOled.Button;

public class Main {
	
	private static Configuration conf = new Configuration(
			EngineFactories.createIntegerFactory(null),
			EngineFactories.createIntegerFactory(0),
			EvilBag7NShapeSource.createFactory(3, 2));
	private static Field field = new Field();
	private static Engine engine = new Engine(field, conf);
	private static EngineDraw draw = new EngineDraw(engine);
	static {
		engine.setHoldEnabled(true);
	}

	public static void main(String[] args) throws Exception {
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
						return sleep();
					}
					
				}));
				pauseMenu.add(new MenuItem("Quit to Menu"));
				if(pauseMenu.show() == 2)
					return false;
			} else if(held.get(Button.LEFT) == 1)
				c = Command.HARD_DROP;
			else if(held.get(Button.DOWN) == 1)
				c = Command.SHIFT_LEFT;
			else if(held.get(Button.DOWN) > 10)
				c = Command.AUTOSHIFT_LEFT;
			else if(held.get(Button.UP) == 1)
				c = Command.SHIFT_RIGHT;
			else if(held.get(Button.UP) > 10)
				c = Command.AUTOSHIFT_RIGHT;
			else if(held.get(Button.A) == 1)
				c = Command.ROTATE_RIGHT;
			else if(held.get(Button.B) == 1)
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
		
		return false;
	}
	
}
