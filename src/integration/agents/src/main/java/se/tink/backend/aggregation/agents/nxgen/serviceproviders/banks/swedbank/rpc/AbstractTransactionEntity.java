package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractTransactionEntity {
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date date;
    protected String description;
    protected String currency;
    protected String amount;

    public Date getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }
}
