package org.esialb.edison.blocks;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.eviline.core.Engine;
import org.eviline.core.Field;
import org.eviline.core.Shape;
import org.eviline.core.ShapeType;
import org.eviline.core.XYShapes;
import org.eviline.swing.ShapeTypeColor;
import org.esialb.edison.sfo.OledDataBuffer;
import org.esialb.edison.sfo.OledImage;
import org.esialb.edison.sfo.OledRaster;
import org.esialb.edison.sfo.SFOled;
import org.esialb.teensy.i2c.master.SSD1331;

public class EngineDraw {
	protected OledImage image;
	protected Graphics2D g;
	
	
	protected Engine engine;
	
	protected SSD1331 gfx;
	
	public EngineDraw(Engine engine) {
		this(engine, null);
	}
	
	public EngineDraw(Engine engine, SSD1331 gfx) {
		this.engine = engine;
		this.gfx = gfx;
		image = SFOled.createImage();
		g = image.createGraphics();
		g.setFont(Font.decode(Font.MONOSPACED).deriveFont(10f));
	}
	
	public void paint() {
		
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
				else if(engine.getShape() != -1 && XYShapes.has(engine.getShape(), x, y))
					fill = true;
				if(fill) {
					g.fillRect(1+3*y, 31 - (1+3*(field.WIDTH - x - 1)), 3, 3);
				} else if(engine.getGhost() != -1 && XYShapes.has(engine.getGhost(), x, y))
					g.drawRect(2+3*y, 31 - (2+3*(field.WIDTH - x - 1)), 0, 0);

			}
		}
		
		ShapeType[] next = engine.getNext();
		if(next.length > 0 && next[0] != null)
			drawShapeType(next[0], 0, 36);
		
		ShapeType held = engine.getHold();
		if(held != null)
			drawShapeType(held, 58, 36);
		
		g.drawString("" + engine.getLines(), 12, 47);

		if(engine.isOver()) {
			int xo = 2;
			int yo = 9;
			int w = g.getFontMetrics().stringWidth("Game Over") + 2;
			int h = g.getFontMetrics().getHeight();
			g.setColor(Color.BLACK);
			g.fillRect(xo + 0, yo + 0, w + 2, h + 0);
			g.setColor(Color.WHITE);
			g.drawRect(xo + 0, yo + 0, w + 1, h - 1);
			g.drawString("Game Over", xo + 2, yo + h - g.getFontMetrics().getDescent());
		}
		
		image.paint();
		
		paintGfx();
	}
	
	private ShapeTypeColor shapeTypeColor = new ShapeTypeColor();
	private BufferedImage gfxToImage;
	private Image gfxFromImage;
	
	private void paintGfx() {
		if(gfx == null)
			return;
		
		Field field = engine.getField();
		
		int bh = 3 * field.HEIGHT;
		int bw = 3 * field.WIDTH;
		
		gfxToImage = new BufferedImage(96, 64, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = gfxToImage.createGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 96, 64);
		g.setColor(Color.DARK_GRAY);
		g.drawRect(0, 0, bw+1, bh+1);
		for(int y = 0; y < field.HEIGHT; y++) {
			for(int x = 0; x < field.WIDTH; x++) {
				boolean fill = false;
				Color c = null;
				if(field.masked(x, y)) {
					c = shapeTypeColor.get(field.block(x, y).shape().type());
					fill = true;
				} else if(engine.getShape() != -1 && XYShapes.has(engine.getShape(), x, y)) {
					c = shapeTypeColor.get(XYShapes.shapeFromInt(engine.getShape()).type());
					fill = true;
				}
				if(fill) {
					g.setColor(c);
					g.fillRect(1+3*(field.WIDTH - x - 1), 1+3*y, 3, 3);
				} else if(engine.getGhost() != -1 && XYShapes.has(engine.getGhost(), x, y)) {
					c = shapeTypeColor.get(XYShapes.shapeFromInt(engine.getShape()).type());
					g.setColor(c);
					g.drawRect(1+3*(field.WIDTH - x - 1), 1+3*y, 2, 2);
				}
			}
		}

		g.setColor(Color.BLACK);
		g.fillRect(68, 20, 28, 24);
		g.setColor(Color.WHITE);
		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
		g.drawString("" + engine.getLines(), 68, 38);
		
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
		g.drawString("NEXT", 68, 8);
		g.drawString("HOLD", 68, 63);

		if(engine.isOver()) {
			int xo = 2;
			int yo = 9;
			int w = g.getFontMetrics().stringWidth("Game Over") + 2;
			int h = g.getFontMetrics().getHeight();
			g.setColor(Color.BLACK);
			g.fillRect(xo + 0, yo + 0, w + 2, h + 0);
			g.setColor(Color.WHITE);
			g.drawRect(xo + 0, yo + 0, w + 1, h - 1);
			g.drawString("Game Over", xo + 2, yo + h - g.getFontMetrics().getDescent());
		}

		ShapeType[] next = engine.getNext();
		if(next.length > 0 && next[0] != null)
			drawShapeTypeGfx(next[0], 40, 4);
		
		ShapeType held = engine.getHold();
		if(held != null)
			drawShapeTypeGfx(held, 40, 36);
		
		gfxFromImage = gfx.drawImage(gfxToImage, gfxFromImage);
		
	}
	
	private void drawShapeType(ShapeType type, int x, int y) {
		Shape shape = type.start();
		for(int ix = 0; ix < 4; ix++) {
			for(int iy = 0; iy < 4; iy++) {
				if(shape.has(iy, ix))
					g.fillRect(x+3*ix, y + 3*iy, 3, 3);
			}
		}
	}
	
	private void drawShapeTypeGfx(ShapeType type, int x, int y) {
		Shape shape = type.start();
		int minY = 4, maxY = -1;
		for(int ix = 0; ix < 4; ix++) {
			for(int iy = 0; iy < 4; iy++) {
				if(shape.has(3 - ix, iy)) {
					minY = Math.min(minY, iy);
					maxY = Math.max(maxY, iy);
				}
			}
		}
		
		int pixelHeight = 6 * (maxY - minY + 1);
		int offsetY = (24 - pixelHeight) / 2 - 6 * minY;
		
		for(int ix = 0; ix < 4; ix++) {
			for(int iy = 0; iy < 4; iy++) {
				Color c = (shape.has(3 - ix, iy) ? shapeTypeColor.get(type) : Color.BLACK);
				Graphics2D g = gfxToImage.createGraphics();
				g.setColor(c);
				g.fillRect(x+6*ix, offsetY + y + 6*iy, 6, 6);
			}
		}
	}
	
	public void clear() {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		image.paint();
		gfx.fillRect(0, 0, 96, 64, Color.BLACK);
		gfxFromImage = null;
	}
}
