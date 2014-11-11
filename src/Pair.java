
public class Pair {
	int english;
	int foreign;
	public Pair(int english, int foreign) {
		this.english = english;
		this.foreign = foreign;
	}
	
	@Override
	public int hashCode() {
		return (english + "|" + foreign).hashCode();
	}
	
	@Override
	public boolean equals(Object p) {
		return this.toString().equals(p.toString());
	}
	public int e() {
		return english;
	}
	
	public int f() {
		return foreign;
	}
	
	public String toString() {
		return english + ", "+ foreign;
	}
	
}
