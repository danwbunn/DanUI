import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;


// This is going to take a lot of work
// This is where we get the slider usable on a high resolution screen.

public class TallSliderUI extends BasicSliderUI {

	int scaleFactor;
	
    public TallSliderUI(JSlider b, int scaleFactor) {
		super(b);
		this.scaleFactor = scaleFactor;
	}

    public void paint(Graphics g, JComponent c) {
        Graphics2D g2d = 		(Graphics2D) g;
        g2d.setRenderingHint	(RenderingHints.KEY_ANTIALIASING, 
                				 RenderingHints.VALUE_ANTIALIAS_ON);
        super.paint(g, c);
    }
    
    protected Dimension getThumbSize() {
       return new Dimension(11 * scaleFactor, 20 * scaleFactor);    	
    }

    
    protected int getTickLength() {
    	return super.getTickLength()*scaleFactor;
    }
 
    protected void scrollDueToClickInTrack(int direction) {
        // this is the default behaviour, let's comment that out
        //scrollByBlock(direction);

    	int location = slider.getValue();
        int value = this.valueForXPosition(slider.getMousePosition().x);        
        if (value > location) { slider.setValue(location+1); }
        else if (value < location) { slider.setValue(location-1); }
    }
    
    // this is so awful
    // had to override this whole mess just to add one line to change color
    
      public void paintThumb(Graphics g) {
         Color saved_color = g.getColor();
     
         Point a = new Point(thumbRect.x, thumbRect.y);
         Point b = new Point(a);	Point c = new Point(a);
         Point d = new Point(a);    Point e = new Point(a);
     
         Polygon bright;
         Polygon light; // light shadow
         Polygon dark; // dark shadow
         Polygon all;
     
         // This will be in X-dimension if the slider is inverted and y if it isn't.
         // No it won't becuase I killed the if statement for clarity
         int turnPoint;
     
         turnPoint = thumbRect.height * 3 / 4;
     
         b.translate(thumbRect.width - 1, 0);
         c.translate(thumbRect.width - 1, turnPoint);
         d.translate(thumbRect.width / 2 - 1, thumbRect.height - 1);
         e.translate(0, turnPoint);
 
         bright = new Polygon(new int[] { b.x - 1, a.x, e.x, d.x },
                              new int[] { b.y, a.y, e.y, d.y }, 4);
 
         dark = new Polygon(new int[] { b.x, c.x, d.x + 1 }, 
        		 			new int[] { b.y, c.y - 1, d.y }, 3);
 
         light = new Polygon(new int[] { b.x - 1, c.x - 1, d.x + 1 },
                             new int[] { b.y + 1, c.y - 1, d.y - 1 }, 3);
 
         all = new Polygon(new int[] { a.x + 1, b.x - 2, c.x - 2, d.x, e.x + 1 },
                           new int[] { a.y + 1, b.y + 1, c.y - 1, d.y - 1, e.y }, 5);
     
     g.setColor(Color.WHITE); 		g.drawPolyline(bright.xpoints, bright.ypoints, bright.npoints); 
     g.setColor(Color.BLACK); 		g.drawPolyline(dark.xpoints, dark.ypoints, dark.npoints);
     g.setColor(Color.GRAY);  		g.drawPolyline(light.xpoints, light.ypoints, light.npoints);
     g.setColor(Color.LIGHT_GRAY); 	g.drawPolyline(all.xpoints, all.ypoints, all.npoints);
     g.setColor(Color.BLACK);		g.fillPolygon(all);
     g.setColor(saved_color);
   }
}