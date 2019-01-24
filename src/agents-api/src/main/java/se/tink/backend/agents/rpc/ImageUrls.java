package se.tink.backend.agents.rpc;

public class ImageUrls {
    private String icon;
    private String banner;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public ImageUrls(String icon, String banner) {
        this.icon = icon;
        this.banner = banner;
    }
}
