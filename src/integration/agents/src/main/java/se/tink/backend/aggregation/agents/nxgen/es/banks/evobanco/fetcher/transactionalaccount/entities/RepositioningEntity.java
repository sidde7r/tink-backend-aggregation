package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RepositioningEntity {
    @JsonProperty("saldoArrastre")
    private String dragBalance;

    @JsonProperty("numeroSecuencialApunte")
    private String sequentialNumber;

    @JsonProperty("codigoMoneda")
    private String currencyCode;

    @JsonIgnore
    private RepositioningEntity(Builder builder) {
        dragBalance = builder.dragBalance;
        sequentialNumber = builder.sequentialNumber;
        currencyCode = builder.currencyCode;
    }

    public static class Builder {
        private String dragBalance;
        private String sequentialNumber;
        private String currencyCode;

        public RepositioningEntity.Builder withDragBalance(String dragBalance) {
            this.dragBalance = dragBalance;
            return this;
        }

        public RepositioningEntity.Builder withSequentialNumber(String sequentialNumber) {
            this.sequentialNumber = sequentialNumber;
            return this;
        }

        public RepositioningEntity.Builder withCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public RepositioningEntity build() {
            return new RepositioningEntity(this);
        }
    }
}
