package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.error.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities.EeOConsultationMovementsTarjetabeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities.ListMovementsCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.AnswerEntityTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.CustomerNotesListEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.entities.EeOConsultationMovementsPostponedViewEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Getter
@JsonObject
public class CardTransactionsResponse implements PaginatorResponse, EERpcResponse {
    @JsonProperty("EE_O_ConsultaMovimientosTarjetaBE")
    private EeOConsultationMovementsTarjetabeEntity eeOConsultationMovementsTarjetabe;

    public EeOConsultationMovementsTarjetabeEntity getEeOConsultationMovementsTarjetabe() {
        return eeOConsultationMovementsTarjetabe;
    }

    public List<ListMovementsCardEntity> getListMovementsCard() {
        if (eeOConsultationMovementsTarjetabe
                .getReturnCode()
                .equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE)) {
            return Collections.emptyList();
        }
        return eeOConsultationMovementsTarjetabe
                .getAnswer()
                .map(
                        answer ->
                                answer.getCardTransactionsList().stream()
                                        .filter(ListMovementsCardEntity::isCreditTransaction)
                                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    @Override
    public List<? extends Transaction> getTinkTransactions() {
        if (eeOConsultationMovementsTarjetabe
                .getReturnCode()
                .equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE)) {
            return Collections.emptyList();
        }

        return eeOConsultationMovementsTarjetabe
                .getAnswer()
                .map(
                        answer ->
                                answer.getCardTransactionsList().stream()
                                        .filter(ListMovementsCardEntity::isCreditTransaction)
                                        .map(ListMovementsCardEntity::toTinkTransaction)
                                        .collect(Collectors.toList()))
                .orElseGet(Collections::emptyList);
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        if (eeOConsultationMovementsTarjetabe
                .getReturnCode()
                .equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE)) {
            return Optional.of(false);
        }

        return Optional.of(
                eeOConsultationMovementsTarjetabe
                        .getAnswer()
                        .map(answer -> answer.getMoreData())
                        .orElse(false));
    }

    @Override
    public boolean isUnsuccessfulReturnCode() {
        return eeOConsultationMovementsTarjetabe.isUnsuccessfulReturnCode();
    }

    @Override
    public Optional<ErrorsEntity> getErrors() {
        return eeOConsultationMovementsTarjetabe.getErrors();
    }

    public TransactionsResponse toTransactionsResponse() {
        // from card movements list
        List<ListMovementsCardEntity> listMovementsCard = this.getListMovementsCard();
        // to account movements
        AtomicInteger counter = new AtomicInteger(listMovementsCard.size());
        List<CustomerNotesListEntity> customerNotesListEntities =
                listMovementsCard.stream()
                        .map(entity -> entity.toCustomerNotesListEntity(counter.getAndDecrement()))
                        .collect(Collectors.toList());

        EeOConsultationMovementsPostponedViewEntity eeOConsultationMovementsPostponedViewEntity =
                new EeOConsultationMovementsPostponedViewEntity();
        // put movements into Account Transactions Response
        AnswerEntityTransactionsResponse answer = new AnswerEntityTransactionsResponse();
        answer.setListCustomerNotes(customerNotesListEntities);
        eeOConsultationMovementsPostponedViewEntity.setAnswer(answer);
        if (listMovementsCard.isEmpty()) {
            // put errors into Account Transactions Response
            eeOConsultationMovementsPostponedViewEntity.setReturnCode(
                    this.getEeOConsultationMovementsTarjetabe().getReturnCode());
            Optional<ErrorsEntity> optionalErrors =
                    this.getEeOConsultationMovementsTarjetabe().getErrors();
            optionalErrors.ifPresent(eeOConsultationMovementsPostponedViewEntity::setErrors);
        }
        TransactionsResponse transactionsResponse = new TransactionsResponse();
        transactionsResponse.setEeOConsultationMovementsPostponedView(
                eeOConsultationMovementsPostponedViewEntity);
        return transactionsResponse;
    }
}
