package org.ccls.nlp.cbt.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConfusionMatrix {

	private HashMap<Pair, Integer> cell;
	
	class Pair<T> {
		
		private T first;
		private T second;
		
		public Pair(T first, T second) {
			this.first = first;
			this.second = second;
		}
		
		public T getFirst() {
			return first;
		}
		public void setFirst(T first) {
			this.first = first;
		}
		public T getSecond() {
			return second;
		}
		public void setSecond(T second) {
			this.second = second;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((first == null) ? 0 : first.hashCode());
			result = prime * result
					+ ((second == null) ? 0 : second.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (first == null) {
				if (other.first != null)
					return false;
			} else if (!first.equals(other.first))
				return false;
			if (second == null) {
				if (other.second != null)
					return false;
			} else if (!second.equals(other.second))
				return false;
			return true;
		}

		private ConfusionMatrix getOuterType() {
			return ConfusionMatrix.this;
		}

		@Override
		public String toString() {
			return "Pair [first=" + first + ", second=" + second + "]";
		}
	}

	public ConfusionMatrix() {
		cell = new HashMap<Pair, Integer>();
	}
	
	public void addEntry(String actual, String prediction) {
		Pair p = new Pair(actual, prediction);
		if (!cell.containsKey(p)) {
			cell.put(p, 0);
		}
		cell.put(p, 1 + cell.get(p));
	}

	public HashMap<Pair, Integer> getCell() {
		return cell;
	}

	public void setCell(HashMap<Pair, Integer> cell) {
		this.cell = cell;
	}
	
	public void toConsole() {
		System.out.println();
		Set firstSet = getFirstSet();
		for (Object first : firstSet) {
			System.out.print(("\t") + first);
		}
		System.out.println();
		Set secondSet = getSecondSet();
		for (Object second : secondSet) {
			System.out.print(second);
			for (Object first : firstSet) {
				System.out.print(("\t") + cell.get(new Pair(first, second)));
			}
			System.out.println();
		}
		System.out.println();		
	}	
	
	// this produces a tsv file
	public void toFile(String filename) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(filename);
		Set firstSet = getFirstSet();
		for (Object first : firstSet) {
			pw.print(("\t") + first);
		}
		pw.println();
		Set secondSet = getSecondSet();
		for (Object second : secondSet) {
			pw.print(second);
			for (Object first : firstSet) {
				pw.print(("\t") + cell.get(new Pair(first, second)));
			}
			pw.println();
		}
		pw.close();
	}
	
	private Set getFirstSet() {
		Set toReturn = new LinkedHashSet();
		for (Pair p : cell.keySet()) {
			toReturn.add(p.getFirst());
		}
		return toReturn;
	}
	
	private Set getSecondSet() {
		Set toReturn = new LinkedHashSet();
		for (Pair p : cell.keySet()) {
			toReturn.add(p.getSecond());
		}
		return toReturn;	
	}

	@Override
	public String toString() {
		return "ConfusionMatrix [cell=" + cell + "]";
	}

	
	public static void main(String args[]) throws FileNotFoundException {
		ConfusionMatrix cm = new ConfusionMatrix();
		String taga = "a";
		String tagb = "b";
		cm.addEntry(taga, taga);
		cm.addEntry(tagb, tagb);
		cm.addEntry(taga, tagb);
		cm.addEntry(taga, tagb);
		cm.toFile("cmtest.txt");
	}
	
}