package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentListEntity {
    @JsonProperty("Assignments")
    private List<AssignmentEntity> assignments;

    public List<AssignmentEntity> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<AssignmentEntity> assignments) {
        this.assignments = assignments;
    }

}
