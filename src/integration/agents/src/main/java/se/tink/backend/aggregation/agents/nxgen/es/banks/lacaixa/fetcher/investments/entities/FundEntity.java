package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FundEntity {
    @JsonProperty("fondoNombre")
    private String fundName;
    @JsonProperty("tieneOrdenesPendientes")
    private boolean hasPendingOrders;
    @JsonProperty("inversion")
    private BalanceEntity investment;
    @JsonProperty("saldoActual")
    private BalanceEntity currentBalance;
    @JsonProperty("plusvalia")
    private BalanceEntity valueChange;
    private boolean permiteInfoPlusvalia;
    private double incremento;
    private OptionsEntity opciones;
    private String refValCuenta;
    private String refValEmpresaGestora;
    private String refValCodigoFondo;
    @JsonProperty("codigoFondo")
    private String fundCode;
    @JsonProperty("refValExpediente")
    private String fundReference;
    @JsonProperty("divisa")
    private String currency;
    private String alias;

    public String getFundCode() {
        return fundCode;
    }

    public String getFundReference() {
        return fundReference;
    }

    public String getCurrency() {
        return currency;
    }
}
