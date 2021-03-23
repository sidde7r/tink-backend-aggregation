package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecurringPaymentResponseDto {
    private String id;

    private String paymentProduct;

    private RecurringPaymentInitiationDto recurringPaymentInitiation;

    private String paymentStatus;

    @JsonProperty("_links")
    private LinksDto links;
}
