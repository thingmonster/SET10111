package edu.pccomponents;

public class Component {

	private String name;
	private Float price;
	
	public Component(String n, Float p) {
		name = n;
		price = p;
	}

	public String getName() {
		return name;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float p) {
		price = p;
	}
}
