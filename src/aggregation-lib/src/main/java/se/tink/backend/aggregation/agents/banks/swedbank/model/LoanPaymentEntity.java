package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanPaymentEntity {

    private String description;
    private String totalAmount;
    private List<LoanAmountEntity> amounts;
    private String dueDay;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<LoanAmountEntity> getAmounts() {
        return amounts;
    }

    public void setAmounts(List<LoanAmountEntity> amounts) {
        this.amounts = amounts;
    }

    public String getDueDay() {
        return dueDay;
    }

    public void setDueDay(String dueDay) {
        this.dueDay = dueDay;
    }
}
