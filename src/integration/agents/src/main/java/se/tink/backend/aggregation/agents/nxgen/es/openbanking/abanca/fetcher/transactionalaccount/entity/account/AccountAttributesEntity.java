package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountAttributesEntity {

    @JsonProperty("codigoDeTipoDeCuenta")
    private String accountTypeCode;

    @JsonProperty("codigoDeCuenta")
    private String accountCode; // iban

    @JsonProperty("divisa")
    private String currency;

    @JsonProperty("saldoTotal")
    private AmountEntity totalBalance;

    @JsonProperty("saldoDisponible")
    private AmountEntity availableBalance;

    @JsonProperty("saldoBloqueado")
    private AmountEntity blockedBalance;

    public String getAccountCode() {
        return accountCode;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }
}
