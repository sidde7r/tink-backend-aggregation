package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class LoanEntity {
    private AmountEntity availableCapital;
    private AmountEntity initialCapital;
    private AmountEntity instalmentAmount;
    private String interest;
    private KeyValue interestType;
    private AmountEntity pendingCapital;
    private String pendingInstalments;
    private KeyValue recurrence;
    private KeyValue type;
    private String vencDate;

    public Double getInterest() {
        return StringUtils.parseAmount(interest);
    }

    @JsonIgnore
    public Amount getAmortized() {
        return initialCapital.parseToTinkAmount().subtract(pendingCapital.parseToTinkAmount());
    }

    @JsonIgnore
    public Amount getInitialBalance() {
        return initialCapital.parseToTinkAmount();
    }

    @JsonIgnore
    public Amount getInstalmentValue() {
        return instalmentAmount.parseToTinkAmount();
    }

    @JsonIgnore
    public Amount getBalance() {
        return pendingCapital.parseToTinkAmount();
    }
}
