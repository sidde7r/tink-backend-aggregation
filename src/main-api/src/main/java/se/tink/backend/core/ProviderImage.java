package se.tink.backend.core;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "providers_images")
public class ProviderImage {

    public enum Type {
        ICON, BANNER;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    @Id
    private String code;
    private String url;

    public ProviderImage() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
