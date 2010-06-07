package GSim;

import lejos.util.Matrix;
import static GSim.Matlab.*;

public class MatlabTest {
	
	public static void main(String[] arg)
	{
	double[][] temp_m = {{0.091976381876777,  -0.008867901387261,   0.146025265002760,  -0.003003741071050},
			{-0.008867901387261,   0.128394398537096,  -0.002586719730546,   0.168108766741639},
			{0.146025265002760,  -0.002586719730545,   0.314483115935252,   0.044025746742871},
			{-0.003003741071050,   0.168108766741639,   0.044025746742871,   0.342995645416571}};
	Matrix m = new Matrix(temp_m); 
	//System.out.println(temp_m.length);
	
	Matrix m_diag = diagFromMatrix(m);
	printM(m_diag);
	double[][] temp_expected_result = {{	0.091976381876777,  0.0,  0.0,  0.0 },  
								  { 0.0,  0.128394398537096,  0.0,  0.0 },  
							      { 0.0,  0.0,  0.314483115935252,  0.0 },  
								  { 0.0,  0.0,  0.0,  0.342995645416571 }};  
	Matrix expected_result = new Matrix(temp_expected_result); 
	System.out.println("Excpected results: ");
	printM(expected_result);
	
	
	
	double[][] temp_m2 = {{-0.003003741071050,   0.168108766741639,   0.044025746742871,   0.342995645416571}};
	Matrix m2 = new Matrix(temp_m2); 
	//System.out.println(temp_m.length);
	
	Matrix m_diag2 = diagFromColumn(m2);
	printM(m_diag2);
	double[][] temp_expected_result2 = {{	-0.00300374107105,  0.0,  0.0,  0.0 },  
								  { 0.0,  0.168108766741639,  0.0,  0.0 },  
							      { 0.0,  0.0,  0.044025746742871,  0.0 },  
								  { 0.0,  0.0,  0.0,  0.342995645416571 }};  
	Matrix expected_result2 = new Matrix(temp_expected_result2); 
	System.out.println("Excpected results: ");
	printM(expected_result2);
	}

}
