package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UpcomingInvoiceEntity {
    private AmountEntity totalAmount;

    @JsonProperty("chargeDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    private List<ExpenseEntity> expenses;

    public AmountEntity getTotalAmount() {
        return totalAmount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public List<ExpenseEntity> getExpenses() {
        return expenses;
    }
}
