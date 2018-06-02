package se.tink.backend.rpc;

import io.protostuff.Tag;
import se.tink.backend.core.ImageUrls;

public class ClearingLookupResponse {
    @Tag(1)
    private String bankDisplayName;
    @Tag(2)
    private ImageUrls images;

    public String getBankDisplayName() {
        return bankDisplayName;
    }

    public ImageUrls getImages() {
        return images;
    }

    public void setBankDisplayName(String bankDisplayName) {
        this.bankDisplayName = bankDisplayName;
    }

    public void setImages(ImageUrls images) {
        this.images = images;
    }
}
