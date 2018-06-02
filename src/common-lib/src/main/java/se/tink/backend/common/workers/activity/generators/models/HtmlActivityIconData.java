package se.tink.backend.common.workers.activity.generators.models;

public class HtmlActivityIconData {
    private String iconColor;
    private String iconSymbol;

    public HtmlActivityIconData(String iconSymbol, String iconColor) {
        setIconSymbol(iconSymbol);
        setIconColor(iconColor);
    }

    public String getIconColor() {
        return iconColor;
    }

    public String getIconSymbol() {
        return iconSymbol;
    }

    public void setIconColor(String iconColor) {
        this.iconColor = iconColor;
    }

    public void setIconSymbol(String iconSymbol) {
        this.iconSymbol = iconSymbol;
    }
}
