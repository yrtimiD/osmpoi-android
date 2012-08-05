/**
 * 
 */
package il.yrtimid.osm.osmpoi;

/**
 * @author yrtimid
 *
 */
public class Pair<A, B> {
	private A a;
	private B b;
	
	public Pair(A a, B b){
		this.a = a;
		this.b = b;
	}
	
	/**
	 * @return the a
	 */
	public A getA() {
		return a;
	}
	
	/**
	 * @return the b
	 */
	public B getB() {
		return b;
	}
	
}
