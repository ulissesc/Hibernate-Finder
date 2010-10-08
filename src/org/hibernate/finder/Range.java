package org.hibernate.finder;

/**
 * Utilizado para passar intervalos de buscas (FIRST e MAX)
 * @author ulisses
 *
 */
public class Range {

	private int first;
	private int max;

	public Range() {}
	
	public Range(int first, int max) {
		this.first = first;
		this.max = max;
	}

	public int getFirst() {
		return first;
	}
	public void setFirst(int first) {
		this.first = first;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
}
