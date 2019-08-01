package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialInstitutionIdentificationEntity {
    @JsonProperty("bicFi")
    private String bicFi = null;

    @JsonProperty("clearingSystemMemberId")
    private ClearingSystemMemberIdentificationEntity clearingSystemMemberId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("postalAddress")
    private PostalAddressEntity postalAddress = null;

    public String getBicFi() {
        return bicFi;
    }

    public void setBicFi(String bicFi) {
        this.bicFi = bicFi;
    }

    public ClearingSystemMemberIdentificationEntity getClearingSystemMemberId() {
        return clearingSystemMemberId;
    }

    public void setClearingSystemMemberId(
            ClearingSystemMemberIdentificationEntity clearingSystemMemberId) {
        this.clearingSystemMemberId = clearingSystemMemberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PostalAddressEntity getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(PostalAddressEntity postalAddress) {
        this.postalAddress = postalAddress;
    }
}
