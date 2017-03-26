package org.esialb.edison.blocks;

import java.util.EnumMap;
import java.util.Map;

import org.eviline.core.Command;
import org.eviline.core.Configuration;
import org.eviline.core.Engine;
import org.eviline.core.Field;

import com.robinkirkman.edison.sfo.SFOled;
import com.robinkirkman.edison.sfo.SFOled.Button;

public class Main {
	private static final Map<Button, Command> commands = new EnumMap<>(Button.class);
	static {
		commands.put(Button.UP, Command.HARD_DROP);
		commands.put(Button.DOWN, Command.SHIFT_DOWN);
		commands.put(Button.LEFT, Command.SHIFT_LEFT);
		commands.put(Button.RIGHT, Command.SHIFT_RIGHT);
		commands.put(Button.A, Command.ROTATE_LEFT);
		commands.put(Button.B, Command.ROTATE_RIGHT);
	}
	
	public static void main(String[] args) throws Exception {
		
		Configuration conf = new Configuration(null, 0);
		Field field = new Field();
		Engine engine = new Engine(field, conf);
		engine.setHoldEnabled(true);
		EngineDraw draw = new EngineDraw(engine);
		
		engine.tick(Command.NOP);
		
		Map<Button, Boolean> bp = null;
		Map<Button, Boolean> prevBp = null;
		Map<Button, Integer> held = new EnumMap<>(Button.class);
		
		Command c = Command.NOP;
		while(!engine.isOver()) {
			draw.draw();
			SFOled.display();
			bp = SFOled.pressed(bp);
			for(Button b : bp.keySet()) {
				if(bp.get(b))
					held.put(b, 1 + held.getOrDefault(b, 0));
				else
					held.put(b, 0);
			}
			if(held.get(Button.LEFT) == 1)
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
			Thread.sleep(1000/60);
			Map<Button, Boolean> t = prevBp;
			prevBp = bp;
			bp = t;
		}
		
		draw.draw();
		SFOled.display();
	}
}
