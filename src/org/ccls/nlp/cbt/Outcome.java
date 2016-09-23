package org.ccls.nlp.cbt;


import org.cleartk.classifier.Instance;

public class Outcome {

	private Instance<String> result;
	private Integer id;

	public Outcome() {
		result = null;
		id = null;
	}
	
	public Outcome(Instance<String> result, Integer id) {
		this.result = result;
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Instance<String> getResult() {
		return result;
	}

	public void setResult(Instance<String> result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Outcome [result=" + result + ", id=" + id + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
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
		Outcome other = (Outcome) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		return true;
	}

}