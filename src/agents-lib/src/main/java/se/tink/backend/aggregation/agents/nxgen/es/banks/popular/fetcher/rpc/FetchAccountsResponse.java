package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.BancoPopularCuenta;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.BancoPopularCustom;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FetchAccountsResponse extends BancoPopularResponse {
    private BancoPopularCustom custom;

    public Collection<TransactionalAccount> getTinkAccounts() {
        if (custom != null && custom.getCustomRr001014() != null) {
            return custom.getCustomRr001014().stream().map(BancoPopularCuenta::toTinkAccount).collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public BancoPopularCustom getCustom() {
        return custom;
    }

    public void setCustom(
            BancoPopularCustom custom) {
        this.custom = custom;
    }
}
