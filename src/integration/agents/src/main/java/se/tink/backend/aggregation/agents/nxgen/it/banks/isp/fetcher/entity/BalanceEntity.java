package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class BalanceEntity {
    @JsonProperty("fido")
    private double debit;

    @JsonProperty("saldoContabile")
    private double currentBalance;

    @JsonProperty("saldoDisponibile")
    private double availableBalance;

    @JsonProperty("saldoDisponibileFidoEscluso")
    private double availableBalanceMinusDebit;
}
