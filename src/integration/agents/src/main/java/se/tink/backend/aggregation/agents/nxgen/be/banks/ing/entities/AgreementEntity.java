package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class AgreementEntity {

    private String id;
    private String type;
    private String subType;
    private String group;
    private String role;
    private CommercialIdEntity commercialId;
    private String productName;
    private String holderName;
    private boolean visible;
    private String status;
    private AmountEntity balance;
    private AmountEntity availableBalance;

    @JsonProperty("_links")
    private List<LinkEntity> links;

    public String toString() {
        String commercialValue = Hash.sha256AsHex(this.getCommercialId().getValue().getBytes());
        return "AgreementEntity(commercialIdValue="
                + commercialValue
                + ", type="
                + this.getType()
                + ", subType="
                + this.getSubType()
                + ", group="
                + this.getGroup()
                + ", role="
                + this.getRole()
                + ", productName="
                + this.getProductName()
                + ")";
    }
}
