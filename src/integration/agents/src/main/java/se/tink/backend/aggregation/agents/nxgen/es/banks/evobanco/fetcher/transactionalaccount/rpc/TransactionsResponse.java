package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.CustomerNotesListEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.EeOConsultationMovementsPostponedViewEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.RepositioningEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsResponse
        implements TransactionKeyPaginatorResponse<RepositioningEntity>, EERpcResponse {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionsResponse.class);

    @JsonProperty("EE_O_ConsultaMovimientosVistaAplazada")
    private EeOConsultationMovementsPostponedViewEntity eeOConsultationMovementsPostponedView;

    @Override
    public RepositioningEntity nextKey() {
        CustomerNotesListEntity lastTransactionInPage =
                getEeOConsultationMovementsPostponedView()
                        .getAnswer()
                        .getListCustomerNotes()
                        .get(getNumberOfTransactionsInPage() - 1);

        return new RepositioningEntity.Builder()
                .withDragBalance(lastTransactionInPage.getDragBalance())
                .withSequentialNumber(lastTransactionInPage.getSequentialNumber())
                .withCurrencyCode(lastTransactionInPage.getCurrencyCode())
                .build();
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return getEeOConsultationMovementsPostponedView().getAnswer().getListCustomerNotes()
                .stream()
                .map(CustomerNotesListEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(
                !getEeOConsultationMovementsPostponedView()
                        .getAnswer()
                        .getListCustomerNotes()
                        .get(getNumberOfTransactionsInPage() - 1)
                        .getSequentialNumber()
                        .equals(EvoBancoConstants.Constants.FIRST_SEQUENTIAL_NUMBER));
    }

    private int getNumberOfTransactionsInPage() {
        return getEeOConsultationMovementsPostponedView().getAnswer().getListCustomerNotes().size();
    }

    public EeOConsultationMovementsPostponedViewEntity getEeOConsultationMovementsPostponedView() {
        return eeOConsultationMovementsPostponedView;
    }

    @Override
    public boolean isUnsuccessfulReturnCode() {
        return eeOConsultationMovementsPostponedView.isUnsuccessfulReturnCode();
    }

    @Override
    public Optional<ErrorsEntity> getErrors() {
        return eeOConsultationMovementsPostponedView.getErrors();
    }

    @Override
    public void handleReturnCode() {
        if (isUnsuccessfulReturnCode()) {
            switch (getErrors().get().getShowCode()) {
                case EvoBancoConstants.ErrorCodes.NO_TRANSACTIONS_FOUND:
                    LOG.debug("No more transactions found.");
                    break;

                default:
                    throw new IllegalStateException(
                            "Unknown unsuccessful return code " + getErrors().get().toString());
            }
        }
    }
}
