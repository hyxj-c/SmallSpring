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
 * Spring������
 * 		���𴴽�Bean���󣬲�����Bean����
 */
public class SpringContainer {
	private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>(); // ��Ŷ���������bean�ڵ����Ķ�Ӧ��ϵ
	private Map<String, Object> singletonBeanMap = new HashMap<>(); // ��Ŷ���������bean����Ķ�Ӧ��ϵ
	
	public SpringContainer(String fileName){
		// ����xml
		readXML(fileName);
		// ʵ����bean����
		instanceObject();
		// ��bean���Ը�ֵ
		injectPropertyValue();
		injectObjectByAnnotation();
	}
	
	/**
	 * ʵ����Bean����
	 */
	private void instanceObject(){
		// ����beanDefinitionMap�����е�value��ʵ����bean���󣬲���ʵ������bean������뵽singletonBeanMap��
		Collection<BeanDefinition> beanDefinitions = beanDefinitionMap.values(); // ��ȡ�����е�����bean�ڵ����
		for (BeanDefinition beanDefinition : beanDefinitions) {
			// ��ȡbean�ڵ��ж��������
			String id = beanDefinition.getId();
			// ��ȡbean�ڵ���Ҫʵ��������
			String className = beanDefinition.getClassName();
			
			try {
				// ͨ������ʵ����bean����
				Object bean = Class.forName(className).newInstance();
				// �Ѹö�����뵽singletonBeanMap��
				singletonBeanMap.put(id, bean);
				
				// �ж�bean�ڵ����Ƿ�������init-method���ԣ��������ˣ���ִ��bean����ĳ�ʼ������
				if (beanDefinition.getInitMethod() != null) {
					Method initMethod = getBean(id).getClass().getMethod(beanDefinition.getInitMethod());
					initMethod.invoke(getBean(id));
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Cannot find class ["+ className +"] for bean with name "
				+ beanDefinition.getId() +"������<bean>�ڵ���class����ֵ�е�����");
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Couldn't find an init method named '"+beanDefinition.getInitMethod()
				+"' on bean with name '"+id+"'������init-method�е�����ֵ");
			} catch (Exception e) {
				e.printStackTrace();
			} 			
		}
	}	
	
	/**
	 * ��Bean����ע������ֵ
	 */
	private void injectPropertyValue(){
		// ��ȡ�����е�����bean�ڵ����
		Collection<BeanDefinition> beanDefinitions = beanDefinitionMap.values();
		for (BeanDefinition beanDefinition : beanDefinitions) {
			// ��ȡbean�ڵ��ж����id
			String id = beanDefinition.getId();
			// ���ݶ���id��ȡʵ������bean����
			Object object = singletonBeanMap.get(id);			
			// ��ȡ��bean������
			Class<?> clazz = object.getClass();
			
			// ��ȡbean�ڵ��д��Property�ڵ��map
			Map<String, PropertyDefinition> propertyDefinitionMap = beanDefinition.getProperties();
			// ��ȡ���е�property�ڵ�
			Collection<PropertyDefinition> properties = propertyDefinitionMap.values();
			
			for (PropertyDefinition property : properties) {
				// ��ȡproperty�ڵ���Ҫע��ķ�����д��
				String name = property.getName();
				// ��ȡҪ������ע���ֵ
				String value = property.getValue();
				// ��ȡҪ������ע��ֵ�����ö���id
				String ref = property.getRef();
				// ƴ��Ҫ���ø�ֵ��set����  ����userDao -> set + U + serDao
				String setterName="set"+name.substring(0, 1).toUpperCase()+name.substring(1);
								
				// ��ø�bean�������й����ķ���
				Method[] methods = clazz.getDeclaredMethods();
				for (Method method : methods) {
					// �жϸ�bean�����Ƿ���Ҫ��ֵ�ķ���
					if (setterName.equals(method.getName())) {
						// ���и�ֵ
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
	 * ͨ��ע��ķ�ʽ��bean���� ����������������� ע��ֵ
	 */
	private void injectObjectByAnnotation(){
		// �������������е�bean����
		for (Object bean : singletonBeanMap.values()){
			// ��ȡbean�������е������ֶ�
			Field[] fields = bean.getClass().getDeclaredFields();
			if (fields == null) {
				continue;
			}
			
			for (Field field : fields) {
				// ��ȡ�ֶ��ϵ�@Resourceע��
				Resource resourceAnnotation = field.getAnnotation(Resource.class);
				if (resourceAnnotation != null ) {
					// ���ע���ϵ�ֵ
					String name = resourceAnnotation.name();
					// ����˽�����Կɲ���
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					
					try {
						// ����ע���ϵ�ֵע���Ӧ��bean����
						Object obj = getBean(name);
						if (obj == null) {
							throw new RuntimeException("'"+name+"' is not find������Ҫע��Ķ�����");
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
	 * ���ݶ������ƻ�ȡ����ʵ��
	 * @param name ��������
	 * @return name��ȷ���ظö��󣬴��󷵻�null
	 */
	public Object getBean(String name){
		return singletonBeanMap.get(name);
	}
	
	/**
	 * ���ݶ������ƺ�ָ�������ͻ�ȡָ�����͵Ķ���ʵ��
	 * @param name ��������
	 * @param c ָ��������
	 * @return name��ȷ���ظö��󣬴��󷵻�null
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(String name, Class<T> c){
		return (T) singletonBeanMap.get(name);
	}
	
	/**
	 * �ر�����
	 */
	public void close(){
		// �������������е�bean id
		for (String id : singletonBeanMap.keySet()) {
			// 1.�ж�bean�ڵ��Ƿ�������destroy-method����			
			// ����id�õ�bean�ڵ����
			BeanDefinition beanDefinition = beanDefinitionMap.get(id);
			// ��ȡbean�ڵ�����е�destroy-method�����е�ֵ
			String destroyMethodName = null;
			if (beanDefinition != null) {
				destroyMethodName = beanDefinition.getDestroyMethod();
			}
			
			if (destroyMethodName != null) {
				// 2.ִ��bean�ڵ���ָ����bean�����destroy����				
				try {
					// ���bean�����destroy����
					Method destroyMethod = getBean(id).getClass().getMethod(destroyMethodName);
					// ͨ������ִ�и÷���
					destroyMethod.invoke(getBean(id));
					
				} catch (NoSuchMethodException e) {
					throw new RuntimeException("Couldn't find an destroy method named '"+destroyMethodName
					+"' on bean with name '"+id+"'������destroy-method�е�����ֵ");
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
		
		// 3.��������ɾ��bean�����bean�ڵ����
		singletonBeanMap.clear();
		beanDefinitionMap.clear();
		singletonBeanMap = null;
		beanDefinitionMap = null;
	}	
	
	/**
	 * ����spring�������ļ������ѽ�����<bean>�ڵ�Ķ����ŵ�Map��
	 * @param fileName �����ļ�����
	 */
	@SuppressWarnings("unchecked")
	private void readXML(String fileName){
		// 1.����dom4j����xml�������ļ�
		SAXReader sax = new SAXReader();
		URL xmlPath = getClass().getClassLoader().getResource(fileName);
		try {
			// ��ȡ�����ļ�
			Document doc = sax.read(xmlPath);
			// ��ȡ��Ԫ��
			Element root = doc.getRootElement();
			
			// ����<context>�ڵ�
			parseXMLOfContext(root);
			
			// ��ȡ��Ԫ����������Ϊ<bean>���ӽڵ�
			List<Element> beanElements = root.elements("bean");
			// 2.����<bean>�ڵ㣬��ȡ����ֵ�������뵽BeanDefinition��
			for (Element bean : beanElements) {
				// ��ȡid��
				String id = bean.attributeValue("id");
				// �ж��������Ƿ��Ѵ��ڴ�id�����������׳��쳣
				boolean containsKey = beanDefinitionMap.containsKey(id);
				if (containsKey) {
					throw new RuntimeException("Bean name '"+id+"' is already used in this <beans> element");
				}
				// ��ȡ����
				String className = bean.attributeValue("class");
				// ��ȡdestroy-method������
				String destroyMethod = bean.attributeValue("destroy-method");
				// ��ȡinit-method������
				String initMethod = bean.attributeValue("init-method");
				
				// ���뵽BeanDefinition����
				BeanDefinition beanDefinition = new BeanDefinition();
				beanDefinition.setId(id);
				beanDefinition.setClassName(className);
				beanDefinition.setDestroyMethod(destroyMethod);
				beanDefinition.setInitMethod(initMethod);
				
				// ��ȡ<bean>�ڵ��µ�<property>�ӽڵ�
				List<Element> propElements = bean.elements("property");
				
				// 3.����<property>�ڵ㣬��ȡ����ֵ�������뵽PropertyDefinition��
				for (Element prop : propElements) {
					// ��ȡname����ֵ�д�ķ�������д
					String propName = prop.attributeValue("name");
					// ��ȡvalue����ֵ�е�ֵ
					String propValue = prop.attributeValue("value");
					// ��ȡref����ֵ�д�����õĶ���id
					String propRef = prop.attributeValue("ref");
					// ���뵽PropertyDefinition��
					PropertyDefinition propertyDefinition = new PropertyDefinition();
					propertyDefinition.setName(propName);
					propertyDefinition.setValue(propValue);
					propertyDefinition.setRef(propRef);
					
					beanDefinition.getProperties().put(propName, propertyDefinition);
				}
				
				// ��bean�ڵ��ŵ�beanDefinitionMap��
				beanDefinitionMap.put(id, beanDefinition);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ����XML�е�<context>�ڵ�
	 * @param root
	 */
	@SuppressWarnings("unchecked")
	private void parseXMLOfContext(Element root) {
		// 1.��ȡ��·�������е�<context>�ӽڵ㣬����ȡҪɨ��İ���
		List<Element> contextElements = root.elements("context");
		for (Element element : contextElements) {
			// ��ȡ����ֵbase-package�д�İ���
			String packageName = element.attributeValue("base-package");
			
			// 2.��ȡ���е���
			// ��ȡҪɨ�����url
			URL url = getClass().getClassLoader().getResource(packageName);
			File file = null;
			try {
				// ���ݰ����ڵ�·������File
				file = new File(url.toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if (!file.exists() || file.isFile()) {
				throw new RuntimeException("'"+packageName+"' path error������Ҫɨ��İ�");
			}
			
			// ��ȡ��·�������е��ļ���Ŀ¼
			File[] listFiles = file.listFiles();
			for (File f : listFiles) {
				// ��ȡ�ļ���
				String fileName = f.getName();
				// �ҵ�.class�ļ�
				if (f.isFile() && fileName.endsWith(".class")) {
					// ��ȡ������ƴ���ϰ���
					String className = packageName+"."+fileName.substring(0, fileName.lastIndexOf(".class"));
					
					try {
						// 3.ʵ��������@Componentע�����
						// ��ø������
						Class<?> clazz = Class.forName(className);
						// �жϸ����Ƿ�ʹ����@Componentע��
						Component annotation = clazz.getAnnotation(Component.class);
						if (annotation != null) {
							// ��ȡע���ϵ�ֵ
							String name = annotation.name();
							// ʵ�����ö��󣬲����뵽singletonBeanMap��
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
