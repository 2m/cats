package se.uu.it.cats.brick.network.packet;

public class MeanAndCovarianceUpdate extends Packet
{
	public byte _type = 0x04;
	
	private float _mean_x;
	private float _mean_y;
	private float _mean_xv;
	private float _mean_yv;
	private float _var_xx;
	private float _var_xy;
	private float _var_yy;
	private float _var_xvxv;
	private float _var_xvyv;
	private float _var_yvyv;
	private float _weight;
	
	public static int LENGTH =
		1		// _type - 1byte
		+ 1		// _src  - 1byte
		+ 4		// _mean_x - 1float - 4bytes
		+ 4		// _mean_y - 1float - 4bytes
		+ 4		// _mean_xv - 1float - 4bytes
		+ 4		// _mean_yv - 1float - 4bytes
		+ 4		// _var_xx - 1float - 4bytes
		+ 4		// _var_xy - 1float - 4bytes
		+ 4		// _var_yy - 1float - 4bytes
		+ 4		// _var_xvxv - 1float - 4bytes
		+ 4		// _var_xvyv - 1float - 4bytes
		+ 4		// _var_yvyv - 1float - 4bytes
		+ 4;	// _weight - 1float - 4bytes
	
	public MeanAndCovarianceUpdate()
	{		
	}
	
	public MeanAndCovarianceUpdate(float mean_x, float mean_y, float mean_xv, float mean_yv,
			float var_xx, float var_xy, float var_yy, float var_xvxv, float var_xvyv, float var_yvyv, float weight)
	{
		_mean_x = mean_x;
		_mean_y = mean_y;
		_mean_xv = mean_xv;
		_mean_yv = mean_yv;
		_var_xx = var_xx;
		_var_xy = var_xy;
		_var_yy = var_yy;
		_var_xvxv = var_xvxv;
		_var_xvyv = var_xvyv;
		_var_yvyv = var_yvyv;
		_weight = weight;
	}
	
	public float getMeanX()
	{
		return _mean_x;
	}
	
	public float getMeanY()
	{
		return _mean_y;
	}
	
	public float getMeanXv()
	{
		return _mean_xv;
	}
	
	public float getMeanYv()
	{
		return _mean_yv;
	}
	
	public float getVarXX()
	{
		return _var_xx;
	}
	
	public float getVarXY()
	{
		return _var_xy;
	}
	
	public float getVarYY()
	{
		return _var_yy;
	}
	
	public float getVarXvXv()
	{
		return _var_xvxv;
	}
	
	public float getVarXvYv()
	{
		return _var_xvyv;
	}
	
	public float getVarYvYv()
	{
		return _var_yvyv;
	}
	
	public float getWeight()
	{
		return _weight;
	}
	
	public void readImpl(byte[] bArr)
	{
		// _type is already defined,
		// no need to read it again from network 
	
		// read byte
		_src = readByte(bArr, 1);
		
		// read float
		_mean_x = readFloat(bArr, 2);
		_mean_y = readFloat(bArr, 6);
		_mean_xv = readFloat(bArr, 10);
		_mean_yv = readFloat(bArr, 14);
		_var_xx = readFloat(bArr, 18);
		_var_xy = readFloat(bArr, 22);
		_var_yy = readFloat(bArr, 26);
		_var_xvxv = readFloat(bArr, 30);
		_var_xvyv = readFloat(bArr, 34);
		_var_yvyv = readFloat(bArr, 38);
		_weight = readFloat(bArr, 42);
	}
	
	public byte[] writeImpl()
	{
		byte[] output = new byte[getLength()];
		
		// write byte
		writeByte(_type, output, 0);
		
		// write byte
		writeByte(_src, output, 1);
		
		// write float
		writeFloat(_mean_x, output, 2);
		writeFloat(_mean_y, output, 6);
		writeFloat(_mean_xv, output, 10);
		writeFloat(_mean_yv, output, 14);
		writeFloat(_var_xx, output, 18);
		writeFloat(_var_xy, output, 22);
		writeFloat(_var_yy, output, 26);
		writeFloat(_var_xvxv, output, 30);
		writeFloat(_var_xvyv, output, 34);
		writeFloat(_var_yvyv, output, 38);
		writeFloat(_weight, output, 42);
		
		return output;
	}
	
	public int getLength()
	{
		return LENGTH;
	}
	
	public String toString()
	{
		return "MeanAndCovarianceUpdate[_type:"+_type+", _src:"+_src+
			", _mean_x:"+_mean_x+
			", _mean_y:"+_mean_y+
			", _mean_xv:"+_mean_xv+
			", _mean_yv:"+_mean_yv+
			", _var_xx:"+_var_xx+
			", _var_xy:"+_var_xy+
			", _var_yy:"+_var_yy+
			", _var_xvxv:"+_var_xvxv+
			", _var_xvyv:"+_var_xvyv+
			", _var_yvyv:"+_var_yvyv+
			", _weight:"+_weight+
			"]";
	}
}
