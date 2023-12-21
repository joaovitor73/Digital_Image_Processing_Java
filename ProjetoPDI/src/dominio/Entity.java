package dominio;

public class Entity {
	private double area;
	private double areaAmarelo;
	private String color;
	private String classification;
	private String image;
	
	public Entity(double area, double areaAmarelo, String color, String image, String classification) {
		this.area = area;
		this.areaAmarelo = areaAmarelo;
		this.color = color;
		this.setImage(image);
		this.setClassification(classification);
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}
	
	public double getAreaAmarelo() {
		return areaAmarelo;
	}

	public void setAreaAmarelo(double areaAmarelo) {
		this.areaAmarelo = areaAmarelo;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
	
}
