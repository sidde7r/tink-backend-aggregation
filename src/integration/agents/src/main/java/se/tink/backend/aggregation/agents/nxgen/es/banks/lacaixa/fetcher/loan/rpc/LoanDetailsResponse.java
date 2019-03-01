package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsResponse {

    @JsonProperty("importeContratado")
    private String totalAmount;

    @JsonProperty("cuotaMensual")
    private String monthlyFee;

    @JsonProperty("plazo")
    private Long timeLimit;

    @JsonProperty("titular")
    private String title;

    @JsonProperty("numeroContrato")
    private String contractNumber;

    @JsonProperty("cuentaRelacionada")
    private String relatedAccountNumber;

    private String alias;
    //  format: ddmmyyyy
    @JsonProperty("fechaContratacion")
    private String contractDate;

    @JsonProperty("tipoInteresNominal")
    private String nominalInterest;
    // format: ddmmyy
    @JsonProperty("fechaVencimiento")
    private String endDate;

    @JsonProperty("pagosSucesivos")
    private String sufulPaymentMessage;

    @JsonProperty("fechaProximaCuota")
    private String nextPaymentDay;
    // Seems like reference id for account, but need to doublecheck
    @JsonProperty("refValContratoRelacionado")
    private String relatedAccountId;

    @JsonProperty("simulacionAmortizacionDisponible")
    private Boolean amortizationSimulation;

    @JsonProperty("cuotasPendientes")
    private Long pendingFees;

    @JsonProperty("importeMinimoAmortizacion")
    private String minimalAmortization;

    @JsonProperty("capitalPendiente")
    private String capitalToPay;

    @JsonProperty("proximoRecibo")
    private String nextPeaymentAmount;

    @JsonProperty("simulacionCancelacionDisponible")
    private Boolean cancelationSimulation;

    public String getMonthlyFee() {
        return monthlyFee;
    }

    public String getTitle() {
        return title;
    }

    public String getRelatedAccountNumber() {
        return relatedAccountNumber;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getNextPaymentDay() {
        return nextPaymentDay;
    }

    public String getNominalInterest() {
        return nominalInterest;
    }

    public String getMinimalAmortization() {
        return minimalAmortization;
    }

    public String getCapitalToPay() {
        return capitalToPay;
    }

    public String getNextPeaymentAmount() {
        return nextPeaymentAmount;
    }

}
