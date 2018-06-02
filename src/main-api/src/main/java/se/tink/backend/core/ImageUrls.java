package se.tink.backend.core;

import io.protostuff.Tag;

public class ImageUrls {

    @Tag(1)
    private String icon;
    @Tag(2)
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
}
