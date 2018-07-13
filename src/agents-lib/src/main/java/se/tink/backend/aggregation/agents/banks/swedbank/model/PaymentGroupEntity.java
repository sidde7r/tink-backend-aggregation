package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentGroupEntity {
    private List<PaymentAccountEntity> payees;

    public List<PaymentAccountEntity> getPayees() {
        return payees;
    }

    public void setPayees(List<PaymentAccountEntity> payees) {
        this.payees = payees;
    }
}
