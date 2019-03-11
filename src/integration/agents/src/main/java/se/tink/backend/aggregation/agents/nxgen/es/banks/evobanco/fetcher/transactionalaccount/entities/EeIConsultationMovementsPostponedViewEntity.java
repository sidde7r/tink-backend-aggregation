package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EeIConsultationMovementsPostponedViewEntity {
    @JsonProperty("usuarioBE")
    private String userbe;

    @JsonProperty("acuerdo")
    private String agreement;

    @JsonProperty("Reposicionamiento")
    private RepositioningEntity repositioning;

    @JsonProperty("acuerdoBE")
    private String agreementbe;

    @JsonProperty("codigoEntidad")
    private String entityCode;

    @JsonIgnore
    private EeIConsultationMovementsPostponedViewEntity(Builder builder) {
        userbe = builder.userbe;
        agreement = builder.agreement;
        repositioning = builder.repositioning;
        agreementbe = builder.agreementbe;
        entityCode = builder.entityCode;
    }

    public static class Builder {
        private String userbe;
        private String agreement;
        private RepositioningEntity repositioning;
        private String agreementbe;
        private String entityCode;

        public EeIConsultationMovementsPostponedViewEntity.Builder withUserbe(String userbe) {
            this.userbe = userbe;
            return this;
        }

        public EeIConsultationMovementsPostponedViewEntity.Builder withAgreement(
                String agreement) {
            this.agreement = agreement;
            return this;
        }

        public EeIConsultationMovementsPostponedViewEntity.Builder withRepositioning(
                RepositioningEntity repositioning) {
            this.repositioning = repositioning;
            return this;
        }

        public EeIConsultationMovementsPostponedViewEntity.Builder withAgreementbe(
                String agreementbe) {
            this.agreementbe = agreementbe;
            return this;
        }

        public EeIConsultationMovementsPostponedViewEntity.Builder withEntityCode(
                String entityCode) {
            this.entityCode = entityCode;
            return this;
        }

        public EeIConsultationMovementsPostponedViewEntity build() {
            return new EeIConsultationMovementsPostponedViewEntity(this);
        }
    }
}
