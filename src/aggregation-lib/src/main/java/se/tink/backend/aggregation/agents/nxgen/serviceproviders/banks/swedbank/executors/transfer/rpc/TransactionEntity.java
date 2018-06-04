package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.transfer.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity {
    private LinksEntity links;
    private String id;
    private String type;
    private String currencyCode;
    private boolean selected;
    private String amount;
    private TransferEntity transfer;
    private String noteToSender;
    private boolean canSign;
    private boolean counterSigning;

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

    public String getNoteToSender() {
        return noteToSender;
    }

    public boolean isCanSign() {
        return canSign;
    }

    public boolean isCounterSigning() {
        return counterSigning;
    }
}
