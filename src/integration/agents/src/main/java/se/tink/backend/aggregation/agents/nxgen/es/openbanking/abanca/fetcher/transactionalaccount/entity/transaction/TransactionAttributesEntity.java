package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAttributesEntity {

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    @JsonProperty("concept")
    private String concept;

    @JsonProperty("amount")
    private AmountEntity amount;

    public Date getDate() {
        return date;
    }

    public AmountEntity getAmount() {
        return amount;
    }

    public String getConcept() {
        return concept;
    }
}
