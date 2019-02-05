package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignBundleBodyEntity {
    @JsonProperty("SignedAssignmentList")
    private SignedAssignmentListEntity signedAssignmentList;

    public SignedAssignmentListEntity getSignedAssignmentList() {
        return signedAssignmentList;
    }
}
