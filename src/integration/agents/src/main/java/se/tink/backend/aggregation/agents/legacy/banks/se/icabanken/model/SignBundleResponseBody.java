package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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
