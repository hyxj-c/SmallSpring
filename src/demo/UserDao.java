package demo;

import base.annotation.Component;

@Component(name = "userDao")
public class UserDao {
	public void login(){
		System.out.println("UserDao: login()");
	}
}
