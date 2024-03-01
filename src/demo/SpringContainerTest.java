package demo;

import org.junit.Test;

import base.core.SpringContainer;

public class SpringContainerTest {
	
	@Test
	public void test1(){
		// ����spring��������
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		// ��spring�����л�ȡ��Ҫ�Ķ���
		User user = (User) springContainer.getBean("user");
		// ִ�ж���ķ���
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
	 * ���Դ�������ȡ���Ƿ�Ϊͬһ������
	 */
	public void test3(){
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		Cat cat = springContainer.getBean("cat", Cat.class);
		Cat cat2 = springContainer.getBean("cat", Cat.class);
		System.out.println(cat==cat2);
	}
	
	/**
	 * ���Զ������������
	 */
	@Test
	public void test4(){
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		User user = springContainer.getBean("user", User.class);
		user.play();
		
		springContainer.close();
	}
	
	/**
	 * ����ע��ʵ�������ע����������ֵ
	 */
	@Test
	public void test5(){
		SpringContainer springContainer = new SpringContainer("conf/applicationContext.xml");
		UserService userService = springContainer.getBean("userService", UserService.class);
		userService.login();
	}
}
