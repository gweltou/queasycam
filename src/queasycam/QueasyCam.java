/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package queasycam;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.GraphicsEnvironment;
import java.util.HashMap;
import processing.core.*;
import processing.core.PConstants;
import processing.event.KeyEvent;
import processing.opengl.PGraphicsOpenGL;

public class QueasyCam {
	public final static String VERSION = "##library.prettyVersion##";

	public boolean controllable;
	public float speed;
	public float sensitivity;
	public PVector position;
	public float pan;
	public float tilt;
	public PVector velocity;
	public float friction;
	public char key_forward = 'w';
	public char key_backward = 's';
	public char key_left = 'a';
	public char key_right = 'd';
	public char key_up = 'q';
	public char key_down = 'e';

	private final PApplet applet;
	private Robot robot;
	private PVector center;
	private PVector up;
	private PVector right;
	private PVector forward;
    private PVector target;
	private Point mouse;
	private Point prevMouse;
	private final HashMap<Character, Boolean> keys;

	public QueasyCam(PApplet applet){
		this.applet = applet;
		applet.registerMethod("draw", this);
		applet.registerMethod("keyEvent", this);
		
		try {
			robot = new Robot();
		} catch (Exception e){}

		controllable = true;
		speed = 3f;
		sensitivity = 2f;
		position = new PVector(0f, 0f, 0f);
		up = new PVector(0f, 1f, 0f);
		right = new PVector(1f, 0f, 0f);
		forward = new PVector(0f, 0f, 1f);
		velocity = new PVector(0f, 0f, 0f);
		pan = 0f;
		tilt = 0f;
		friction = 0.75f;
		keys = new HashMap<Character, Boolean>();

		applet.perspective(PConstants.PI/3f, (float)applet.width/(float)applet.height, 0.01f, 1000f);
	}

	public QueasyCam(PApplet applet, float fov){
		this(applet);
		applet.perspective(fov, (float)applet.width/(float)applet.height, 0.01f, 1000f);
	}
    
    public QueasyCam(PApplet applet, float near, float far){
        this(applet);
        applet.perspective(PConstants.PI/3f, (float)applet.width/(float)applet.height, near, far);
    }

	public QueasyCam(PApplet applet, float fov, float near, float far){
		this(applet);
		applet.perspective(fov, (float)applet.width/(float)applet.height, near, far);
	}

	public void draw(){
		if (!controllable) return;
		
		mouse = MouseInfo.getPointerInfo().getLocation();
		if (prevMouse == null) prevMouse = new Point(mouse.x, mouse.y);
		
		int w = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
		int h = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
		
		if (mouse.x < 1 && (mouse.x - prevMouse.x) < 0){
			robot.mouseMove(w-2, mouse.y);
			mouse.x = w-2;
			prevMouse.x = w-2;
		}
				
		if (mouse.x > w-2 && (mouse.x - prevMouse.x) > 0){
			robot.mouseMove(2, mouse.y);
			mouse.x = 2;
			prevMouse.x = 2;
		}
		
		if (mouse.y < 1 && (mouse.y - prevMouse.y) < 0){
			robot.mouseMove(mouse.x, h-2);
			mouse.y = h-2;
			prevMouse.y = h-2;
		}
		
		if (mouse.y > h-1 && (mouse.y - prevMouse.y) > 0){
			robot.mouseMove(mouse.x, 2);
			mouse.y = 2;
			prevMouse.y = 2;
		}
		
		pan += PApplet.map(mouse.x - prevMouse.x, 0, applet.width, 0, PConstants.TWO_PI) * sensitivity;
		tilt += PApplet.map(mouse.y - prevMouse.y, 0, applet.height, 0, PConstants.PI) * sensitivity;
		tilt = clamp(tilt, -PConstants.PI/2.01f, PConstants.PI/2.01f);
		
		if (tilt == PConstants.PI/2) tilt += 0.001f;

		forward = new PVector(PApplet.cos(pan), PApplet.tan(tilt), PApplet.sin(pan));
		forward.normalize();
		right = new PVector(PApplet.cos(pan - PConstants.PI/2), 0, PApplet.sin(pan - PConstants.PI/2));
        
        target = PVector.add(position, forward);
		
		prevMouse = new Point(mouse.x, mouse.y);
		
		if (keys.containsKey(key_left) && keys.get(key_left)) velocity.add(PVector.mult(right, speed));
		if (keys.containsKey(key_right) && keys.get(key_right)) velocity.sub(PVector.mult(right, speed));
		if (keys.containsKey(key_forward) && keys.get(key_forward)) velocity.add(PVector.mult(forward, speed));
		if (keys.containsKey(key_backward) && keys.get(key_backward)) velocity.sub(PVector.mult(forward, speed));
		if (keys.containsKey(key_down) && keys.get(key_down)) velocity.add(PVector.mult(up, speed));
		if (keys.containsKey(key_up) && keys.get(key_up)) velocity.sub(PVector.mult(up, speed));

		velocity.mult(friction);
		position.add(velocity);
		center = PVector.add(position, forward);
		applet.camera(position.x, position.y, position.z, center.x, center.y, center.z, up.x, up.y, up.z);
	}
	
	public void keyEvent(KeyEvent event){
		char key = event.getKey();
		
		switch (event.getAction()){
			case KeyEvent.PRESS: 
				keys.put(Character.toLowerCase(key), true);
				break;
			case KeyEvent.RELEASE:
				keys.put(Character.toLowerCase(key), false);
				break;
		}
	}

	private boolean pushedLights = false;

    public void beginHUD(){
		applet.g.hint(PConstants.DISABLE_DEPTH_TEST);
		applet.g.pushMatrix();
		applet.g.resetMatrix();
		if (applet.g.isGL() && applet.g.is3D()) {
			PGraphicsOpenGL pgl = (PGraphicsOpenGL) applet.g;
			pushedLights = pgl.lights;
			pgl.lights = false;
			pgl.pushProjection();
			applet.g.ortho(0, applet.g.width, -applet.g.height, 0, -Float.MAX_VALUE, +Float.MAX_VALUE);
		}
    }
    
    public void endHUD(){
		if (applet.g.isGL() && applet.g.is3D()) {
			PGraphicsOpenGL pgl = (PGraphicsOpenGL) applet.g;
			pgl.popProjection();
			pgl.lights = pushedLights;
		}
		applet.g.popMatrix();
		applet.g.hint(PConstants.ENABLE_DEPTH_TEST);
    }
	
	private float clamp(float x, float min, float max){
		if (x > max) return max;
		if (x < min) return min;
		return x;
	}
	
	public PVector getForward(){
		return forward;
	}
	
	public PVector getUp(){
		return up;
	}
	
	public PVector getRight(){
		return right;
	}
    
    public PVector getTarget(){
        return target;
    }
    
}

