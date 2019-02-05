package se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.omasp.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionsEntity {
    private String id;
    private String title;
    private DateEntity date;
    private AmountEntity sum;
    private String type;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public DateEntity getDate() {
        return date;
    }

    public AmountEntity getSum() {
        return sum;
    }

    public String getType() {
        return type;
    }
}
