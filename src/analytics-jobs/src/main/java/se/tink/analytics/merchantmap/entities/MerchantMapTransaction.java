package se.tink.analytics.merchantmap.entities;

import com.google.common.collect.ImmutableMap;
import java.util.Objects;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

/**
 * A optimized version of the CassandraTransaction class that only includes those fields that are needed
 * for merchant map calculations. Data extraction time is 2-3x faster compared to collecting all fields (tested on
 * local machine)
 */
public class MerchantMapTransaction implements Serializable {

    private static final long serialVersionUID = 679630202264333109L;

    private UUID userId;
    private UUID merchantId;
    private String originalDescription;
    private String categoryType;
    private Boolean userModifiedLocation;

    public Boolean isValid() {
        return merchantId != null && Objects.equals(categoryType, "EXPENSES") && userModifiedLocation != null
                && userModifiedLocation && originalDescription != null;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public static Map<String, String> getColumnMap() {

        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

        return builder.put("categoryType", "categorytype")
                .put("originalDescription", "originaldescription")
                .put("userId", "userid")
                .put("merchantId", "merchantid")
                .put("userModifiedLocation", "usermodifiedlocation")
                .build();
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
    }

    public void setOriginalDescription(String originalDescription) {
        this.originalDescription = originalDescription;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public boolean getUserModifiedLocation() {
        return userModifiedLocation;
    }

    public void setUserModifiedLocation(Boolean userModifiedLocation) {
        this.userModifiedLocation = userModifiedLocation;
    }
}
