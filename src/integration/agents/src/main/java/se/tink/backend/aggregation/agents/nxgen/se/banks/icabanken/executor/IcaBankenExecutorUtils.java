package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities.TransferBankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities.UpcomingTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.RecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.utils.IcaBankenFormatUtils;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.backend.aggregation.utils.accountidentifier.IntraBankChecker;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.libraries.transfer.rpc.Transfer;

/** The logic in this util class is directly ported from the old ICABanken agent. */
public class IcaBankenExecutorUtils {

    public static Optional<AccountEntity> tryFindOwnAccount(
            final AccountIdentifier accountIdentifier, Collection<AccountEntity> accounts) {

        return Optional.ofNullable(accounts).orElseGet(Collections::emptyList).stream()
                .filter(
                        accountEntity ->
                                (accountIdentifier
                                        .getIdentifier(
                                                IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER)
                                        .equals(accountEntity.getUnformattedAccountNumber())))
                .findFirst();
    }

    static Optional<RecipientEntity> tryFindRegisteredTransferAccount(
            final AccountIdentifier accountIdentifier, List<RecipientEntity> recipientAccounts) {

        return Optional.ofNullable(recipientAccounts).orElseGet(Collections::emptyList).stream()
                .filter(
                        recipientEntity ->
                                (accountIdentifier
                                        .getIdentifier(
                                                IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER)
                                        .equals(recipientEntity.getUnformattedAccountNumber())))
                .findFirst();
    }

    static Optional<RecipientEntity> tryFindRegisteredPaymentAccount(
            final AccountIdentifier accountIdentifier, List<RecipientEntity> recipientAccounts) {

        return Optional.ofNullable(recipientAccounts).orElseGet(Collections::emptyList).stream()
                .filter(
                        recipientEntity ->
                                (accountIdentifier
                                        .getIdentifier(
                                                IcaBankenFormatUtils.ACCOUNT_IDENTIFIER_FORMATTER)
                                        .equals(recipientEntity.getAccountNumber())))
                .findFirst();
    }

    public static Optional<TransferBankEntity> findBankForAccountNumber(
            String destinationAccount, List<TransferBankEntity> transferBanks) {

        if (transferBanks == null || transferBanks.isEmpty()) {
            return Optional.empty();
        }

        ImmutableMap<Integer, TransferBankEntity> banksByClearingNumber =
                Maps.uniqueIndex(
                        transferBanks,
                        transferBankEntity ->
                                Integer.parseInt(transferBankEntity.getTransferBankId()));

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

    static boolean isMatchingTransfers(
            Transfer transfer, UpcomingTransactionEntity upcomingTransaction) {
        return !(transfer == null || upcomingTransaction == null)
                && transfer.getHash().equalsIgnoreCase(upcomingTransaction.getHash(false));
    }

    public static String getDueDate(Transfer transfer) {
        CountryDateHelper dateHelper =
                new CountryDateHelper(
                        IcaBankenConstants.Date.DEFAULT_LOCALE,
                        TimeZone.getTimeZone(IcaBankenConstants.Date.DEFAULT_ZONE_ID));
        if (transfer.getType().equals(TransferType.PAYMENT)) {
            return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                    dateHelper.getProvidedDateOrBestPossibleDate(transfer.getDueDate(), 9, 30));
        } else {
            if (IntraBankChecker.isSwedishMarketIntraBank(
                    transfer.getSource(), transfer.getDestination())) {
                return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                        dateHelper.getProvidedDateOrCurrentDate(transfer.getDueDate()));
            } else {
                return ThreadSafeDateFormat.FORMATTER_DAILY.format(
                        dateHelper.getProvidedDateOrBestPossibleDate(
                                transfer.getDueDate(), 13, 00));
            }
        }
    }

    public static String getRecipientType(AccountIdentifierType type, Catalog catalog) {
        if (Objects.equals(type, AccountIdentifierType.SE)) {
            return IcaBankenConstants.Transfers.BANK_TRANSFER;
        } else if (Objects.equals(type, AccountIdentifierType.SE_BG)) {
            return IcaBankenConstants.Transfers.PAYMENT_BG;
        } else if (Objects.equals(type, AccountIdentifierType.SE_PG)) {
            return IcaBankenConstants.Transfers.PAYMENT_PG;
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(catalog.getString("Unsupported transfer type"))
                .build();
    }

    public static AccountIdentifierType paymentTypeToIdentifierType(String type) {
        switch (type.toLowerCase()) {
            case IcaBankenConstants.AccountTypes.PAYMENT_BG:
                return AccountIdentifierType.SE_BG;
            case IcaBankenConstants.AccountTypes.PAYMENT_PG:
                return AccountIdentifierType.SE_PG;
            default:
                throw new IllegalArgumentException(
                        String.format("Unused payment type identifier: %s", type));
        }
    }

    /** ICA Banken uses numbers to identify reference type: 1 = OCR, 2 = Message */
    public static void validateAndSetRemittanceInformationType(Transfer transfer) {
        GiroMessageValidator giroValidator =
                GiroMessageValidator.create(OcrValidationConfiguration.softOcr());
        Optional<String> validOcr =
                giroValidator
                        .validate(transfer.getRemittanceInformation().getValue())
                        .getValidOcr();

        if (validOcr.isPresent()) {
            transfer.getRemittanceInformation().setType(RemittanceInformationType.OCR);
        } else {
            transfer.getRemittanceInformation().setType(RemittanceInformationType.UNSTRUCTURED);
        }
    }

    public static String getTruncatedSourceMessage(Transfer transfer) {
        preconditionIsPayment(transfer);

        String sourceMessage = transfer.getSourceMessage();
        if (sourceMessage != null
                && sourceMessage.length() > IcaBankenConstants.Transfers.SOURCE_MSG_MAX_LENGTH) {
            return sourceMessage.substring(0, IcaBankenConstants.Transfers.SOURCE_MSG_MAX_LENGTH);
        }
        return sourceMessage;
    }

    private static void preconditionIsPayment(Transfer transfer) {
        if (!transfer.isOfType(TransferType.PAYMENT)) {
            throw new IllegalArgumentException(
                    "This method should only be used for transfers of type PAYMENT. Use TransferMessageFormatter for BANK_TRANSFER");
        }
    }
}
