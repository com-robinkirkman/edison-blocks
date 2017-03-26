package org.esialb.edison.blocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import org.eviline.core.Engine;
import org.eviline.core.Field;
import org.eviline.core.Shape;
import org.eviline.core.ShapeType;
import org.eviline.core.XYShapes;

import com.robinkirkman.edison.sfo.OledDataBuffer;
import com.robinkirkman.edison.sfo.OledImage;
import com.robinkirkman.edison.sfo.OledRaster;
import com.robinkirkman.edison.sfo.SFOled;

public class EngineDraw {
	protected static int drawCount = 0;

	protected OledImage image;
	protected Graphics2D g;
	
	
	protected Engine engine;
	
	public EngineDraw(Engine engine) {
		this.engine = engine;
		image = new OledImage();
		g = image.createGraphics();
		g.setFont(Font.decode(Font.MONOSPACED).deriveFont(10f));
	}
	
	public void draw() {
		OledDataBuffer odb = (OledDataBuffer) image.getRaster().getDataBuffer();
		SFOled.read(odb.getBuffer());
		
		drawCount++;
		
		Field field = engine.getField();
		
		int bh = 3 * field.HEIGHT;
		int bw = 3 * field.WIDTH;
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 64, 48);
		g.setColor(Color.WHITE);
		g.drawRect(0, 0, bh+1, bw+1);
		for(int y = 0; y < field.HEIGHT; y++) {
			for(int x = 0; x < field.WIDTH; x++) {
				boolean fill = false;
				if(field.masked(x, y))
					fill = true;
				if(engine.getShape() != -1 && XYShapes.has(engine.getShape(), x, y))
					fill = true;
				if(fill) {
					g.fillRect(1+3*y, 1+3*(field.WIDTH - x - 1), 3, 3);
				}
			}
		}
		
		ShapeType[] next = engine.getNext();
		if(next.length > 0)
			drawShapeType(next[0], 0, 36);
		
		ShapeType held = engine.getHold();
		if(held != null)
			drawShapeType(held, 58, 36);
		
		g.drawString("" + engine.getLines(), 12, 47);
		
		SFOled.write(odb.getBuffer());
	}
	
	private void drawShapeType(ShapeType type, int x, int y) {
		Shape shape = type.start();
		for(int ix = 0; ix < 4; ix++) {
			for(int iy = 0; iy < 4; iy++) {
				if(shape.has(3 - iy, ix))
					g.fillRect(x+3*ix, y + 3*iy, 3, 3);
			}
		}
	}
}
