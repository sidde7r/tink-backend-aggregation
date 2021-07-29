package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.error.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities.EeOConsultationMovementsTarjetabeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities.ListMovementsCardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Getter
@JsonObject
public class CardTransactionsResponse implements PaginatorResponse, EERpcResponse {
    @JsonProperty("EE_O_ConsultaMovimientosTarjetaBE")
    private EeOConsultationMovementsTarjetabeEntity eeOConsultationMovementsTarjetabe;

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
                                answer.getListMovementsCard().stream()
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
}
