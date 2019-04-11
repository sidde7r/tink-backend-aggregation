package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.format.DateTimeFormatter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.Amount;

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
    private Amount amount;

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

    public void setAmount(double money) {
        this.amount = new Amount(ImaginBankConstants.CURRENCY, money);
    }

    public CreditCardTransaction toTinkTransaction() {

        CreditCardTransaction.Builder builder = CreditCardTransaction.builder();

        builder.setDate(transactionDate, DATE_FORMATTER);
        builder.setAmount(amount);
        builder.setDescription(description);

        return builder.build();
    }
}
