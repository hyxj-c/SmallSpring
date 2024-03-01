package demo;

public class User {
	private String name;
	private Cat cat;
	
	public void play(){
		System.out.println(name+"ÕıÔÚß£"+cat.getName());
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public void setCat(Cat cat) {
		this.cat = cat;
	}
	
}
