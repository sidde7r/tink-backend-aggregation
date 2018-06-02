package se.tink.backend.core;

public class BadgeActivityContent {

	private String badgeImage;
	private char categoryIcon;
	private String title;
	private String description;
	private String formattedValue;
	private String image;
	
	public char getCategoryIcon() {
		return categoryIcon;
	}
	public void setCategoryIcon(char categoryIcon) {
		this.categoryIcon = categoryIcon;
	}
	public String getBadgeImage() {
		return badgeImage;
	}
	public void setBadgeImage(String badgeImage) {
		this.badgeImage = badgeImage;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getFormattedValue() {
		return formattedValue;
	}
	public void setFormattedValue(String formattedValue) {
		this.formattedValue = formattedValue;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setImage(String image) {
		this.image = image;
	}
	
	public String getImage()
	{
		return image;
	}
}
