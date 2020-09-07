package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.StandardResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvoiceLinesEntity extends StandardResponse {
    private BigDecimal amount;
    private String invoiceLineType;
    private BigDecimal lineNumber;
    private String typeDescription;
    private String typeDescriptionDetail;
}
