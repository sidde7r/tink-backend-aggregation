package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities.InfoEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.RepositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@XmlRootElement(name = "methodResult")
public class TransactionsResponse implements TransactionKeyPaginatorResponse<RepositionEntity> {
    private InfoEntity info;

    @JsonProperty("contador")
    private String counter;

    @JsonProperty("nombreProducto")
    private String productName;

    @JsonProperty("importeSaldo")
    private AmountEntity balance;

    @JsonProperty("importeDisponible")
    private AmountEntity disposible;

    private AmountEntity importeCta;

    @JsonProperty("finLista")
    private String endOfList;

    @JsonProperty("contratoNuevo")
    private ContractEntity newContract;

    @JsonProperty("codProducto")
    private String productCode;

    @JsonProperty("listadoMovimientos")
    private List<TransactionEntity> transactionList;

    @JsonProperty("repo")
    private RepositionEntity reposition;

    public InfoEntity getInfo() {
        return info;
    }

    public String getCounter() {
        return counter;
    }

    public String getProductName() {
        return productName;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public AmountEntity getDisposible() {
        return disposible;
    }

    public AmountEntity getImporteCta() {
        return importeCta;
    }

    public String getEndOfList() {
        return endOfList;
    }

    public ContractEntity getNewContract() {
        return newContract;
    }

    public String getProductCode() {
        return productCode;
    }

    public List<TransactionEntity> getTransactionList() {
        return transactionList;
    }

    public RepositionEntity getReposition() {
        return reposition;
    }

    @Override
    public Collection<Transaction> getTinkTransactions() {

        if (transactionList == null || transactionList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        return transactionList.stream()
                .map(TransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(SantanderEsConstants.Indicators.NO.equalsIgnoreCase(endOfList));
    }

    @Override
    public RepositionEntity nextKey() {
        return reposition;
    }
}
