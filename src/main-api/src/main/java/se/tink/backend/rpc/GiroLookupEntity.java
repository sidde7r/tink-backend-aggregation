package se.tink.backend.rpc;

import io.protostuff.Tag;
import java.net.URI;
import se.tink.backend.core.ImageUrls;

public class GiroLookupEntity {
    @Tag(1)
    private String displayName;
    @Tag(4)
    private String displayNumber;
    @Tag(2)
    private String identifier;
    @Tag(3)
    private ImageUrls images;

    public String getDisplayName() {
        return displayName;
    }

    public ImageUrls getImages() {
        return images;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setImages(ImageUrls images) {
        this.images = images;
    }

    public URI getIdentifier() {
        if (identifier == null) {
            return null;
        }
        return URI.create(identifier);
    }

    public void setIdentifier(URI identifier) {
        if (identifier == null) {
            this.identifier = null;
        } else {
            this.identifier = identifier.toString();
        }
    }

    public String getDisplayNumber() {
        return displayNumber;
    }

    public void setDisplayNumber(String displayNumber) {
        this.displayNumber = displayNumber;
    }
}
