package se.tink.backend.aggregationcontroller.v1.rpc.entities;

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

    public se.tink.backend.aggregation.rpc.ImageUrls toAggregationImageUrls() {
        se.tink.backend.aggregation.rpc.ImageUrls imageUrls = new se.tink.backend.aggregation.rpc.ImageUrls();

        imageUrls.setBanner(this.banner);
        imageUrls.setIcon(this.icon);

        return imageUrls;
    }
}
