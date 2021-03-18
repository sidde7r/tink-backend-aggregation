package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.entity.PaymentStatusDto;

@Data
public class PaymentStatusResponseDto {
    private String id;
    private PaymentStatusDto status;
}
