package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.balance;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.VolksbankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String referenceDate;

    public boolean isInterimAvailable() {
        return VolksbankConstants.Balance.INTERIM_AVAILABLE.equalsIgnoreCase(balanceType);
    }

    public Amount getBalanceAmount() {
        return Optional.ofNullable(balanceAmount)
                .map(BalanceAmountEntity::toAmount)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_BALANCE));
    }
}
