package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities.SavingTransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class SavingTransactionResponse implements PaginatorResponse {
    private Body body;

    @JsonObject
    @Getter
    public static class Body {
        @JsonProperty("listaMovimentoRisparmioPostale")
        private List<SavingTransactionEntity> transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return getBody().getTransactions().stream()
                .map(SavingTransactionEntity::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        // Haven't noticed any signs of pagination in case of saving accounts
        return Optional.of(false);
    }
}
