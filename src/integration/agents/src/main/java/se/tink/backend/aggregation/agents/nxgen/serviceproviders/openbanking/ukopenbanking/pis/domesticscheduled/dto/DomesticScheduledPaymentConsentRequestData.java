package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.domesticscheduled.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@RequiredArgsConstructor
@Getter
public class DomesticScheduledPaymentConsentRequestData {

    private final String permission = "Create";

    private final String readRefundAccount = "Yes";

    private final DomesticScheduledPaymentInitiation initiation;
}
