package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignBundleResponseBody {

    @JsonProperty("SignedAssignmentList")
    private SignedAssignmentList signedAssignmentList;

    public SignedAssignmentList getSignedAssignmentList() {
        return signedAssignmentList;
    }

    public void setSignedAssignmentList(SignedAssignmentList signedAssignmentList) {
        this.signedAssignmentList = signedAssignmentList;
    }
}
