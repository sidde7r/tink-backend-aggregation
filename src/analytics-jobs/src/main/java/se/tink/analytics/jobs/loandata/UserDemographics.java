package se.tink.analytics.jobs.loandata;

import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

public class UserDemographics implements Serializable {

    private static final long serialVersionUID = -1028184213887072561L;

    private UUID userId;
    private String postalCode;
    private String market;

    public static Map<String, String> getColumnMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("userId", "userid");
        map.put("postalCode", "postalcode");

        return map;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }
}