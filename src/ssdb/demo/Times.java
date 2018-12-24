package ssdb.demo;

public class Times {
	public long calTime;
	public long insertTime;
	public long adjustTime;
	//public boolean isAdjusted;
	public Times(long x,long a,long b){
		this.calTime=x;
		this.insertTime=a;
		this.adjustTime=b;
	}
}
