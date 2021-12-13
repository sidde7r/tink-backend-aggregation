package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class ListMovementsCardEntity {
    private static final Logger log = LoggerFactory.getLogger(ListMovementsCardEntity.class);

    @JsonProperty("interesCarencia")
    private String interestLack;

    @JsonProperty("numFormasPago")
    private String onFormsPaid;

    @JsonProperty("fechaHoraOperacion")
    private String dateOperationTime;

    @JsonProperty("tipoMovimiento")
    private String movementType;

    @JsonProperty("tipoFactura")
    private String invoiceType;

    @JsonProperty("codigoActividad")
    private String activityCode;

    @JsonProperty("codigoPais")
    private String countryCode;

    @JsonProperty("importeTotal")
    private String total;

    @JsonProperty("importeIntereses")
    private String amountInterests;

    @JsonProperty("comision")
    private String commission;

    @JsonProperty("claveAutorizacion")
    private String authorizationKey;

    @JsonProperty("numExtracto")
    private String surelyExtract;

    @JsonProperty("movimientosScp")
    private String scpMovements;

    @JsonProperty("numMovimientoExtracto")
    private String numAbstractMovement;

    @JsonProperty("importeOriginal")
    private String originalAmount;

    private String panToken;

    @JsonProperty("importeOperacion")
    private String operationAmount;

    @JsonProperty("indicadorTitBenef")
    private String titBenefIndicator;

    @JsonProperty("indicadorHCE")
    private String checIndicator;

    @JsonProperty("importeCuota")
    private String amountFee;

    @JsonProperty("monedaOperacion")
    private String operationCurrency;

    @JsonProperty("indicadorAplazable")
    private String delayIndicator;

    @JsonProperty("signoImporte")
    private String amountSign;

    @JsonProperty("estadoOperacion")
    private String operationState;

    @JsonProperty("indicador")
    private String indicator;

    @JsonProperty("porcentajeIntereses")
    private String interestPercentage;

    @JsonProperty("monedaOriginal")
    private String originalCurrency;

    @JsonProperty("descTipoOperacion")
    private String descOperationType;

    @JsonProperty("importeCapital")
    private String capitalAmount;

    @JsonProperty("importeComisionApertura")
    private String amountCommissionOpening;

    @JsonProperty("numeroOperacion")
    private String operationNumber;

    @JsonProperty("lugarOperacion")
    private String placeOfOperation;

    @JsonProperty("formaPago")
    private String paymentForm;

    @JsonProperty("ListaFormasPago")
    private List<PaymentFormsListEntity> paymentFormsList;

    @JsonProperty("nombreComercio")
    private String tradeName;

    @JsonProperty("codigoComercio")
    private String commerceCode;

    public CreditCardTransaction toTinkTransaction() {
        return new CreditCardTransaction.Builder()
                .setAmount(
                        ExactCurrencyAmount.inEUR(
                                AgentParsingUtils.parseAmount(getTransactionAmount())))
                .setDateTime(getZonedDateTime())
                .setDescription(getDescription())
                .build();
    }

    private String getTransactionAmount() {
        return ("+".equals(amountSign) ? "-" : "+") + operationAmount;
    }

    private String getDescription() {
        return tradeName == null ? descOperationType : tradeName;
    }

    private ZonedDateTime getZonedDateTime() {
        LocalDateTime unzonedLocalDateTime =
                LocalDateTime.parse(
                        dateOperationTime, EvoBancoConstants.Constants.DATE_TIME_FORMATTER);
        return ZonedDateTime.of(
                unzonedLocalDateTime, ZoneId.of(EvoBancoConstants.Constants.MADRID_ZONE_ID));
    }

    public boolean isCreditTransaction() {
        final boolean isCreditTransaction =
                Constants.CREDIT_TRANSACTION_TYPES.contains(movementType);
        if (!isCreditTransaction) {
            log.warn("Unknown card transaction type {}", movementType);
        }
        return isCreditTransaction;
    }
}
