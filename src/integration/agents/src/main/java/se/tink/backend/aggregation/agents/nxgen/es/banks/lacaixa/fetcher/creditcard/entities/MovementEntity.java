package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
public class MovementEntity {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @JsonProperty("numeroMov")
    private int movNumber;

    @JsonProperty("descMov")
    private String description;

    @JsonProperty("fechaMov")
    private String transactionDate;

    @JsonProperty("horaMov")
    private String movTime;

    @JsonProperty("estadoMov")
    private String movStatus;

    @JsonProperty("importeMov")
    private BigDecimal amount;

    @JsonProperty("fechaLiqMov")
    private String leahMoe;

    @JsonProperty("numContratoMov")
    private String inAMovAgreement;

    private String numTarjetaMov;

    @JsonProperty("aliasTarjetaMov")
    private String aliasMovCard;

    @JsonProperty("tipoTarjetaMov")
    private String movCardType;

    @JsonProperty("codCategoriaMov")
    private int codeMovCategory;

    @JsonProperty("numComercioMov")
    private long inATradingMov;

    @JsonProperty("ramoComercioMov")
    private String movTradeBouquet;

    @JsonProperty("indAdjuntos")
    private String intoAdjuntos;

    @JsonProperty("indLeido")
    private String inLeido;

    @JsonProperty("indFraccionable")
    private String inFraccionable;

    @JsonProperty("accesoDetalleMovimiento")
    private String accessDetailMovement;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount account) {
        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setCreditAccount(account)
                        .setDate(transactionDate, DATE_FORMATTER)
                        .setAmount(
                                ExactCurrencyAmount.of(amount.negate(), LaCaixaConstants.CURRENCY))
                        .setDescription(description)
                        .setPending(false)
                        .setMutable(false)
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setBookingDate(
                                                new AvailableDateInformation(
                                                        LocalDate.parse(
                                                                transactionDate, DATE_FORMATTER)))
                                        .setValueDate(
                                                new AvailableDateInformation(
                                                        LocalDate.parse(
                                                                transactionDate, DATE_FORMATTER)))
                                        .build())
                        .setProviderMarket(MarketCode.ES.toString())
                        .build();
    }
}
