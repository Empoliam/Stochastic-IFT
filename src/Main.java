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

	//Parameters
	static final long SEED = 0L;
	static Random RNG = new Random(SEED);

	static final int N_FLAGELLA = 2 ; //Number of flagella
	static final boolean SEVER = true; //Do sever

	static final int MAX_MOTOR_POOL = 444; //150
	static final double MAX_TUBULIN_POOL = 84; //30

	//Time Parameters
	static final double tMax = 10000;
	static final double dt = 0.05;

	static List<Double> FLAGELLA = new LinkedList<Double>();
	static List<Transport> TRANSPORTS = new ArrayList<Transport>();	

	public static void main(String args[]) {


		//Variables //Default
		final double v = 2.5d; //2.5d
		final double D = 1.7d; //1.7d
		final double da = 3d/60d; //3d/60d
		final double db = 8.5e-4; //8.5e-4
		final double k = 0.03; //0.03
		final double a = 2.5e-4; //2.5e-4

		File fileOut = new File("./output/out.csv");
		fileOut.getParentFile().mkdir();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileOut));

			//Fill transport pool
			for(int x = 0; x < MAX_MOTOR_POOL; x++) {
				TRANSPORTS.add(new Transport());			
			}

			//Fill flagellum list
			for(int x = 0; x < N_FLAGELLA; x++) {
				FLAGELLA.add(0d);
			}

			//Initialise tubulin pool
			double tubulinPool = MAX_TUBULIN_POOL - usedTubulin();

			for(double t = 0; t < tMax; t += dt) {

				//Sever flagellum at 3000 seconds
				if(SEVER && Math.abs(t - 3000) < dt*0.1) {

					FLAGELLA.set(0, 0d);

					//Remove lost IFT particles
					Iterator<Transport> iterator = TRANSPORTS.iterator();
					while(iterator.hasNext()) {
						Transport T = iterator.next();
						if(T.getState() != State.POOL && T.getFlagellum() == 0) {
							iterator.remove();
						}
					}

				}

				//Flagella decay
				for(int i  = 0; i < N_FLAGELLA; i++) {

					double decay = (da+db*tipConcentration(i))*dt;
					FLAGELLA.set(i, Math.max(0d, FLAGELLA.get(i)-decay));
					tubulinPool += decay;

				}

				//Inject IFT particles
				double injProb = k * dt * countFree(); //Probability of injection
				for(int i  = 0; i < N_FLAGELLA; i++) {

					if(RNG.nextDouble() < injProb) {

						Transport freeTransport = getFreeTransport(); 
						freeTransport.setState(State.BALLISTIC);
						freeTransport.setFlagellum(i);

						double tubulin = a * tubulinPool; //Calculate carried tubulin
						tubulinPool -= tubulin; //Remove tubulin from free pool
						freeTransport.setTubulin(tubulin);

					}

				}

				for(Transport T : TRANSPORTS) {



					if(T.getState() == State.BALLISTIC) {

						//Advance Ballistic Transports
						T.setLocation(T.getLocation()+(v*dt));

						//Find IFT at tip
						if(T.getLocation() > FLAGELLA.get(T.getFlagellum())) {

							FLAGELLA.set(T.getFlagellum(),FLAGELLA.get(T.getFlagellum())+T.getTubulin()); //Grow flagellum
							T.setState(State.DIFFUSE); //Set IFT to diffuse
							T.setTubulin(0);
							T.setLocation(FLAGELLA.get(T.getFlagellum())); //Reset IFT location to tip

						}

					} else if (T.getState() == State.DIFFUSE) {

						//Move diffuse IFT
						double diffuseMovement = RNG.nextGaussian() * Math.sqrt(2*D*dt);
						T.setLocation(T.getLocation()+diffuseMovement);

						//Refelct IFT at tip
						if(T.getLocation() > FLAGELLA.get(T.getFlagellum())) {
							T.setLocation(2*FLAGELLA.get(T.getFlagellum())-T.getLocation());
						}

						//Absorb IFT at base
						if(T.getLocation() < 0) {

							T.setState(State.POOL);
							T.setLocation(0d);
							T.setFlagellum(-1);

						}

					}

				}

				//Regenerate tubulins
				if(tubulinPool < MAX_TUBULIN_POOL) {
					tubulinPool += ((MAX_TUBULIN_POOL)/30000d * dt);
				}

				//Regenerate IFT particles
				if(TRANSPORTS.size() < MAX_MOTOR_POOL) {
					double roll = RNG.nextDouble();
					if(roll < dt*(MAX_MOTOR_POOL/30000d)) {
						TRANSPORTS.add(new Transport());
					}
				}

				//Write out
				bw.append(Double.toString(t));
				for(Double F : FLAGELLA) {
					bw.append("," + F);
				}
				bw.append("\n");

			}

			//Print final state
			System.out.println(FLAGELLA);

			bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//Count number of free transports
	public static int countFree() {
		int count = 0;
		for(Transport T : TRANSPORTS) {
			if(T.getState() == State.POOL) {
				count ++;
			}
		}
		return count;
	}

	//Find a free transport
	public static Transport getFreeTransport() {

		Transport F = null;

		for(Transport T : TRANSPORTS) {
			if(T.getState() == State.POOL) {
				F = T;
				break;
			}
		}

		return F;

	}

	//Count tubulin used in flagella
	public static double usedTubulin() {

		double sum = 0d;
		for(Double F : FLAGELLA) {
			sum += F;
		}

		return sum;
	}

	//Count diffuse IFT particles 1um from tip
	public static double tipConcentration(int i) {

		int count = 0;
		for(Transport T : TRANSPORTS) {
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
