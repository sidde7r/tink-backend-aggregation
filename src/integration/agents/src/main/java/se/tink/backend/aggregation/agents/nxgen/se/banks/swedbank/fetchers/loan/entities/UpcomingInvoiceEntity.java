package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class UpcomingInvoiceEntity {
    private AmountEntity totalAmount;
    private List<ExpenseEntity> expenses;

    @JsonProperty("chargeDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;
}
