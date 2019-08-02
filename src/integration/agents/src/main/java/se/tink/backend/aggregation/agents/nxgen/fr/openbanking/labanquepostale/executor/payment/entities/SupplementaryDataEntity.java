package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.executor.payment.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;

@JsonObject
public class SupplementaryDataEntity {
    private List<String> acceptedAuthenticationApproach;
    private String successfulReportUrl;
    private String unsuccessfulReportUrl;

    public static SupplementaryDataEntity of(PaymentRequest paymentRequest) {
        List<String> authenticationApproach = new ArrayList<>();
        authenticationApproach.add("DECOUPLED");
        return new SupplementaryDataEntity.Builder()
                .withAcceptedAuthenticationApproach(authenticationApproach)
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
