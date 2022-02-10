package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor;

import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

public class ExecutorExceptionResolver {
    private final Catalog catalog;

    public ExecutorExceptionResolver(Catalog catalog) {
        this.catalog = catalog;
    }

    private String translateMessage(Supplier<LocalizableKey> message) {
        return catalog.getString(message.get());
    }

    public TransferExecutionException asException(Messageable messageable) {
        return abortTransfer(
                messageable,
                new ExceptionMessage() {
                    private LocalizableKey errorMessage =
                            new LocalizableKey(
                                    messageable
                                            .getFirstErrorMessage()
                                            .orElse(
                                                    Optional.ofNullable(messageable.getStatus())
                                                            .orElse(
                                                                    TransferExecutionException
                                                                            .EndUserMessage
                                                                            .GENERIC_PAYMENT_ERROR_MESSAGE
                                                                            .getKey()
                                                                            .get()))
                                            .replaceAll("\n", ""));

                    @Override
                    public LocalizableKey getEndUserMessage() {
                        return errorMessage;
                    }

                    @Override
                    public LocalizableKey getUserMessage() {
                        return errorMessage;
                    }
                });
    }

    public TransferExecutionException abortTransfer(
            Messageable messageable, ExceptionMessage exceptionMessage) {
        String code = messageable.getCode();
        if (StringUtils.isBlank(code)) {
            return failTransfer(exceptionMessage);
        }
        switch (code) {
            case "1010": // "The transfer amount exceeds the available amount on the account"
                return cancelTransfer(exceptionMessage, InternalStatus.INSUFFICIENT_FUNDS);
            case "1030":
                return cancelTransfer(exceptionMessage, InternalStatus.INVALID_OCR);
            case "6242": // "The payment date is too soon or not a business day"
            case "1026": // "The payment date is too soon or not a business day"
                return cancelTransfer(exceptionMessage, InternalStatus.INVALID_DUE_DATE);
            case "1014": // "The amount you entered is too large. exceed maximum amount per day"
            case "1011": // "The amount you entered is too large. exceed maximum amount per day"
            case "11041": // "Payment exceeds the allowed maximum"
                return cancelTransfer(exceptionMessage, InternalStatus.INVALID_MAXIMUM_AMOUNT);
            default:
                return failTransfer(exceptionMessage);
        }
    }

    private TransferExecutionException failTransfer(ExceptionMessage exceptionMessage) {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setEndUserMessage(translateMessage(exceptionMessage::getUserMessage))
                .setMessage(translateMessage(exceptionMessage::getUserMessage))
                .build();
    }

    public TransferExecutionException cancelTransfer(
            ExceptionMessage exceptionMessage, InternalStatus internalStatus) {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setEndUserMessage(translateMessage(exceptionMessage::getUserMessage))
                .setMessage(translateMessage(exceptionMessage::getUserMessage))
                .setInternalStatus(internalStatus.toString())
                .build();
    }

    public interface ExceptionMessage {

        LocalizableKey getEndUserMessage();

        LocalizableKey getUserMessage();
    }

    public interface Messageable {
        Optional<String> getFirstErrorMessage();

        String getStatus();

        String getCode();
    }
}
