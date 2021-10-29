package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardDataEntityGlobalPositionResponse {
    @JsonProperty("limiteDiaCajeroDebito")
    private String limitDayATMDebit;

    @JsonProperty("importeMinimo")
    private String minimumAmount;

    @JsonProperty("limiteDiaCajeroCredito")
    private String creditCardDayATMLimit;

    @JsonProperty("limiteCreditoCuenta")
    private String limitCreditAccount;

    @JsonProperty("saldoDisponibleCredito")
    private String creditAvailableBalance;

    @JsonProperty("descripcionEstado")
    private String stateDescription;

    @JsonProperty("importeFijo")
    private String fixedAmount;

    @JsonProperty("saldoDispuestoCredito")
    private String creditUsed;

    @JsonProperty("descripcionFormaPago")
    private String descriptionPaymentForm;

    @JsonProperty("importeAutorizado")
    private String authorizedAmount;

    @JsonProperty("indicadorTarjetaDual")
    private String dualCardIndicator;

    @JsonProperty("porcentaje")
    private String percentage;

    @JsonProperty("iBANcuentaAsociada")
    private String ibanAssociatedAccount;

    @JsonProperty("descripcionBloqueo")
    private String descriptionLock;

    @JsonProperty("tipoTarjeta")
    private String cardType;

    @JsonProperty("limiteCreditoTarjeta")
    private String cardCreditLimit;

    @JsonProperty("titular")
    private String holder;

    private String panToken;

    @JsonProperty("codigoBloqueo")
    private String lockCode;

    @JsonProperty("limiteDebitoCuenta")
    private String limitDebitAccount;

    @JsonProperty("importeVencido")
    private String amountDue;
}
