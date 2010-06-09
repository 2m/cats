package GSim;

import lejos.util.Matrix;
//import se.uu.it.cats.brick.filter.Matlab;
//import se.uu.it.cats.brick.Logger;

/*Below is the syntax highlighted version of Cholesky.java 
 *from 9.4 Numerical Solutions to Differential Equations. 
 *Copyright 2007, Robert Sedgewick and Kevin Wayne. 
 *Last updated: Tue Sep 29 16:17:41 EDT 2009. 
 */
/*************************************************************************
 *  Compilation:  javac Cholesky.java
 *  Execution:    java Cholesky
 * 
 *  Compute Cholesky decomposition of symmetric positive definite
 *  matrix A = LL^T.
 *
 *  % java Cholesky
 *  2.00000  0.00000  0.00000 
 *  0.50000  2.17945  0.00000 
 *  0.50000  1.26179  3.62738 
 *
 *************************************************************************/
public class Cholesky {
    private static final double EPSILON = 1e-10;
    private static final int DEBUG = 1; //0 = no debug, 1 = debug on PC, 2 = debug on brick

    // is symmetric
    public static boolean isSymmetric(double[][] A) {
		debug("A = ");
    	debug(Matlab.MatrixToString(new Matrix(A)));
        int N = A.length;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < i; j++) {
            	if (A[i][j] == Double.NaN || A[j][i] == Double.NaN)
            	{
                	debug("Matrix contains NaN");
                	debug("A = ");
                	debug(Matlab.MatrixToString(new Matrix(A)));
            		throw new RuntimeException("Matrix contains NaN");	
            	}
                if (A[i][j] - A[j][i] > EPSILON) 
                	return false;  //updated so that rounding errors doesn't make it fail
                else
                	A[i][j] = A[j][i];
            }
        }
        return true;
    }

    // is symmetric
    public static boolean isSquare(double[][] A) {
        int N = A.length;
        for (int i = 0; i < N; i++) {
            if (A[i].length != N) return false;
        }
        return true;
    }


    // return Cholesky factor L of psd matrix A = L L^T
    public static double[][] cholesky(double[][] A) {
    	debug("Entering Cholesky"); 
        if (!isSquare(A)) {
        	debug("Matrix is not square");
        	debug("A = ");
        	debug(Matlab.MatrixToString(new Matrix(A)));
            throw new RuntimeException("Matrix is not square");
        }
        if (!isSymmetric(A)) {
        	debug("Matrix is not symmetric");      	
        	debug("A = ");
        	debug(Matlab.MatrixToString(new Matrix(A)));
            throw new RuntimeException("Matrix is not symmetric");
        }

        int N  = A.length;
        double[][] L = new double[N][N];

        for (int i = 0; i < N; i++)  {
            for (int j = 0; j <= i; j++) {
                double sum = 0.0;
                for (int k = 0; k < j; k++) {
                    sum += L[i][k] * L[j][k];
                }
                if (i == j) L[i][i] = Math.sqrt(A[i][i] - sum);
                else        L[i][j] = 1.0 / L[j][j] * (A[i][j] - sum);
            }
            if (L[i][i] <= 0) {
            	debug("Matrix not positive definite: L["+i+"]["+i+"] = " + L[i][i] + " <= 0");
            	debug("L = ");
            	debug(Matlab.MatrixToString(new Matrix(L)));            	
            	debug("A = ");
            	debug(Matlab.MatrixToString(new Matrix(A)));
                throw new RuntimeException("Matrix not positive definite");
            }
        }
        checkForNaN(L);
        debug("Leaving Cholesky"); 
        return L;
    }
    
    private static void checkForNaN(double[][] A)
    {
    	int N = A.length;
    	for (int i = 0; i < N; i++) {
    		for (int j = 0; j < i; j++) {
    			if (Double.isNaN(A[i][j]))
    			{
    				debug("Matrix contains NaN");
    				debug("A = ");
    				debug(Matlab.MatrixToString(new Matrix(A)));
    				throw new RuntimeException("Matrix contains NaN");	
    			}
    		}
    	}        
    }
    
    private static void debug(String s)
    {
    	if (DEBUG == 0)
    		return;
    	if(DEBUG == 1){
    		System.out.println(s);		
    	}
    	else 
    		Logger.println(s);		
    }
    
    /*
    // sample client
    public static void main(String[] args) {
        int N = 3;
        double[][] A = { { 4, 1,  1 },
                         { 1, 5,  3 },
                         { 1, 3, 15 }
                       };
        double[][] L = cholesky(A);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                System.out.printf("%8.5f ", L[i][j]);
            }
            System.out.println();
        }

    }*/

}//End of class
