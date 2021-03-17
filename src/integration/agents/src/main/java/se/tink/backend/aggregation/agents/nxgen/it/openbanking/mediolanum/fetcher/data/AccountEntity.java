package se.tink.backend.aggregation.agents.nxgen.it.openbanking.mediolanum.fetcher.data;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;

@Getter
public class AccountEntity {
    private String resourceId;
    private String iban;
    private String currency;
    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return balances == null ? Collections.emptyList() : balances;
    }
}
