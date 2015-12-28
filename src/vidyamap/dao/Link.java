package vidyamap.dao;

import javax.persistence.Entity;

@Entity
public class Link {
	
	private int source;
	private int target;
	private int value;
	private int info;
	
	public int getSource() {
		return source;
	}
	public void setSource(int source) {
		this.source = source;
	}
	public int getTarget() {
		return target;
	}
	public void setTarget(int target) {
		this.target = target;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public int getInfo() {
		return info;
	}
	public void setInfo(int info) {
		this.info = info;
	}
}
