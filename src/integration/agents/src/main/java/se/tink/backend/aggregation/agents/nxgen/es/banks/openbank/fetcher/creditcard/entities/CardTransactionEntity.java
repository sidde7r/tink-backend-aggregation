package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.EarningCategoryEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
@Getter
public class CardTransactionEntity {
    @JsonProperty("situacionDelMovimiento")
    private String situacionDelMovimiento;

    @JsonProperty("descFormaPago")
    private String descFormaPago;

    @JsonProperty("situacionContratoDESSITUA")
    private String situacionContratoDESSITUA;

    @JsonProperty("numSecDelMovto")
    private int numSecDelMovto;

    @JsonProperty("horaMovimiento")
    private String horaMovimiento;

    @JsonProperty("estadoPeticion")
    private String estadoPeticion;

    @JsonProperty("impOperacion")
    private AmountEntity impOperacion;

    @JsonProperty("redondeo")
    private AmountEntity redondeo;

    @JsonProperty("fechaLiquidacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaLiquidacion;

    @JsonProperty("codOperacionBancaria")
    private String codOperacionBancaria;

    @JsonProperty("txtCajero")
    private String description;

    @JsonProperty("descTipoSaldo")
    private String descTipoSaldo;

    @JsonProperty("fechaOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate operationDate;

    @JsonProperty("impMovimiento")
    private AmountEntity transactionAmount;

    @JsonProperty("codigoOperacionBasica")
    private String codigoOperacionBasica;

    @JsonProperty("indNfc")
    private String indNfc;

    @JsonProperty("categorias")
    private List<CategoryEntity> categories;

    @JsonProperty("indPagoFacil")
    private String indPagoFacil;

    @JsonProperty("numeroOperacionDGO")
    private int numeroOperacionDGO;

    @JsonProperty("categoriaGanadora")
    private EarningCategoryEntity categoriaGanadora;

    @JsonProperty("fechaMovimiento")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @JsonProperty("modalidad")
    private ModeEntity modality;

    @JsonProperty("fechaAnotacionMovimiento")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaAnotacionMovimiento;

    @JsonProperty("txtComercio")
    private String txtComercio;

    @JsonProperty("numeroMovimintoEnDia")
    private int numeroMovimintoEnDia;

    @JsonProperty("conceptoSaldo")
    private String conceptoSaldo;

    public CreditCardTransaction toTinkTransaction(CreditCardAccount account) {
        return (CreditCardTransaction)
                CreditCardTransaction.builder()
                        .setCreditAccount(account != null ? account.getAccountNumber() : null)
                        .setAmount(transactionAmount.toTinkAmount())
                        .setDescription(description)
                        .setDate(transactionDate)
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setBookingDate(new AvailableDateInformation(operationDate))
                                        .setExecutionDate(
                                                new AvailableDateInformation(
                                                        fechaAnotacionMovimiento))
                                        .setValueDate(
                                                new AvailableDateInformation(fechaLiquidacion))
                                        .build())
                        .setPending(!Optional.ofNullable(fechaLiquidacion).isPresent())
                        .setMutable(!Optional.ofNullable(fechaLiquidacion).isPresent())
                        .setProviderMarket(MarketCode.ES.toString())
                        .build();
    }
}
