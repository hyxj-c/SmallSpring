package base.core;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import base.annotation.Component;
import base.annotation.Resource;

/**
 * Spring容器：
 * 		负责创建Bean对象，并管理Bean对象
 */
public class SpringContainer {
	private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>(); // 存放对象名称与bean节点对象的对应关系
	private Map<String, Object> singletonBeanMap = new HashMap<>(); // 存放对象名称与bean对象的对应关系
	
	public SpringContainer(String fileName){
		// 解析xml
		readXML(fileName);
		// 实例化bean对象
		instanceObject();
		// 给bean属性赋值
		injectPropertyValue();
		injectObjectByAnnotation();
	}
	
	/**
	 * 实例化Bean对象
	 */
	private void instanceObject(){
		// 遍历beanDefinitionMap集合中的value，实例化bean对象，并把实例化的bean对象存入到singletonBeanMap中
		Collection<BeanDefinition> beanDefinitions = beanDefinitionMap.values(); // 获取集合中的所有bean节点对象
		for (BeanDefinition beanDefinition : beanDefinitions) {
			// 获取bean节点中对象的名称
			String id = beanDefinition.getId();
			// 获取bean节点中要实例化的类
			String className = beanDefinition.getClassName();
			
			try {
				// 通过反射实例化bean对象
				Object bean = Class.forName(className).newInstance();
				// 把该对象存入到singletonBeanMap中
				singletonBeanMap.put(id, bean);
				
				// 判断bean节点中是否设置了init-method属性，若设置了，则执行bean对象的初始化方法
				if (beanDefinition.getInitMethod() != null) {
					Method initMethod = getBean(id).getClass().getMethod(beanDefinition.getInitMethod());
					initMethod.invoke(getBean(id));
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Cannot find class ["+ className +"] for bean with name "
				+ beanDefinition.getId() +"，请检查<bean>节点中class属性值中的类名");
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Couldn't find an init method named '"+beanDefinition.getInitMethod()
				+"' on bean with name '"+id+"'，请检查init-method中的属性值");
			} catch (Exception e) {
				e.printStackTrace();
			} 			
		}
	}	
	
	/**
	 * 给Bean对象注入属性值
	 */
	private void injectPropertyValue(){
		// 获取集合中的所有bean节点对象
		Collection<BeanDefinition> beanDefinitions = beanDefinitionMap.values();
		for (BeanDefinition beanDefinition : beanDefinitions) {
			// 获取bean节点中对象的id
			String id = beanDefinition.getId();
			// 根据对象id获取实例化的bean对象
			Object object = singletonBeanMap.get(id);			
			// 获取该bean对象类
			Class<?> clazz = object.getClass();
			
			// 获取bean节点中存放Property节点的map
			Map<String, PropertyDefinition> propertyDefinitionMap = beanDefinition.getProperties();
			// 获取所有的property节点
			Collection<PropertyDefinition> properties = propertyDefinitionMap.values();
			
			for (PropertyDefinition property : properties) {
				// 获取property节点中要注入的方法缩写的
				String name = property.getName();
				// 获取要给方法注入的值
				String value = property.getValue();
				// 获取要给方法注入值的引用对象id
				String ref = property.getRef();
				// 拼接要调用赋值的set方法  比如userDao -> set + U + serDao
				String setterName="set"+name.substring(0, 1).toUpperCase()+name.substring(1);
								
				// 获得该bean类中所有公开的方法
				Method[] methods = clazz.getDeclaredMethods();
				for (Method method : methods) {
					// 判断该bean类中是否有要赋值的方法
					if (setterName.equals(method.getName())) {
						// 进行赋值
						try {
							if (value != null) {
								method.invoke(object, value);
							}
							if (ref != null) {
								method.invoke(object, singletonBeanMap.get(ref));
							}
						} catch (Exception e) {
							e.printStackTrace();
						} 
					}
				}
			}
		}
	}
	
	/**
	 * 通过注解的方式给bean对象 引用其它对象的属性 注入值
	 */
	private void injectObjectByAnnotation(){
		// 遍历容器中所有的bean对象
		for (Object bean : singletonBeanMap.values()){
			// 获取bean对象类中的所有字段
			Field[] fields = bean.getClass().getDeclaredFields();
			if (fields == null) {
				continue;
			}
			
			for (Field field : fields) {
				// 获取字段上的@Resource注解
				Resource resourceAnnotation = field.getAnnotation(Resource.class);
				if (resourceAnnotation != null ) {
					// 获得注解上的值
					String name = resourceAnnotation.name();
					// 设置私有属性可操作
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					
					try {
						// 根据注解上的值注入对应的bean对象
						Object obj = getBean(name);
						if (obj == null) {
							throw new RuntimeException("'"+name+"' is not find，请检查要注入的对象名");
						}
						field.set(bean, obj);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}
		}
	}
	
	/**
	 * 根据对象名称获取对象实例
	 * @param name 对象名称
	 * @return name正确返回该对象，错误返回null
	 */
	public Object getBean(String name){
		return singletonBeanMap.get(name);
	}
	
	/**
	 * 根据对象名称和指定的类型获取指定类型的对象实例
	 * @param name 对象名称
	 * @param c 指定的类型
	 * @return name正确返回该对象，错误返回null
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(String name, Class<T> c){
		return (T) singletonBeanMap.get(name);
	}
	
	/**
	 * 关闭容器
	 */
	public void close(){
		// 遍历容器中所有的bean id
		for (String id : singletonBeanMap.keySet()) {
			// 1.判断bean节点是否设置了destroy-method属性			
			// 根据id得到bean节点对象
			BeanDefinition beanDefinition = beanDefinitionMap.get(id);
			// 获取bean节点对象中的destroy-method属性中的值
			String destroyMethodName = null;
			if (beanDefinition != null) {
				destroyMethodName = beanDefinition.getDestroyMethod();
			}
			
			if (destroyMethodName != null) {
				// 2.执行bean节点中指定的bean对象的destroy方法				
				try {
					// 获得bean对象的destroy方法
					Method destroyMethod = getBean(id).getClass().getMethod(destroyMethodName);
					// 通过反射执行该方法
					destroyMethod.invoke(getBean(id));
					
				} catch (NoSuchMethodException e) {
					throw new RuntimeException("Couldn't find an destroy method named '"+destroyMethodName
					+"' on bean with name '"+id+"'，请检查destroy-method中的属性值");
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
		
		// 3.从容器中删除bean对象和bean节点对象
		singletonBeanMap.clear();
		beanDefinitionMap.clear();
		singletonBeanMap = null;
		beanDefinitionMap = null;
	}	
	
	/**
	 * 解析spring的配置文件，并把解析的<bean>节点的定义存放到Map中
	 * @param fileName 配置文件名称
	 */
	@SuppressWarnings("unchecked")
	private void readXML(String fileName){
		// 1.利用dom4j解析xml的配置文件
		SAXReader sax = new SAXReader();
		URL xmlPath = getClass().getClassLoader().getResource(fileName);
		try {
			// 读取配置文件
			Document doc = sax.read(xmlPath);
			// 获取根元素
			Element root = doc.getRootElement();
			
			// 解析<context>节点
			parseXMLOfContext(root);
			
			// 获取根元素下所有名为<bean>的子节点
			List<Element> beanElements = root.elements("bean");
			// 2.遍历<bean>节点，获取属性值，并存入到BeanDefinition中
			for (Element bean : beanElements) {
				// 获取id名
				String id = bean.attributeValue("id");
				// 判断容器中是否已存在此id名，存在则抛出异常
				boolean containsKey = beanDefinitionMap.containsKey(id);
				if (containsKey) {
					throw new RuntimeException("Bean name '"+id+"' is already used in this <beans> element");
				}
				// 获取类名
				String className = bean.attributeValue("class");
				// 获取destroy-method方法名
				String destroyMethod = bean.attributeValue("destroy-method");
				// 获取init-method方法名
				String initMethod = bean.attributeValue("init-method");
				
				// 存入到BeanDefinition类中
				BeanDefinition beanDefinition = new BeanDefinition();
				beanDefinition.setId(id);
				beanDefinition.setClassName(className);
				beanDefinition.setDestroyMethod(destroyMethod);
				beanDefinition.setInitMethod(initMethod);
				
				// 获取<bean>节点下的<property>子节点
				List<Element> propElements = bean.elements("property");
				
				// 3.遍历<property>节点，获取属性值，并存入到PropertyDefinition中
				for (Element prop : propElements) {
					// 获取name属性值中存的方法名缩写
					String propName = prop.attributeValue("name");
					// 获取value属性值中的值
					String propValue = prop.attributeValue("value");
					// 获取ref属性值中存的引用的对象id
					String propRef = prop.attributeValue("ref");
					// 存入到PropertyDefinition中
					PropertyDefinition propertyDefinition = new PropertyDefinition();
					propertyDefinition.setName(propName);
					propertyDefinition.setValue(propValue);
					propertyDefinition.setRef(propRef);
					
					beanDefinition.getProperties().put(propName, propertyDefinition);
				}
				
				// 把bean节点存放到beanDefinitionMap中
				beanDefinitionMap.put(id, beanDefinition);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 解析XML中的<context>节点
	 * @param root
	 */
	@SuppressWarnings("unchecked")
	private void parseXMLOfContext(Element root) {
		// 1.获取根路径下所有的<context>子节点，并读取要扫描的包名
		List<Element> contextElements = root.elements("context");
		for (Element element : contextElements) {
			// 获取属性值base-package中存的包名
			String packageName = element.attributeValue("base-package");
			
			// 2.读取包中的类
			// 获取要扫描包的url
			URL url = getClass().getClassLoader().getResource(packageName);
			File file = null;
			try {
				// 根据包所在的路径创建File
				file = new File(url.toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if (!file.exists() || file.isFile()) {
				throw new RuntimeException("'"+packageName+"' path error，请检查要扫描的包");
			}
			
			// 获取该路径下所有的文件和目录
			File[] listFiles = file.listFiles();
			for (File f : listFiles) {
				// 获取文件名
				String fileName = f.getName();
				// 找到.class文件
				if (f.isFile() && fileName.endsWith(".class")) {
					// 截取类名并拼接上包名
					String className = packageName+"."+fileName.substring(0, fileName.lastIndexOf(".class"));
					
					try {
						// 3.实例化带有@Component注解的类
						// 获得该类对象
						Class<?> clazz = Class.forName(className);
						// 判断该类是否使用了@Component注解
						Component annotation = clazz.getAnnotation(Component.class);
						if (annotation != null) {
							// 获取注解上的值
							String name = annotation.name();
							// 实例化该对象，并存入到singletonBeanMap中
							singletonBeanMap.put(name, clazz.newInstance());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
}
