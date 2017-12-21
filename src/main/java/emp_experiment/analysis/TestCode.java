package emp_experiment.analysis;




public class TestCode {
	
	public static void main(String[] args){
		System.out.println('a' < 'b');
	} 

}

class Util {
	private int count;

	public void count(int i) {
		if (i >= 10) {
			count++;
		}
	}
	
	public void reset() {
		count = 0;
	}
	
	public int currentCount(){
		return count;
	}
}
