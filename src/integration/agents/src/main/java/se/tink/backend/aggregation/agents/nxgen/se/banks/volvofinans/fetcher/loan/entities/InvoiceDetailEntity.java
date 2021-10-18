package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvoiceDetailEntity {
    @JsonProperty("fakturaText")
    private String invoiceDescription;

    @JsonProperty("fakturaBelopp")
    private double amount;
}
