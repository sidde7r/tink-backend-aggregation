package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanEntity {
    protected AmountEntity balance;
    protected String iconReference;
    protected String id;
    protected String loanCategory;
    protected String loanNumber;
    protected NextPaymentEntity nextPayment;

    @JsonProperty("usePurpose")
    protected String name;

    public String getId() {
        return id;
    }

    public String getName() {
        if (Strings.isNullOrEmpty(name)) {
            return "";
        }
        // `name` is in capital letters
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }
}
