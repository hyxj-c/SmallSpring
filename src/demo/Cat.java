package demo;

public class Cat {
	private String name;

	public void init(){
		System.out.println("Cat的init方法");
	}
	
	public void destroy(){
		System.out.println("Cat的destroy方法");
	}

	public void hello(){
		System.out.println("Hello,我是"+name);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}
