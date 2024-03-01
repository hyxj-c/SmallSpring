package demo;

import base.annotation.Component;
import base.annotation.Resource;

@Component(name = "userService")
public class UserService {
	@Resource(name="userDao")
	private UserDao userDao;
	
	public void login () {
		System.out.println("UserService: login()");
		userDao.login();
	}
}
