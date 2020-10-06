package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountDetailsResponse {
    private Body body;

    @Getter
    @JsonObject
    public static class Body {
        private String iban;
        private Saldo saldo;

        @Getter
        @JsonObject
        public static class Saldo {
            @JsonProperty("saldoContabile")
            private String bookedBalance;

            @JsonProperty("saldoDisponibile")
            private String availableBalance;

            @JsonProperty("segnoSaldoContabile")
            private String bookedBalanceSymbol;

            @JsonProperty("segnoSaldoDisponibile")
            private String availableBalanceSymbol;
        }
    }
}
