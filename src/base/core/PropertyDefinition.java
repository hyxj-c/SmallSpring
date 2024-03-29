package base.core;

/**
 * property节点类：
 * 		存储配置文件中的Property的定义
 *
 */
public class PropertyDefinition {
	private String name;
	private String ref;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRef() {
		return ref;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
