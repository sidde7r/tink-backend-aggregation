package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.Constants.SEQUENTIAL_NUMBER_PATTERN;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.error.ErrorsEntity;
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
                .withSequentialNumber(
                        String.format(
                                SEQUENTIAL_NUMBER_PATTERN,
                                Integer.parseInt(lastTransactionInPage.getSequentialNumber()) - 1))
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
                getEeOConsultationMovementsPostponedView().getAnswer() != null
                        && !getEeOConsultationMovementsPostponedView()
                                .getAnswer()
                                .getListCustomerNotes()
                                .isEmpty()
                        && !getEeOConsultationMovementsPostponedView()
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

    public void setEeOConsultationMovementsPostponedView(
            EeOConsultationMovementsPostponedViewEntity eeOConsultationMovementsPostponedView) {
        this.eeOConsultationMovementsPostponedView = eeOConsultationMovementsPostponedView;
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
            if (ErrorCodes.NO_TRANSACTIONS_FOUND.equals(getErrors().get().getShowCode())) {
                LOG.debug("No more transactions found.");
            } else {
                throw new IllegalStateException(
                        "Unknown unsuccessful return code " + getErrors().get().toString());
            }
        }
    }
}
