package vidyamap.core;

import java.util.HashMap;

public class NodeMap <K, V> extends HashMap<K, V>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1897793782264691154L;
	
	@Override
	public boolean equals(Object o) {
		return (o instanceof NodeMap) && (((NodeMap) o).get("id").toString().trim()).equals(this.get("id").toString().trim());
	}
	
	@Override
	public int hashCode() {
		return Integer.parseInt(this.get("id").toString().trim());
	}
}
