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

	static final long SEED = 0L;
	static Random RNG = new Random(SEED);

	static final int NFLAG = 2 ;
	static final boolean SEVER = true;

	static final int MAX_MOTOR_POOL = 444; //150
	static final double MAX_TUBULIN_POOL = 84; //30

	//Time
	static final double tMax = 10000;
	static final double dt = 0.05;

	static List<Double> FLAGELLA = new LinkedList<Double>();
	static List<Transport> IFT = new ArrayList<Transport>();	

	public static void main(String args[]) {

		final double v = 2.5d; //2d
		final double D = 1.7d; //2d
		final double da = 3d/60d; //0.3d/60d
		final double db = 8.5e-4; //1.7e-4
		final double k = 0.03; //1d/60d
		final double a = 2.5e-4; //2e-4

		File fileOut = new File("./output/out.csv");
		fileOut.getParentFile().mkdir();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));

			for(int x = 0; x < MAX_MOTOR_POOL; x++) {
				IFT.add(new Transport());			
			}

			for(int x = 0; x < NFLAG; x++) {
				FLAGELLA.add(0d);
			}

			double POOL = MAX_TUBULIN_POOL - usedTubulin();

			for(double t = 0; t < tMax; t += dt) {

				if(SEVER && Math.abs(t - 3000) < dt*0.1) {

					FLAGELLA.set(0, 0d);
					Iterator<Transport> iterator = IFT.iterator();
					while(iterator.hasNext()) {
						Transport T = iterator.next();
						if(T.getState() != State.POOL && T.getFlagellum() == 0) {
							iterator.remove();
						}
					}

				}

				for(int i  = 0; i < NFLAG; i++) {
					
					double decay = (da+db*tipConcentration(i))*dt;
					FLAGELLA.set(i, Math.max(0d, FLAGELLA.get(i)-decay));
					POOL += decay;
					
				}
				
				double injProb = k * dt * countFree();
				for(int i  = 0; i < NFLAG; i++) {
					
					if(RNG.nextDouble() < injProb) {

						Transport freeTransport = getFreeTransport(); 
						freeTransport.setState(State.BALLISTIC);
						freeTransport.setFlagellum(i);

						double tubulin = a * POOL;
						POOL -= tubulin;
						freeTransport.setTubulin(tubulin);

					}

				}
				
				for(Transport T : IFT) {

					if(T.getState() == State.BALLISTIC) {

						T.setLocation(T.getLocation()+(v*dt));

					} else if (T.getState() == State.DIFFUSE) {

						double diffuseMovement = RNG.nextGaussian() * Math.sqrt(2*D*dt);
						T.setLocation(T.getLocation()+diffuseMovement);

					}

				}

				for(Transport T : IFT) {

					if(T.getState() == State.BALLISTIC) {

						if(T.getLocation() > FLAGELLA.get(T.getFlagellum())) {

							FLAGELLA.set(T.getFlagellum(),FLAGELLA.get(T.getFlagellum())+T.getTubulin());
							T.setState(State.DIFFUSE);
							T.setTubulin(0);
							T.setLocation(FLAGELLA.get(T.getFlagellum()));

						}

					} else if (T.getState() == State.DIFFUSE) {

						if(T.getLocation() > FLAGELLA.get(T.getFlagellum())) {
							T.setLocation(2*FLAGELLA.get(T.getFlagellum())-T.getLocation());
						}

						if(T.getLocation() < 0) {

							T.setState(State.POOL);
							T.setLocation(0d);
							T.setFlagellum(-1);

						}

					}

				}
				

				//Regenerate proteins
				if(POOL < MAX_TUBULIN_POOL) {
					POOL += ((MAX_TUBULIN_POOL)/30000d * dt);
				}

				if(IFT.size() < MAX_MOTOR_POOL) {
					double roll = RNG.nextDouble();
					if(roll < dt*(MAX_MOTOR_POOL/30000d)) {
						IFT.add(new Transport());
					}
				}


				bw.append(Double.toString(t));
				for(Double F : FLAGELLA) {
					bw.append("," + F);
				}
				bw.append("," +  POOL + "\n");

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
					T.getLocation() > FLAGELLA.get(i) - 1
					) {
				count++;
			}
		}

		return (double) count;
	}

}
