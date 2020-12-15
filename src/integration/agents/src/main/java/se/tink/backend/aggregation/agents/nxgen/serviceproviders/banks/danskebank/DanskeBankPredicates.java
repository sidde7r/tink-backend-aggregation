package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.GroupAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.GroupEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc.SecurityEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public final class DanskeBankPredicates {
    public static Predicate<AccountEntity> knownCheckingAccountProducts(
            List<String> checkingAccountProducts) {
        return accountEntity -> checkingAccountProducts.contains(accountEntity.getAccountProduct());
    }

    public static Predicate<AccountEntity> knownSavingsAccountProducts(
            List<String> savingsAccountProducts) {
        return accountEntity -> savingsAccountProducts.contains(accountEntity.getAccountProduct());
    }

    public static Predicate<AccountEntity> knownLoanAccountProducts(
            Map<String, LoanDetails.Type> loanByAccountProduct) {
        return accountEntity -> loanByAccountProduct.containsKey(accountEntity.getAccountProduct());
    }

    public static final Predicate<AccountEntity> CREDIT_CARDS =
            a -> Objects.equals(DanskeBankConstants.Account.CREDIT_CARD_CODE, a.getCardType());

    public static final Predicate<GroupEntity> GROUPS_WITH_ACCOUNTS =
            g -> !g.getAccounts().isEmpty();

    public static final Predicate<SecurityEntity> NON_ZERO_QUANTITY =
            s -> !s.getQuantity().equals(BigDecimal.ZERO);

    public static final Predicate<GroupAccountEntity> ALL_INVESTMENTS_GROUP =
            ga -> ga.getType().equals(DanskeBankConstants.Account.ALL_INVESTMENTS_GROUP);
}
