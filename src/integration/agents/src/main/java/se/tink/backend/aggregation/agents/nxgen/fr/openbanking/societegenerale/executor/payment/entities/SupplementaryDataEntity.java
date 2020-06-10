package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class SupplementaryDataEntity {
    private List<String> acceptedAuthenticationApproach;
    private String successfulReportUrl;
    private String unsuccessfulReportUrl;

    @JsonIgnore
    public static SupplementaryDataEntity of(PaymentRequest paymentRequest, String redirectUrl) {
        List<String> authenticationApproach = new ArrayList<>();
        authenticationApproach.add("REDIRECT");
        return SupplementaryDataEntity.builder()
                .acceptedAuthenticationApproach(authenticationApproach)
                .successfulReportUrl(redirectUrl)
                .unsuccessfulReportUrl(redirectUrl)
                .build();
    }
}
