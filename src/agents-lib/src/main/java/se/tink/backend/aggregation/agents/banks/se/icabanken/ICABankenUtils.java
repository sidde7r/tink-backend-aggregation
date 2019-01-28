package se.tink.backend.aggregation.agents.banks.se.icabanken;

import java.util.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.banks.se.icabanken.model.BankEntity;
import se.tink.libraries.i18n.Catalog;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.transfer.enums.TransferType;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;

public class ICABankenUtils {
    static Optional<BankEntity> findBankForAccountNumber(String destinationAccount, List<BankEntity> banks) {
        ImmutableMap<Integer, BankEntity> banksByClearingNumber = Maps.uniqueIndex(banks,
                be -> Integer.parseInt(be.getTransferBankId()));

        ArrayList<Integer> clearingNumbers = Lists.newArrayList(banksByClearingNumber.keySet());
        Collections.sort(clearingNumbers);

        Integer accountClearingNumber = Integer.parseInt(destinationAccount.substring(0, 4));
        Integer bankClearingNumber = null;

        for (int i = 0; i < clearingNumbers.size(); i++) {
            if (clearingNumbers.get(i) > accountClearingNumber)
            {
                bankClearingNumber = clearingNumbers.get(i - 1);
                break;
            }
        }

        if (bankClearingNumber == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(banksByClearingNumber.get(bankClearingNumber));
    }

    static String findOrCreateDueDateFor(Transfer transfer) {
        if (transfer.getType().equals(TransferType.PAYMENT)) {
            return (transfer.getDueDate() != null) ? ThreadSafeDateFormat.FORMATTER_DAILY.format(DateUtils.getCurrentOrNextBusinessDay(transfer.getDueDate()))
                    : ThreadSafeDateFormat.FORMATTER_DAILY.format(DateUtils.getNextBusinessDay(new Date()));
        } else {
            return (transfer.getDueDate() != null) ? ThreadSafeDateFormat.FORMATTER_DAILY.format(transfer.getDueDate())
                    : ThreadSafeDateFormat.FORMATTER_DAILY.format(new Date());
        }
    }

    static String identifierTypeToString(AccountIdentifier.Type type, Catalog catalog) {
        if (Objects.equals(type, AccountIdentifier.Type.SE)) {
            return "Transfer";
        } else if (Objects.equals(type, AccountIdentifier.Type.SE_BG)) {
            return "PaymentBg";
        } else if (Objects.equals(type, AccountIdentifier.Type.SE_PG)) {
            return "PaymentPg";
        }

        throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(catalog.getString("Unsupported transfer type"))
                .build();
    }

    public static AccountIdentifier.Type paymentTypeToIdentifierType(String type) {
        switch (type) {
        case "PaymentBg":
            return AccountIdentifier.Type.SE_BG;
        case "PaymentPg":
            return AccountIdentifier.Type.SE_PG;
        default:
            throw new IllegalArgumentException(String.format("Unused payment type identifier: %s", type));
        }
    }

    static String getReferenceTypeFor(Transfer transfer) {
        GiroMessageValidator giroValidator = GiroMessageValidator.create(OcrValidationConfiguration.softOcr());
        Optional<String> validOcr = giroValidator.validate(transfer.getDestinationMessage()).getValidOcr();

        return validOcr.isPresent() ? "Ocr" : "Message";
    }
}
