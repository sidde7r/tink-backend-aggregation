package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentResponse {
    @JsonProperty("UnsignedAssignmentList")
    private AssignmentListEntity unsignedAssignmentList;

    public AssignmentListEntity getUnsignedAssignmentList() {
        return unsignedAssignmentList;
    }

    public void setUnsignedAssignmentList(AssignmentListEntity unsignedAssignmentList) {
        this.unsignedAssignmentList = unsignedAssignmentList;
    }

}
