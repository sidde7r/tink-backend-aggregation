package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SignedAssignmentList {

    @JsonProperty("Assignments")
    private List<AssignmentEntity> assignments = Lists.newArrayList();

    public List<AssignmentEntity> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<AssignmentEntity> assignments) {
        if (assignments != null) {
            this.assignments = assignments;
        }
    }

    public boolean containRejected() {
        for (AssignmentEntity assignment : assignments) {
            if (assignment.isRejected()) {
                return true;
            }
        }

        return false;
    }
}
