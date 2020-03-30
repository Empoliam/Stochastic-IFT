import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Main {	

	static final long SEED = 1L;
	static Random RNG = new Random(SEED);

	static final int NFLAG = 2;

	static final double v = 2d; //2d
	static final double D = 2d; //2d
	static final double da = 0.3/60d; //0.3/60d
	static final double db = 1.7e-3; //0.3/60
	static final double k = 1d/60d; //1d/60d
	static final double a = 2e-4; //2e-4

	//Time
	static final double tMax = 10000;
	static final double dt = 0.05;

	//Max motor pool
	static final int NBASE = 200; //150

	//Tubulin Pool
	static final double POOLMAX = 38*NFLAG; //30
	static double POOL = POOLMAX;

	static List<Double> FLAGELLA = new LinkedList<Double>();
	static List<Transport> IFT = new ArrayList<Transport>();	

	public static void main(String args[]) {

		File fileOut = new File("./output/out.csv");
		fileOut.getParentFile().mkdir();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));

			for(int x = 0; x < NBASE; x++) {
				IFT.add(new Transport());			
			}

			for(int x = 0; x < NFLAG; x++) {
				FLAGELLA.add(0d);
			}

			for(double t = 0; t < tMax; t += dt) {

				for(Transport T : IFT) {

					if(T.getState() == State.BALLISTIC) {

						T.setLocation(T.getLocation()+(v*dt));

						if(T.getLocation() > FLAGELLA.get(T.getFlagellum())) {

							FLAGELLA.set(T.getFlagellum(),FLAGELLA.get(T.getFlagellum())+T.getTubulin());
							T.setState(State.DIFFUSE);
							T.setLocation(FLAGELLA.get(T.getFlagellum()));

						}

					} else if (T.getState() == State.DIFFUSE) {

						double diffuseMovement = RNG.nextGaussian() * Math.sqrt(2*D*dt);

						T.setLocation(T.getLocation()+diffuseMovement);

						if(T.getLocation() > FLAGELLA.get(T.getFlagellum())) {
							T.setLocation(2*FLAGELLA.get(T.getFlagellum())-T.getLocation());
						}

						if(T.getLocation() < 0) {

							T.setState(State.POOL);

						}

					}

				}

				double injProb = k * dt * countFree();
				for(int i  = 0; i < NFLAG; i++) {

					double roll = RNG.nextDouble();

					if(roll < injProb) {

						Transport freeTransport = getFreeTransport(); 
						freeTransport.setState(State.BALLISTIC);
						freeTransport.setFlagellum(i);

						double tubulin = a * POOL;
						POOL -= tubulin;
						freeTransport.setTubulin(tubulin);

					}

					double decay = (da+db*tipConcentration(i))*dt;
					FLAGELLA.set(i, Math.max(0d, FLAGELLA.get(i)-decay));

				}

				//Regenerate proteins
				if(POOL + usedTubulin() < POOLMAX) {
					POOL += dt*(POOLMAX/300d);
				}
				if(IFT.size() < NBASE) {
					double roll = RNG.nextDouble();
					if(roll < dt*(NBASE/300d)) {
						IFT.add(new Transport());
					}
				}

				if(Math.abs(t - (tMax/2)) < dt*0.1) {

					FLAGELLA.set(0, 0d);
					Iterator<Transport> iterator = IFT.iterator();
					while(iterator.hasNext()) {
						Transport T = iterator.next();
						if(T.getState() != State.POOL && T.getFlagellum() == 0) {
							iterator.remove();
						}
					}
					
				}



				bw.append(Double.toString(t));
				for(Double F : FLAGELLA) {
					bw.append("," + F);
				}
				bw.append("\n");

			}

			System.out.println("ping");
			System.out.println(FLAGELLA);

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

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

	public static Transport getFreeTransport() {

		Transport F = null;

		for(Transport T : IFT) {
			if(T.getState() == State.POOL) {
				F = T;
				break;
			}
		}

		return F;

	}

	public static double usedTubulin() {

		double sum = 0d;
		for(Double F : FLAGELLA) {
			sum += F;
		}

		return sum;
	}
	
	public static double tipConcentration(int i) {
		
		int count = 0;
		for(Transport T : IFT) {
			if(T.getState() == State.DIFFUSE && 
					T.getFlagellum() == i && 
					FLAGELLA.get(i) - T.getLocation() <= 1
					) {
				count++;
			}
		}
		
		return (double) count;
	}

}
