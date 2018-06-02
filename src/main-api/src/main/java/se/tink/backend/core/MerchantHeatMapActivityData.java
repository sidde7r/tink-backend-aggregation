package se.tink.backend.core;

public class MerchantHeatMapActivityData {

	private String buttonLabel;
	private String title;
	private String description;
	private String mapArrayData;
	private String mapFocusCoordinates;
	private char categoryIcon;
	
	public String getButtonLabel() {
		return buttonLabel;
	}
	public void setButtonLabel(String buttonLabel) {
		this.buttonLabel = buttonLabel;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMapArrayData() {
		return mapArrayData;
	}
	public void setMapArrayData(String mapArrayData) {
		this.mapArrayData = mapArrayData;
	}
	public String getMapFocusCoordinates() {
		return mapFocusCoordinates;
	}
	public void setMapFocusCoordinates(String mapFocusCoordinates) {
		this.mapFocusCoordinates = mapFocusCoordinates;
	}
	public char getCategoryIcon() {
		return categoryIcon;
	}
	public void setCategoryIcon(char categoryIcon) {
		this.categoryIcon = categoryIcon;
	}
}
