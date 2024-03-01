package base.core;

import java.util.HashMap;
import java.util.Map;

/**
 * bean节点类：
 * 		存储spring配置文件中的bean定义
 *
 */
public class BeanDefinition {
	private String id;
	private String className;
	private String destroyMethod;
	private String initMethod;
	private Map<String, PropertyDefinition> properties = new HashMap<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getDestroyMethod() {
		return destroyMethod;
	}

	public void setDestroyMethod(String destroyMethod) {
		this.destroyMethod = destroyMethod;
	}

	public String getInitMethod() {
		return initMethod;
	}

	public void setInitMethod(String initMethod) {
		this.initMethod = initMethod;
	}

	public Map<String, PropertyDefinition> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, PropertyDefinition> properties) {
		this.properties = properties;
	}

}
