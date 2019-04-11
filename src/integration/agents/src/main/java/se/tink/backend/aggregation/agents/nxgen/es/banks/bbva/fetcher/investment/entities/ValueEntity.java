package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValueEntity {
    private AmountEntity value;
    private AmountEntity freeAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date transactionDate;

    public AmountEntity getValue() {
        return value;
    }

    public AmountEntity getFreeAmount() {
        return freeAmount;
    }
}
