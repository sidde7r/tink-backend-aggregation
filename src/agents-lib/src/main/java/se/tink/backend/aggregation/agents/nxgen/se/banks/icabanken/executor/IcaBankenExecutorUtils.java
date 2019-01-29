package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.TransferBankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.utils.IcaBankenFormatUtils;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.i18n.Catalog;

/**
 * The logic in this util class is directly ported from the old ICABanken agent.
 */
public class IcaBankenExecutorUtils {

    public static Optional<AccountEntity> tryFindOwnAccount(final AccountIdentifier accountIdentifier,
            Collection<AccountEntity> accounts) {

        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(accountEntity -> (
                        accountIdentifier.getIdentifier(IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER)
                        .equals(accountEntity.getUnformattedAccountNumber())))
                .findFirst();
    }

    static Optional<RecipientEntity> tryFindRegisteredTransferAccount(final AccountIdentifier accountIdentifier,
            List<RecipientEntity> recipientAccounts) {

        return Optional.ofNullable(recipientAccounts).orElse(Collections.emptyList()).stream()
                .filter(recipientEntity -> (
                        accountIdentifier.getIdentifier(IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER)
                        .equals(recipientEntity.getUnformattedAccountNumber())))
                .findFirst();
    }

    static Optional<RecipientEntity> tryFindRegisteredPaymentAccount(final AccountIdentifier accountIdentifier,
            List<RecipientEntity> recipientAccounts) {

        return Optional.ofNullable(recipientAccounts).orElse(Collections.emptyList()).stream()
                .filter(recipientEntity -> (
                        accountIdentifier.getIdentifier(IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER)
                        .equals(recipientEntity.getAccountNumber())))
                .findFirst();
    }

    public static Optional<TransferBankEntity> findBankForAccountNumber(String destinationAccount,
            List<TransferBankEntity> transferBanks) {

        if (transferBanks == null ||transferBanks.isEmpty()) {
            return Optional.empty();
        }

        ImmutableMap<Integer, TransferBankEntity> banksByClearingNumber = Maps.uniqueIndex(transferBanks,
                transferBankEntity -> Integer.parseInt(transferBankEntity.getTransferBankId()));

        List<Integer> clearingNumbers = new ArrayList<>(banksByClearingNumber.keySet());
        Collections.sort(clearingNumbers);

        Integer accountClearingNumber = Integer.parseInt(destinationAccount.substring(0, 4));

        Integer bankClearingNumber = null;

        for (int i = 0; i < clearingNumbers.size(); i++) {
            if (clearingNumbers.get(i) > accountClearingNumber) {
                bankClearingNumber = clearingNumbers.get(i - 1);
                break;
            }
        }

        if (bankClearingNumber == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(banksByClearingNumber.get(bankClearingNumber));
    }

    static boolean isMatchingTransfers(Transfer transfer, UpcomingTransactionEntity upcomingTransaction) {
        return !(transfer == null || upcomingTransaction == null) &&
                transfer.getHash().equalsIgnoreCase(upcomingTransaction.getHash(false));
    }

    public static String findOrCreateDueDateFor(Transfer transfer) {
        if (transfer.getType().equals(TransferType.PAYMENT)) {
            return (transfer.getDueDate() != null) ?
                    ThreadSafeDateFormat.FORMATTER_DAILY
                            .format(DateUtils.getCurrentOrNextBusinessDay(transfer.getDueDate()))
                    : ThreadSafeDateFormat.FORMATTER_DAILY
                    .format(DateUtils.getNextBusinessDay(new Date()));
        } else {
            return (transfer.getDueDate() != null) ? ThreadSafeDateFormat.FORMATTER_DAILY
                    .format(transfer.getDueDate())
                    : ThreadSafeDateFormat.FORMATTER_DAILY
                    .format(new Date());
        }
    }

    public static String getRecipientType(AccountIdentifier.Type type, Catalog catalog) {
        if (Objects.equals(type, AccountIdentifier.Type.SE)) {
            return IcaBankenConstants.Transfers.BANK_TRANSFER;
        } else if (Objects.equals(type, AccountIdentifier.Type.SE_BG)) {
            return IcaBankenConstants.Transfers.PAYMENT_BG;
        } else if (Objects.equals(type, AccountIdentifier.Type.SE_PG)) {
            return IcaBankenConstants.Transfers.PAYMENT_PG;
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(catalog.getString("Unsupported transfer type"))
                .build();
    }

    public static AccountIdentifier.Type paymentTypeToIdentifierType(String type) {
        switch (type.toLowerCase()) {
        case IcaBankenConstants.AccountTypes.PAYMENT_BG:
            return AccountIdentifier.Type.SE_BG;
        case IcaBankenConstants.AccountTypes.PAYMENT_PG:
            return AccountIdentifier.Type.SE_PG;
        default:
            throw new IllegalArgumentException(String.format("Unused payment type identifier: %s", type));
        }
    }

    /**
     * ICA Banken uses numbers to identify reference type: 1 = OCR, 2 = Message
     */
    public static String getReferenceTypeFor(Transfer transfer) {
        GiroMessageValidator giroValidator = GiroMessageValidator.create(OcrValidationConfiguration.softOcr());
        Optional<String> validOcr = giroValidator.validate(transfer.getDestinationMessage()).getValidOcr();

        return validOcr.isPresent() ? IcaBankenConstants.Transfers.OCR : IcaBankenConstants.Transfers.MESSAGE;
    }

}
