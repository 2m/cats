package se.uu.it.cats.pc.actor;

public class Mouse {
	private float _x; // Absolute position horizontal axis
	private float _y; // Absolute position vertical axis
	
	private int bufferLength = 20;
	private float[] bufferX = new float[bufferLength];
	private float[] bufferY = new float[bufferLength];
	private int posBuffer = 0;
	
	public Mouse() {
	}
	public void newPosition(float x, float y) {
		_x = x;
		_y = y;
		
		//Position buffer
		if(posBuffer == bufferLength-1) 
			posBuffer = 0;
		bufferX[posBuffer] = _x;
		bufferY[posBuffer] = _y;
		posBuffer++;
		
	}
	
	public float[] getBufferX() {
		return bufferX;
	}
	public float[] getBufferY() {
		return bufferY;
	}
	public int getBufferLength() {
		return bufferLength;
	}
	public int getPosBuffer() {
		return posBuffer;
	}
	
	public int getX() {
		// convert meters to centimeters for the GUI
		return (int)(_x*100);
	}
	
	public int getY() {
		// convert meters to centimeters for the GUI
		return (int)(_y*100);
	}
	
	public float getFloatX() {
		return _x;
	}
	
	public float getFloatY() {
		return _y;
	}
}