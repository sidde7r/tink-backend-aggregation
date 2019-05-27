package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.balance.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.common.AccountInfoEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceResponse {

    private AccountInfoEntity account;
    private List<BalanceEntity> balances;

    public Amount getBalance() {
        return balances.stream()
                .filter(BalanceEntity::isInterimAvailable)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        VolksbankConstants.ErrorMessages.MISSING_BALANCE))
                .getBalanceAmount();
    }
}
