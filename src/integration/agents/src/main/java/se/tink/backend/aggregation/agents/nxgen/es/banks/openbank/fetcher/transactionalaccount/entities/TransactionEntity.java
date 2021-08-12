package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import java.time.LocalDate;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.ContractSubtypeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.EarningCategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.OperacionDGO;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@JsonObject
@Getter
public class TransactionEntity {
    @JsonProperty("recibo")
    private boolean receipt;

    @JsonProperty("subtipoContratoPrincipal")
    private ContractSubtypeEntity contractSubtype;

    @JsonProperty("categorias")
    private List<CategoryEntity> categories;

    @JsonProperty("conceptoTabla")
    private String description;

    @JsonProperty("fechaValor")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate valueDate;

    @JsonProperty("operacionDGO")
    private OperacionDGO operacionDGO;

    @JsonProperty("saldo")
    private AmountEntity balance;

    @JsonProperty("categoriaGanadora")
    private EarningCategoryEntity earningCategory;

    @JsonProperty("importe")
    private AmountEntity transactionAmount;

    @JsonProperty("indFinanciacion")
    private String indFinanciacion;

    @JsonProperty("contratoPrincipalOperacion")
    private ContractEntity contratoPrincipalOperacion;

    @JsonProperty("diaMvto")
    private int diaMvto;

    @JsonProperty("nummov")
    private int nummov;

    @JsonProperty("fechaOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    public boolean isReceipt() {
        return receipt;
    }

    public Transaction toTinkTransaction() {
        return (Transaction)
                Transaction.builder()
                        .setAmount(transactionAmount.toTinkAmount())
                        .setDescription(description)
                        .setDate(transactionDate)
                        .setTransactionDates(
                                TransactionDates.builder()
                                        .setBookingDate(
                                                new AvailableDateInformation(transactionDate))
                                        .setValueDate(new AvailableDateInformation(valueDate))
                                        .setExecutionDate(new AvailableDateInformation(valueDate))
                                        .build())
                        .setPending(!Optional.ofNullable(valueDate).isPresent())
                        .setMutable(!Optional.ofNullable(valueDate).isPresent())
                        .setProviderMarket(MarketCode.ES.toString())
                        .build();
    }
}
