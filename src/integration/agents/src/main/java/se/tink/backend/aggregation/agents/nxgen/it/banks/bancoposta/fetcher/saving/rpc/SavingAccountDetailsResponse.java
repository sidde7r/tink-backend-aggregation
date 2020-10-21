package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.entity.SavingTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class SavingAccountDetailsResponse {
    private Body body;

    @JsonObject
    @Getter
    public static class Body {
        @JsonProperty("listaMovimentoRisparmioPostale")
        private List<SavingTransactionEntity> transactions;

        private Saldo saldo;

        @JsonObject
        @Data
        public static class Saldo {
            @JsonProperty("saldoContabile")
            private String bookedBalance;

            @JsonProperty("saldoDisponibile")
            private String availableBalance;
        }
    }
}
