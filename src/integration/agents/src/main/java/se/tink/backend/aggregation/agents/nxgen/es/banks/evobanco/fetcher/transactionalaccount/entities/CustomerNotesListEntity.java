package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.ZoneId;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Constants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class CustomerNotesListEntity {
    @JsonProperty("saldoArrastre")
    private String dragBalance;

    @JsonProperty("fechaValor")
    private String valueDate;

    @JsonProperty("indDisposicion")
    private String inDisposicion;

    @JsonProperty("importeTotal")
    private String total;

    @JsonProperty("comision")
    private String commission;

    @JsonProperty("codigoOrigenApunte")
    private String sourceCodeNote;

    @JsonProperty("porcentajeInteres")
    private String percentageInterest;

    @JsonProperty("codigoInternoCentro")
    private String internalCodeCenter;

    @JsonProperty("textoRemitente")
    private String senderText;

    private String terminal;

    @JsonProperty("importeInteres")
    private String amountInterest;

    @JsonProperty("acuerdoDocumento")
    private String agreementDocument;

    @JsonProperty("numeroSecuencialApunte")
    private String sequentialNumber;

    @JsonProperty("numSecuencialApunteDoc")
    private String sequentialNumPointDoc;

    @JsonProperty("fechaOperacion")
    private String dateOfTransaction;

    @JsonProperty("importeCuota")
    private String amountFee;

    @JsonProperty("identificadorOrigenApunte")
    private String identifierOriginAim;

    @JsonProperty("signo")
    private String sign;

    @JsonProperty("codigoOrigen")
    private String sourceCode;

    @JsonProperty("codigoMoneda")
    private String currencyCode;

    @JsonProperty("carencia")
    private String lack;

    @JsonProperty("codigoExtMoneda")
    private String extCurrencyCode;

    @JsonProperty("numSecuencialDocumento")
    private String sequentialNumDocument;

    @JsonProperty("importeApunte")
    private String amountAmount;

    @JsonProperty("importeCapital")
    private String capitalAmount;

    @JsonProperty("codigoDocumento")
    private String documentCode;

    public CustomerNotesListEntity() {}

    public CustomerNotesListEntity(
            String valueDate,
            String senderText,
            String dateOfTransaction,
            String sign,
            String amountAmount,
            String sequentialNumber) {
        this.valueDate = valueDate;
        this.senderText = senderText;
        this.dateOfTransaction = dateOfTransaction;
        this.sign = sign;
        this.amountAmount = amountAmount;
        this.sequentialNumber = sequentialNumber;
    }

    public String getSequentialNumber() {
        return sequentialNumber;
    }

    public String getDragBalance() {
        return dragBalance;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getExactCurrencyAmount())
                .setDate(DateUtils.parseDate(dateOfTransaction))
                .setDescription(senderText)
                .setPending(isPending())
                .build();
    }

    private ExactCurrencyAmount getExactCurrencyAmount() {
        // It appears in some cases that the actual amount has a negative sign and information about
        // the sign, in those cases (double checked in the app), the amount should appear as
        // positive
        final ExactCurrencyAmount amount =
                ExactCurrencyAmount.inEUR(AgentParsingUtils.parseAmount(amountAmount));
        if (Constants.ACCOUNT_TRANSACTION_PLUS_SIGN.equalsIgnoreCase(sign)) {
            return amount;
        } else {
            return amount.negate();
        }
    }

    private boolean isPending() {
        LocalDate valueDateParsed =
                LocalDate.parse(valueDate, EvoBancoConstants.Constants.DATE_FORMATTER);
        LocalDate dateOfTransactionParsed =
                LocalDate.parse(dateOfTransaction, EvoBancoConstants.Constants.DATE_FORMATTER);
        LocalDate nowDate = LocalDate.now(ZoneId.of(EvoBancoConstants.Constants.MADRID_ZONE_ID));

        // pending if valueDate is before the dateOfTransaction and today is before
        // dateOfTransaction
        return valueDateParsed.isBefore(dateOfTransactionParsed)
                && nowDate.isBefore(dateOfTransactionParsed);
    }
}
