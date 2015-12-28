package vidyamap.dao;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Node {
	
	@Id
	private int id;
	private int group;
	private int level;
	private int L1Group;
	private String name;
	private String type;
	private String text;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getGroup() {
		return group;
	}
	public void setGroup(int group) {
		this.group = group;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getL1Group() {
		return L1Group;
	}
	public void setL1Group(int l1Group) {
		L1Group = l1Group;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	
}
