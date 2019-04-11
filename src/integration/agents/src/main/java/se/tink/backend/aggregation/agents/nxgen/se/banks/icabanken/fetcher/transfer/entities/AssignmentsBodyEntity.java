package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AssignmentsBodyEntity {
    @JsonProperty("Assignments")
    private List<AssignmentEntity> assignments;

    @JsonProperty("TotalAmount")
    private double totalAmount;

    @JsonProperty("CurrentMonthTotalAmount")
    private double currentMonthTotalAmount;

    public List<AssignmentEntity> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<AssignmentEntity> assignments) {
        this.assignments = assignments;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getCurrentMonthTotalAmount() {
        return currentMonthTotalAmount;
    }

    public void setCurrentMonthTotalAmount(double currentMonthTotalAmount) {
        this.currentMonthTotalAmount = currentMonthTotalAmount;
    }
}
