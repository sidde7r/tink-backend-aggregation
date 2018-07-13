package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.BancoPopularCustomBTD6;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.BancoPopularCustomEccas211SPartEMV;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchTransactionsResponse extends BancoPopularResponse {
    private BancoPopularCustomBTD6 customBtd6ECOAS211F;

    public Collection<Transaction> getTinkTransactions() {
        if (customBtd6ECOAS211F != null && customBtd6ECOAS211F.getCustomEccas211SPARTEMV() != null) {
            return customBtd6ECOAS211F.getCustomEccas211SPARTEMV().stream()
                    .map(BancoPopularCustomEccas211SPartEMV::toTinkTransaction)
                    .collect(Collectors.toList());
        }

        return Collections.EMPTY_LIST;
    }

    public BancoPopularCustomBTD6 getCustomBtd6ECOAS211F() {
        return customBtd6ECOAS211F;
    }

    public void setCustomBtd6ECOAS211F(BancoPopularCustomBTD6 customBtd6ECOAS211F) {
        this.customBtd6ECOAS211F = customBtd6ECOAS211F;
    }
}
