package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginInfoEntity {
    @JsonProperty("BranchRegistrationNr")
    private String branchRegistrationNr;

    @JsonProperty("CanChangeAgreements")
    private boolean canChangeAgreements;

    @JsonProperty("CurrentAgreement")
    private CurrentAgreementEntity currentAgreement;

    @JsonProperty("CustomerSegment")
    private String customerSegment;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("NoAutoPay")
    private boolean noAutoPay;

    @JsonProperty("NoRealEstate")
    private boolean noRealEstate;

    @JsonProperty("NoTrading")
    private boolean noTrading;

    @JsonProperty("UserHash")
    private String userHash;

    public String getName() {
        return name;
    }
}
