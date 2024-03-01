package demo;

import org.junit.Test;

import base.core.SpringContainer;

public class SpringContainerTest {
	
	@Test
	public void test1(){
		// 创建spring容器对象
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		// 从spring容器中获取需要的对象
		User user = (User) springContainer.getBean("user");
		// 执行对象的方法
		user.play();
	}
	
	@Test
	public void test2(){
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		User user = springContainer.getBean("user", User.class);
		user.play();
	}
	
	@Test
	/**
	 * 测试从容器中取的是否为同一个对象
	 */
	public void test3(){
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		Cat cat = springContainer.getBean("cat", Cat.class);
		Cat cat2 = springContainer.getBean("cat", Cat.class);
		System.out.println(cat==cat2);
	}
	
	/**
	 * 测试对象的生命周期
	 */
	@Test
	public void test4(){
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		User user = springContainer.getBean("user", User.class);
		user.play();
		
		springContainer.close();
	}
	
	/**
	 * 测试注解实例化类和注入引用属性值
	 */
	@Test
	public void test5(){
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		UserService userService = springContainer.getBean("userService", UserService.class);
		userService.login();
	}
}
