package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractExecutorTransactionEntity {
    protected LinksEntity links;
    protected String id;
    protected String type;
    protected String currencyCode;
    protected boolean selected;
    protected String amount;
    protected TransferEntity transfer;

    public LinksEntity getLinks() {
        return links;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getAmount() {
        return amount;
    }

    public TransferEntity getTransfer() {
        return transfer;
    }
}
