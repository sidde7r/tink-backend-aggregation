package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerEntity {
    @JsonProperty("DataWarehouseCustomerId")
    private String dataWarehouseCustomerId;
    @JsonProperty("ABCustomerId")
    private String aBCustomerId;
    @JsonProperty("UserInstallationId")
    private String userInstallationId;
    @JsonProperty("IsAbove16")
    private boolean isAbove16;
    @JsonProperty("IsAbove18")
    private boolean isAbove18;
    @JsonProperty("IsStudent")
    private boolean isStudent;
    @JsonProperty("Kdk")
    private KdkEntity kdk;

    public String getDataWarehouseCustomerId() {
        return dataWarehouseCustomerId;
    }

    public String getaBCustomerId() {
        return aBCustomerId;
    }

    public String getUserInstallationId() {
        return userInstallationId;
    }

    public boolean isAbove16() {
        return isAbove16;
    }

    public boolean isAbove18() {
        return isAbove18;
    }

    public boolean isStudent() {
        return isStudent;
    }

    public KdkEntity getKdk() {
        return kdk;
    }

    boolean mustUpdateInformationAtBank() {
        return kdk != null && kdk.isMustUpdate();
    }
}
