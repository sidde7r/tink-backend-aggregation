package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class SavingAccountEntity extends AccountEntity {
    private static final Logger log = LoggerFactory.getLogger(SavingAccountEntity.class);

    public Optional<TransactionalAccount> toTransactionalAccount(
            BankProfile bankProfile,
            EngagementTransactionsResponse engagementTransactionsResponse) {
        if (type != null) {
            if (SwedbankBaseConstants.SavingAccountTypes.PENSION.equalsIgnoreCase(type)) {
                // Pension accounts are not present in the portfolio (investments) overview and the
                // AccountEntity does not contain any next links (as `ISK` for example).
                return toTransactionalAccount(
                        bankProfile, AccountTypes.SAVINGS, engagementTransactionsResponse);
            }

            // It seems as if the investment accounts has a type and the rest doesn't.
            log.info("Swedbank account type:[{}]", type);
            return Optional.empty();
        }

        return toTransactionalAccount(
                bankProfile, AccountTypes.SAVINGS, engagementTransactionsResponse);
    }
}
