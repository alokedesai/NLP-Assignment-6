
public class Pair {
	String english;
	String foreign;
	public Pair(String english, String foreign) {
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
	public String e() {
		return english;
	}
	
	public String f() {
		return foreign;
	}
	
	public String toString() {
		return english + ", "+ foreign;
	}
	
}
