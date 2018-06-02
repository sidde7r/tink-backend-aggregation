package se.tink.analytics.merchantmap.entities;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class CassandraMerchantMap implements Serializable {

    private static final long serialVersionUID = 4315945614107752688L;

    private String description;

    private UUID merchant;

    private String extendedInformation;

    public CassandraMerchantMap(String description, UUID merchant, String extendedInformation) {
        this.description = description;
        this.merchant = merchant;
        this.extendedInformation = extendedInformation;
    }

    public static Map<String, String> getColumnMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("description", "description");
        map.put("merchant", "merchant");
        map.put("extendedInformation", "extendedinformation");

        return map;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getMerchant() {
        return merchant;
    }

    public void setMerchant(UUID merchant) {
        this.merchant = merchant;
    }

    public String getExtendedInformation() {
        return extendedInformation;
    }

    public void setExtendedInformation(String extendedInformation) {
        this.extendedInformation = extendedInformation;
    }
}
