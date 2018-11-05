package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.CategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.ContractSubtypeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.EarningCategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities.OperacionDGO;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

@JsonObject
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
    private String fechaValor; // Not sure how to translate? "valueDate" doesn't sound right

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
    private String transactionDate;

    public boolean isReceipt() {
        return receipt;
    }

    public ContractSubtypeEntity getContractSubtype() {
        return contractSubtype;
    }

    public List<CategoryEntity> getCategories() {
        return categories;
    }

    public String getDescription() {
        return description;
    }

    public String getFechaValor() {
        return fechaValor;
    }

    public OperacionDGO getOperacionDGO() {
        return operacionDGO;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public EarningCategoryEntity getEarningCategory() {
        return earningCategory;
    }

    public AmountEntity getTransactionAmount() {
        return transactionAmount;
    }

    public String getIndFinanciacion() {
        return indFinanciacion;
    }

    public ContractEntity getContratoPrincipalOperacion() {
        return contratoPrincipalOperacion;
    }

    public int getDiaMvto() {
        return diaMvto;
    }

    public int getNummov() {
        return nummov;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(transactionAmount.toTinkAmount())
                .setDescription(description)
                .setDate(DateUtils.parseDate(transactionDate))
                .build();
    }
}
