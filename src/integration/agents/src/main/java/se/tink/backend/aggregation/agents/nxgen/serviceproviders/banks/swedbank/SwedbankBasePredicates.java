package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import com.google.common.base.Preconditions;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc.FromAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.ConfirmedTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.ConfirmedTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.updatepayment.rpc.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.ExternalRecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.transferdestination.rpc.TransferDestinationAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.ReferenceEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.rpc.Transfer;

public class SwedbankBasePredicates {
    private static final AccountIdentifierFormatter DEFAULT_FORMAT =
            new DefaultAccountIdentifierFormatter();
    private static final String EMPTY_STRING = "";

    public static Predicate<BankEntity> filterBankId(String bankId) {
        Preconditions.checkNotNull(bankId, "You must provide a bankId for comparison.");
        return bankEntity -> bankId.equalsIgnoreCase(bankEntity.getBankId());
    }

    public static Predicate<ConfirmedTransactionsEntity> filterSourceAccount(
            Transfer originalTransfer) {
        return cte ->
                originalTransfer
                        .getSource()
                        .getIdentifier(DEFAULT_FORMAT)
                        .equalsIgnoreCase(
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
            return dateFormat
                    .format(originalTransfer.getDueDate())
                    .equalsIgnoreCase(dateFormat.format(confirmedTransactionEntity.getDate()));
        };
    }

    public static Predicate<ConfirmedTransactionEntity> filterByAmount(
            Transfer originalTransfer, String currency) {
        return confirmedTransactionEntity -> {
            Amount originalAmount = originalTransfer.getAmount();
            Amount transferAmount =
                    new Amount(
                            currency,
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

    public static Predicate<ConfirmedTransactionEntity> filterByDestinationAccount(
            Transfer originalTransfer) {
        return confirmedTransactionEntity ->
                Optional.ofNullable(confirmedTransactionEntity.getPayment())
                        .map(PaymentEntity::getPayee)
                        .map(PayeeEntity::generalGetAccountIdentifier)
                        .filter(
                                accountIdentifier ->
                                        originalTransfer
                                                .getDestination()
                                                .getIdentifier(DEFAULT_FORMAT)
                                                .equalsIgnoreCase(
                                                        accountIdentifier.getIdentifier(
                                                                DEFAULT_FORMAT)))
                        .isPresent();
    }

    public static Predicate<ExternalRecipientEntity> filterExternalRecipients(
            AccountIdentifier accountIdentifier) {
        return ere -> {
            AccountIdentifier ereAccountIdentifier = ere.generalGetAccountIdentifier();
            String originalAccountIdentifier = accountIdentifier.getIdentifier(DEFAULT_FORMAT);

            if (ereAccountIdentifier == null || originalAccountIdentifier == null) {
                return false;
            }

            return originalAccountIdentifier.equals(
                    ereAccountIdentifier.getIdentifier(DEFAULT_FORMAT));
        };
    }

    public static Predicate<TransferDestinationAccountEntity> filterTransferDestinationAccounts(
            AccountIdentifier accountIdentifier) {

        return tdae -> {
            AccountIdentifier tdaeAccountIdentifier = tdae.generalGetAccountIdentifier();
            String originalAccountIdentifier = accountIdentifier.getIdentifier(DEFAULT_FORMAT);

            if (tdaeAccountIdentifier == null || originalAccountIdentifier == null) {
                return false;
            }

            return originalAccountIdentifier.equals(
                    tdaeAccountIdentifier.getIdentifier(DEFAULT_FORMAT));
        };
    }

    public static Predicate<PayeeEntity> filterPayees(AccountIdentifier accountIdentifier) {
        return pe -> {
            AccountIdentifier peAccountIdentifier = pe.generalGetAccountIdentifier();
            String originalAccountIdentifier = accountIdentifier.getIdentifier(DEFAULT_FORMAT);

            if (peAccountIdentifier == null || originalAccountIdentifier == null) {
                return false;
            }

            return originalAccountIdentifier.equals(
                    peAccountIdentifier.getIdentifier(DEFAULT_FORMAT));
        };
    }
}
