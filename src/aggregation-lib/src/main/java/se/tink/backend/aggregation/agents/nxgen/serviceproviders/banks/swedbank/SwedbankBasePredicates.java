package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import com.google.common.base.Preconditions;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.FromAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.ConfirmedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.ConfirmedTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.UpcomingTransactionEntity;
import se.tink.backend.core.Amount;
import se.tink.backend.core.transfer.Transfer;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

public class SwedbankBasePredicates {
    private static final AccountIdentifierFormatter DEFAULT_FORMAT = new DefaultAccountIdentifierFormatter();
    private static final String EMPTY_STRING = "";

    public static Predicate<BankEntity> filterBankId(String bankId) {
        Preconditions.checkNotNull(bankId, "You must provide a bankId for comparison.");
        return bankEntity -> bankId.equalsIgnoreCase(bankEntity.getBankId());
    }

    public static Predicate<UpcomingTransactionEntity> filterAccounts(String accountNumber) {
        Preconditions.checkNotNull(accountNumber, "You must provider a accountNumber for comparison");
        return upcomingTransactionEntity ->
                Objects.equals(accountNumber, upcomingTransactionEntity.getFromAccount().getFullyFormattedNumber());
    }

    public static Predicate<ConfirmedTransactionsEntity> filterSourceAccount(Transfer originalTransfer) {
        return cte -> originalTransfer.getSource().getIdentifier(DEFAULT_FORMAT).equalsIgnoreCase(
                        Optional.ofNullable(cte.getFromAccount())
                                .map(FromAccountEntity::generalGetAccountIdentifier)
                                .map(identifier -> identifier.getIdentifier(DEFAULT_FORMAT))
                                .orElse(EMPTY_STRING));
    }

    public static final Predicate<ConfirmedTransactionEntity> FILTER_PAYMENTS =
            cte -> "payment".equalsIgnoreCase(cte.getType());

    public static Predicate<ConfirmedTransactionEntity> filterByDate(Transfer originalTransfer) {
        return confirmedTransactionEntity -> {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return dateFormat.format(originalTransfer.getDueDate())
                    .equalsIgnoreCase(dateFormat.format(confirmedTransactionEntity.getDate()));
        };
    }

    public static Predicate<ConfirmedTransactionEntity> filterByAmount(Transfer originalTransfer, String currency) {
        return confirmedTransactionEntity -> {
            Amount originalAmount = originalTransfer.getAmount();
            Amount transferAmount = new Amount(currency,
                    StringUtils.parseAmountEU(confirmedTransactionEntity.getAmount()));
            return originalAmount.equals(transferAmount);
        };
    }

    public static Predicate<ConfirmedTransactionEntity> filterByMessage(Transfer originalTransfer) {
        return confirmedTransactionEntity -> {
            String destinationMessage = originalTransfer.getDestinationMessage();

            return Optional.ofNullable(confirmedTransactionEntity.getPayment())
                    .map(PaymentEntity::getReference)
                    .map(ReferenceEntity::getValue)
                    .map(destinationMessage::equalsIgnoreCase)
                    .isPresent();
        };
    }

    public static Predicate<ConfirmedTransactionEntity> filterByDestinationAccount(Transfer originalTransfer) {
        return confirmedTransactionEntity -> Optional.ofNullable(confirmedTransactionEntity.getPayment())
                .map(PaymentEntity::getPayee)
                .map(PayeeEntity::generalGetAccountIdentifier)
                .filter(accountIdentifier -> originalTransfer.getDestination().getIdentifier(DEFAULT_FORMAT)
                        .equalsIgnoreCase(accountIdentifier.getIdentifier(DEFAULT_FORMAT)))
                .isPresent();
    }
}
