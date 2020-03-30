import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Main {	
	
	static final long SEED = 1L;
	static Random RNG = new Random(SEED);
	
	static final int NFLAG = 1;
	
	static final double v = 2d;
	static final double D = 2d;
	static final double d = 0.3d/60d;
	static final int NBASE = 150;
	static final double k = 1d/60d;
	static final double a = 2e-4d;
	
	static final double tMax = 10000;
	static final double dt = 0.02;
	
	//Tubulin Pool
	static double POOL = NFLAG*30;
		
	static List<Double> FLAGELLA = new LinkedList<Double>();
	static List<Transport> IFT = new ArrayList<Transport>();	
	
	public static void main(String args[]) {
			
		for(int x = 0; x < NBASE; x++) {
			IFT.add(new Transport());			
		}
		
		for(int x = 0; x < NFLAG; x++) {
			FLAGELLA.add(0d);
		}
		
		for(double t = 0; t < tMax; t += dt) {
									
		}
		
		System.out.println(k * dt * countFree());
		
	}

	public static int countFree() {
		int count = 0;
		for(Transport T : IFT) {
			if(T.getState() == State.POOL) {
				count ++;
			}
		}
		return count;
	}
	
}
