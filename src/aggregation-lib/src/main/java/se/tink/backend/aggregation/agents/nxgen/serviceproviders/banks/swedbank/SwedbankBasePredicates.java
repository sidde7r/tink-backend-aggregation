package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.UpcomingTransactionEntity;

public class SwedbankBasePredicates {
    public static Predicate<BankEntity> filterBankId(String bankId) {
        Preconditions.checkNotNull(bankId, "You must provide a bankId for comparison.");
        return bankEntity -> bankId.equalsIgnoreCase(bankEntity.getBankId());
    }

    public static Predicate<UpcomingTransactionEntity> filterAccounts(String accountNumber) {
        Preconditions.checkNotNull(accountNumber, "You must provider a accountNumber for comparison");
        return upcomingTransactionEntity ->
                Objects.equals(accountNumber, upcomingTransactionEntity.getFromAccount().getFullyFormattedNumber());
    }
}
