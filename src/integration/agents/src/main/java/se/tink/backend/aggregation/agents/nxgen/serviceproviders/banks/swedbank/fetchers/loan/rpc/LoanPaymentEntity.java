package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.DescriptiveAmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanPaymentEntity {
    private String description;
    private List<DescriptiveAmountEntity> amounts;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDay;
    private String totalAmount;

    public String getDescription() {
        return description;
    }

    public List<DescriptiveAmountEntity> getAmounts() {
        return amounts;
    }

    public Date getDueDay() {
        return dueDay;
    }

    public String getTotalAmount() {
        return totalAmount;
    }
}
