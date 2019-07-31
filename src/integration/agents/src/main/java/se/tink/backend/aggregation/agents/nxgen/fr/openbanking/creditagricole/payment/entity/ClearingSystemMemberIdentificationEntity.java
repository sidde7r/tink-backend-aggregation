package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ClearingSystemMemberIdentificationEntity {
    @JsonProperty("clearingSystemId")
    private String clearingSystemId = null;

    @JsonProperty("memberId")
    private String memberId = null;

    public String getClearingSystemId() {
        return clearingSystemId;
    }

    public void setClearingSystemId(String clearingSystemId) {
        this.clearingSystemId = clearingSystemId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
