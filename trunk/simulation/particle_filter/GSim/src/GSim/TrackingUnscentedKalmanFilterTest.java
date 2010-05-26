package GSim;

import lejos.util.Matrix;
import static java.lang.Math.*;
import static GSim.Matlab.*;

public class TrackingUnscentedKalmanFilterTest {

	//NB: This test worked ok 2010-05-24 on revison 478 in GSim
	//Important: Set float stddegrees = 0.1f; and float dt = 1.0f; in TrackingUnscentedKalmanFilter before running this test

	public static void main(String[] arg)
	{
		System.out.println("Running TestTrackingUnscentedKalmanFilter");
		
		int id = 1;
		int timestepsBetweenFilterUpdates = 5;
		float T = (float)(100.0*5/1000.0);//(float) GSim.timestep * timestepsBetweenFilterUpdates / 1000;
		Buffer buffer = new BufferSorted();
		BillBoard billboard = new BillBoard(3);
		
		TrackingUnscentedKalmanFilter trackingFilter = new TrackingUnscentedKalmanFilter(id, T, billboard);
		
		
		//states 1 (x1), k = 13 in the Matlab sim of mainUKFmerge with 0 camera offset
		int time = Clock.timestamp();
		trackingFilter.initData(1.335855428082178f, 1.203333865549188f, -0.014289338603084f, 0.008865245949793f, time);  //x, y, xv, xy, NB: sets lastCurrentTime to Clock()  
		
		//state covariance 1 (P1)
		double[][] temp_P1_correct = {{0.091976381876777,  -0.008867901387261,   0.146025265002760,  -0.003003741071050},
									{-0.008867901387261,   0.128394398537096,  -0.002586719730546,   0.168108766741639},
									{0.146025265002760,  -0.002586719730545,   0.314483115935252,   0.044025746742871},
									{-0.003003741071050,   0.168108766741639,   0.044025746742871,   0.342995645416571}};
		Matrix P1_correct = new Matrix(temp_P1_correct); 
		P1_correct.timesEquals(0.0001);
		trackingFilter.setStateCovariance(P1_correct);
				
		billboard.setAbsolutePosition(1, 0.363795125006558f, 0.216067986651408f, -0.124804764073335f, time +100);
		billboard.setAbsolutePosition(2, 1.638056992700254f, 1.789290480544979f, -3.095256852035598f, time +100);
		billboard.setAbsolutePosition(3, 1.802691755315792f, 0.349717175671003f,  1.603020253504810f, time +100);		      
		//other angle (cam?) 0.765110147757594   2.199999999999884   2.049976488387934
		
		//NB: x and y are set to the same as in above
		billboard.setLatestSighting(1, 0.363795125006558f, 0.216067986651408f, 0.808272447212222f, time +100);  //Sees the mouse
		billboard.setLatestSighting(2, 1.638056992700254f, 1.789290480544979f, 3.141592653589793f, time -100);  //Doesn't see the mouse
		billboard.setLatestSighting(3, 1.802691755315792f, 0.349717175671003f, 2.072717556964165f, time +100);	//Sees the mouse
		
		//Check that the initial values where set correctcly
		System.out.println("Inputs");
		System.out.println("x = " + trackingFilter.getX() + ", y = " + trackingFilter.getY() );
		Matrix states1 = trackingFilter.getStates();
		System.out.println("Debug: tracking.ukf.test cat " +id + ", states1 dim: " + states1.getRowDimension() + " x " + states1.getColumnDimension() + ", mouse states1:");
		printM(states1);
		Matrix P1 = trackingFilter.getStateCovariance();
		System.out.println("Debug: tracking.ukf.test cat " +id + ", P1 dim: " + P1.getRowDimension() + " x " + P1.getColumnDimension() + ", mouse P1:");
		printM(P1);		
		
		//Run one iteration/update
		System.out.println("Running one iteration/update");
		trackingFilter.update();
		
		//Results
		System.out.println("Results");
		System.out.println("x = " + trackingFilter.getX() + ", y = " + trackingFilter.getY() );
		Matrix states2 = trackingFilter.getStates();
		System.out.println("Debug: tracking.ukf.test cat " +id + ", states2 dim: " + states2.getRowDimension() + " x " + states2.getColumnDimension() + ", mouse states2:");
		printM(states2);
		Matrix P2 = trackingFilter.getStateCovariance();
		System.out.println("Debug: tracking.ukf.test cat " +id + ", P2 dim: " + P2.getRowDimension() + " x " + P2.getColumnDimension() + ", mouse P2:");
		printM(P2);	
		
		//Expected results
		System.out.println("Expected results");
		double[][] temp_states2_correct = {{1.324238074594888},
											{1.220772793110461},
											{-0.012297694714087},
											{0.014366029399230}};
		Matrix states2_correct = new Matrix(temp_states2_correct);   
		double[][] temp_P2_correct = {{0.091836354448210,  -0.009125033257649,   0.144071925085492,  -0.004801538172275},
									{-0.009125033257649,   0.124265596610243,  -0.004311671358678,   0.163771564695871},
									{0.144071925085492,  -0.004311671358678,   0.272728812065396,   0.001021592835730},
									{-0.004801538172275,   0.163771564695871,   0.001021592835730,   0.293191567040436}};
		Matrix P2_correct = new Matrix(temp_P2_correct);  
		P2_correct.timesEquals(0.0001);
		System.out.println("Debug: tracking.ukf.test cat " +id + ", states2 correct dim: " + states2_correct.getRowDimension() + " x " + states2_correct.getColumnDimension() + ", mouse states2 correct:");
		printM(states2_correct);
		System.out.println("Debug: tracking.ukf.test cat " +id + ", P2 correct dim: " + P2_correct.getRowDimension() + " x " + P2_correct.getColumnDimension() + ", mouse P2 correct:");
		printM(P2_correct);	
		
		
		
		/*
K>> k

k =

    13

K>> xm

xm =

   1.335855428082178
   1.203333865549188
  -0.014289338603084
   0.008865245949793

K>> Pm

Pm =

  1.0e-004 *

   0.091976381876777  -0.008867901387261   0.146025265002760  -0.003003741071050
  -0.008867901387261   0.128394398537096  -0.002586719730546   0.168108766741639
   0.146025265002760  -0.002586719730545   0.314483115935252   0.044025746742871
  -0.003003741071050   0.168108766741639   0.044025746742871   0.342995645416571

K>> x

x =

   0.363795125006558   1.638056992700254   1.802691755315792
   0.216067986651408   1.789290480544979   0.349717175671003
   0.012644501792868  -0.013321990812214   0.001255381688827
  -0.000949617277024  -0.004937635566955   0.013153576282474
  -0.124804764073335  -3.095256852035598   1.603020253504810
   0.765110147757594   2.199999999999884   2.049976488387934

K>> actCats

actCats =

     1     3

K>> zm

zm =

   0.808272447212222
   3.141592653589793
   2.072717556964165

K>> k

k =

    14

K>> xm

xm =

   1.324238074594888
   1.220772793110461
  -0.012297694714087
   0.014366029399230

K>> Pm

Pm =

  1.0e-004 *

   0.091836354448210  -0.009125033257649   0.144071925085492  -0.004801538172275
  -0.009125033257649   0.124265596610243  -0.004311671358678   0.163771564695871
   0.144071925085492  -0.004311671358678   0.272728812065396   0.001021592835730
  -0.004801538172275   0.163771564695871   0.001021592835730   0.293191567040436

K>> x

x =

   0.371142316071703   1.627124969028877   1.803191618580991
   0.214246580090804   1.798759144608364   0.355199735908482
   0.011141445944987  -0.011980084592937   0.000824315206839
  -0.000823307421442   0.003151134503447   0.010425640241896
  -0.041973452355884  -2.255573429076776   1.221687303164684
   0.785959009684936   2.399999999999971   2.062358816731698

K>> actCats

actCats =

     1     3

K>> zm

zm =

   0.827104598452806
   3.141592653589793
   2.080580301654018

K>> Qm

Qm =

  1.0e-004 *

   0.062500000000000                   0   0.125000000000000                   0
                   0   0.062500000000000                   0   0.125000000000000
   0.125000000000000                   0   0.250000000000000                   0
                   0   0.125000000000000                   0   0.250000000000000

K>> Rm

Rm =

  1.0e+009 *

   0.000000000000003                   0                   0
                   0   1.000000000000000                   0
                   0                   0   0.000000000000003

K>> 
		*/




		
	}
	


}
