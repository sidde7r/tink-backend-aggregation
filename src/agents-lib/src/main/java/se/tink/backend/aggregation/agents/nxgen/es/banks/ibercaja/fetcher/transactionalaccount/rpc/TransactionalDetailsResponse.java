package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.IberCajaInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities.TransactionDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class TransactionalDetailsResponse implements PaginatorResponse {

    @JsonProperty("Paginacion")
    private boolean pagination;
    @JsonProperty("NumeroMovimientos")
    private int numberOfTransactions;
    @JsonProperty("Divisa")
    private boolean badge;
    @JsonProperty("MostrarSaldo")
    private boolean showBalance;
    @JsonProperty("SaldoInicial")
    private double beginningBalance;
    @JsonProperty("Moneda")
    private String currency;
    @JsonProperty("Movimientos")
    private List<TransactionDetailsEntity> transactions;

    private static final Logger logger = LoggerFactory.getLogger(TransactionalDetailsResponse.class);

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionDetailsEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        if (pagination) {
            logger.info(SerializationUtils.serializeToString(this));
        }

        return Optional.empty();
    }
}
