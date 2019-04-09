package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentsResponseBody {
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
