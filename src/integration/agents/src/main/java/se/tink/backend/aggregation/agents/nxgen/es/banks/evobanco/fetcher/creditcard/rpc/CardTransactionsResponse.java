package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities.EeOConsultationMovementsTarjetabeEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard.entities.ListMovementsCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonObject
public class CardTransactionsResponse implements PaginatorResponse {
    @JsonProperty("EE_O_ConsultaMovimientosTarjetaBE")
    private EeOConsultationMovementsTarjetabeEntity eeOConsultationMovementsTarjetabe;

    @Override
    public Collection<Transaction> getTinkTransactions() {
        if (eeOConsultationMovementsTarjetabe
                .getReturnCode()
                .equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE)) {
            return Collections.emptyList();
        }

        return eeOConsultationMovementsTarjetabe.getAnswer().getListMovementsCard().stream()
                .filter(ListMovementsCardEntity::isCreditTransaction)
                .map(ListMovementsCardEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        if (eeOConsultationMovementsTarjetabe
                .getReturnCode()
                .equals(EvoBancoConstants.ReturnCodes.UNSUCCESSFUL_RETURN_CODE)) {
            return Optional.of(false);
        }

        return Optional.of("1".equals(eeOConsultationMovementsTarjetabe.getAnswer().getMoreData()));
    }
}
