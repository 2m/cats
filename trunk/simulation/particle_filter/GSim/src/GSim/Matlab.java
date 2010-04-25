package GSim;

import lejos.util.Matrix;

/**
 * A collection of Matlab look-a-like functions, 
 * together with some other matrix operations missing in lejos.util.Matrix such as print matrix.
 * Most methods are built upon lejos.util.Matrix .
 * @author Edvard
 */
public abstract class Matlab {
		
	/**
	 * 	Create a diagonal matrix out of a (1)x(n) matrix
	 * @param m (1)x(n) matrix
	 * @return (n)x(n) diagonal matrix
	 */
	public static Matrix diag(Matrix m)
	{
		Matrix m_diag = Matrix.identity(m.getColumnDimension(), m.getColumnDimension());
		//for all rows&coulmns
		for (int idx=0; idx<m.getColumnDimension(); idx++)  
		{
			m_diag.set(idx, idx, m.get(0, idx));
		}
		return m_diag;	
	}
	
	/**
	 * Creates a with matrix with ones
	 * @param rows number of rows
	 * @param columns number of columns
	 * @return matrix with ones
	 */
	public static Matrix ones(int rows, int columns)
	{
		return new Matrix(rows,columns,1);
	}
	
	/**
	 * Creates a with matrix with zeroes
	 * @param rows number of rows
	 * @param columns number of columns
	 * @return matrix with ones
	 */
	public static Matrix zeros(int rows, int columns)
	{
		return new Matrix(rows,columns,0);
	}
	
	/**
	 * Returns an identity matrix of size n
	 * @param n size
	 * @return identity matrix
	 */
	public static Matrix eye(int n)
	{
		return Matrix.identity(n, n);
	}
	
	
	/**
	 * Converts a matrix to a string ready to be printed
	 * The values are rounded to floats (from doubles).
	 * @param m  input matrix
	 * @return a string representation of the matrix
	 */
	public static String MatrixToString(Matrix m)
	{
		double[][] print_m = m.getArray();
		String s = "";
		for (int i=0; i<m.getRowDimension(); i++)
		{
			for (int j=0; j<m.getColumnDimension(); j++)
			{
				s += print_m[i][j] + "  ";
			}
			s += "\n";
		}
		return s;
	}
	
	/**
	 * Prints a matrix.
	 * @param m input matrix
	 */
	public static void printM(Matrix m){
		System.out.println(MatrixToString(m));
	}

}
