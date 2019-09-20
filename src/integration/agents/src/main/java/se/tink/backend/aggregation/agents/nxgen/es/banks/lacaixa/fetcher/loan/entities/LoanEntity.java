package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanEntity {
    @JsonProperty("numContrato")
    private String contractNumber;

    @JsonProperty("idContrato")
    private String contractId;

    @JsonProperty("descContrato")
    private String contractDescription;

    @JsonProperty("nomAplicacion")
    private String applicationName;

    @JsonProperty("codProducto")
    private String productCode;

    @JsonProperty("importeConcedido")
    private String initialBalance;

    @JsonProperty("monedaImporteConcedido")
    private String currency;

    @JsonProperty("importePendiente")
    private String amountToPay;

    @JsonProperty("monedaImportePendiente")
    private String currencyToPay;

    @JsonProperty("fechaConstitucion")
    private String startDate;

    public String getProductCode() {
        return productCode;
    }

    public String getContractDescription() {
        return contractDescription;
    }

    public String getApplicationName() {
        return applicationName == null ? "" : applicationName;
    }

    public String getContractId() {
        return contractId;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public String getInitialBalance() {
        return initialBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmountToPay() {
        return amountToPay;
    }

    public String getCurrencyToPay() {
        return currencyToPay;
    }

    public String getStartDate() {
        return startDate;
    }
}
