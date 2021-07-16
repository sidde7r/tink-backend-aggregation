package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.einvoice.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.DetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EInvoiceEntity {
    private String id;
    private String currency;
    private LinksEntity links;
    private String amount;
    private String dueDate;
    private String hashedEinvoiceRefNo;
    private String payeeName;
    private boolean dueDatePassed;
    private DetailsEntity detailDocument;

    public String getId() {
        return id;
    }

    public String getCurrency() {
        return currency;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getAmount() {
        return amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getHashedEinvoiceRefNo() {
        return hashedEinvoiceRefNo;
    }

    public String getPayeeName() {
        return payeeName;
    }

    public boolean isDueDatePassed() {
        return dueDatePassed;
    }

    public DetailsEntity getDetailDocument() {
        return detailDocument;
    }
}
