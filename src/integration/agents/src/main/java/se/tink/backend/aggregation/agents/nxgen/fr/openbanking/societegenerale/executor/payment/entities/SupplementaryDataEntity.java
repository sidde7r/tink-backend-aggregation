package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

import java.util.ArrayList;
import java.util.List;

@JsonObject
public class SupplementaryDataEntity {
    private List<String> acceptedAuthenticationApproach;
    private String successfulReportUrl;
    private String unsuccessfulReportUrl;


    @JsonIgnore
    public static SupplementaryDataEntity of(PaymentRequest paymentRequest,String redirectUrl) {
        List<String> authenticationApproach = new ArrayList<>();
        authenticationApproach.add("REDIRECT");
        return new SupplementaryDataEntity.Builder()
                .withAcceptedAuthenticationApproach(authenticationApproach)
                .withSuccessfulReportUrl(redirectUrl)
                .withUnsuccessfulReportUrl(redirectUrl)
                .build();
    }

    private SupplementaryDataEntity(Builder builder) {
        this.acceptedAuthenticationApproach = builder.acceptedAuthenticationApproach;
        this.successfulReportUrl = builder.successfulReportUrl;
        this.unsuccessfulReportUrl = builder.unsuccessfulReportUrl;
    }

    public SupplementaryDataEntity() {}

    public static class Builder {
        private List<String> acceptedAuthenticationApproach;
        private String successfulReportUrl;
        private String unsuccessfulReportUrl;

        public Builder withAcceptedAuthenticationApproach(
                List<String> acceptedAuthenticationApproach) {
            this.acceptedAuthenticationApproach = acceptedAuthenticationApproach;
            return this;
        }

        public Builder withSuccessfulReportUrl(String successfulReportUrl) {
            this.successfulReportUrl = successfulReportUrl;
            return this;
        }

        public Builder withUnsuccessfulReportUrl(String unsuccessfulReportUrl) {
            this.unsuccessfulReportUrl = unsuccessfulReportUrl;
            return this;
        }

        public SupplementaryDataEntity build() {
            return new SupplementaryDataEntity(this);
        }
    }
}
