public class Transport {

	private State state = State.POOL;
	private double location = 0;
	private double tubulin = 0;
	private int flagellum;
	
	public Transport() {
	}

	public State getState() {
		return state;
	}
	
	public double getLocation() {
		return location;
	}
	
	public double getTubulin() {
		return tubulin;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
//	@Override
//	public String toString() {
//		return(state + " : " + Double.toString(location) + " : " + Double.toString(tubulin));
//	}
	
	public int getFlagellum() {
		return flagellum;
	}
	
	public void setFlagellum(int num) {
		flagellum = num;
	}
	
	public void setLocation(double location) {
		this.location = location;
	}

	public void setTubulin(double tubulin) {
		this.tubulin = tubulin;		
	}
	
}
