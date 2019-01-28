package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor;

import java.util.Optional;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.core.signableoperation.SignableOperationStatuses;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.i18n.LocalizableKey;

public class ExecutorExceptionResolver {
    private final Catalog catalog;

    public ExecutorExceptionResolver(Catalog catalog) {
        this.catalog = catalog;
    }

    public void throwIf(boolean condition, ExceptionMessage exceptionMessage) {
        if (condition) {
            throw asException(exceptionMessage);
        }
    }

    public void throwIf(boolean condition, Messageable messageable) {
        if (condition) {
            throw asException(messageable);
        }
    }

    public TransferExecutionException asException(ExceptionMessage exceptionMessage) {
        return TransferExecutionException.builder(exceptionMessage.getStatus())
                .setEndUserMessage(translateMessage(exceptionMessage::getEndUserMessage))
                .setMessage(translateMessage(exceptionMessage::getUserMessage))
                .build();
    }

    private String translateMessage(Supplier<LocalizableKey> message) {
        return catalog.getString(message.get());
    }

    public TransferExecutionException asException(Messageable messageable) {
        return asException(new ExceptionMessage() {
            private LocalizableKey errorMessage = new LocalizableKey(messageable.getFirstErrorMessage().orElse(
                    Optional.ofNullable(messageable.getStatus())
                            .orElse("Something went wrong.")
            ).replaceAll("\n", ""));

            @Override
            public SignableOperationStatuses getStatus() {
                return toStatus(messageable);
            }

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

    private SignableOperationStatuses toStatus(Messageable messageable) {
        String code = messageable.getCode();
        if (StringUtils.isBlank(code)) {
            return SignableOperationStatuses.FAILED;
        }
        switch (code) {
        case "1010":// "The transfer amount exceeds the available amount on the account"
        case "11041"://  "Payment exceeds the allowed maximum"
        case "6242": // "The payment date is too soon or not a business day"
            return SignableOperationStatuses.CANCELLED;
        default:
            return SignableOperationStatuses.FAILED;
        }
    }

    public interface ExceptionMessage {

        SignableOperationStatuses getStatus();

        LocalizableKey getEndUserMessage();

        LocalizableKey getUserMessage();
    }

    public interface Messageable {
        Optional<String> getFirstErrorMessage();

        String getStatus();

        String getCode();
    }
}
