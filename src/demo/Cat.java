package demo;

public class Cat {
	private String name;

	public void init(){
		System.out.println("Cat��init����");
	}
	
	public void destroy(){
		System.out.println("Cat��destroy����");
	}

	public void hello(){
		System.out.println("Hello,����"+name);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}
