package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignedAssignmentListEntity {
    @JsonProperty("Assignments")
    private List<AssignmentEntity> assignments;

    @JsonProperty("CurrentMonthTotalAmount")
    private double currentMonthTotalAmount;

    @JsonProperty("TotalAmount")
    private double totalAmount;

    @JsonIgnore
    public boolean containRejected() {
        for (AssignmentEntity assignment : getAssignments()) {
            if (assignment.isRejected()) {
                return true;
            }
        }

        return false;
    }

    public List<AssignmentEntity> getAssignments() {
        return assignments == null ? Collections.emptyList() : assignments;
    }

    public double getCurrentMonthTotalAmount() {
        return currentMonthTotalAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}
