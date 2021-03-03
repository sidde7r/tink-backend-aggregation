package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentResponseDto {

    private String id;
    private String paymentProduct;
    private PaymentInitiationDto paymentInitiation;
    private String paymentStatus;
}
