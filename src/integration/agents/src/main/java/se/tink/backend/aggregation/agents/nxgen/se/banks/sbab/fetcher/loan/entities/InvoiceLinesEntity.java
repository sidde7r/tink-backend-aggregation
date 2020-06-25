package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvoiceLinesEntity extends StandardResponse {
    private BigDecimal amount;
    private String invoiceLineType;
    private BigDecimal lineNumber;
    private String typeDescription;
    private String typeDescriptionDetail;

    public BigDecimal getAmount() {
        return amount;
    }

    public String getInvoiceLineType() {
        return invoiceLineType;
    }

    public BigDecimal getLineNumber() {
        return lineNumber;
    }

    public String getTypeDescription() {
        return typeDescription;
    }

    public String getTypeDescriptionDetail() {
        return typeDescriptionDetail;
    }
}
