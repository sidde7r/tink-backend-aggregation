package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ErrorDetailsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionEntity extends AbstractExecutorTransactionEntity {
    private static final Logger log = LoggerFactory.getLogger(TransactionEntity.class);

    private String noteToSender;
    private boolean canSign;
    private boolean counterSigning;
    private List<ErrorDetailsEntity> rejectionCauses;

    public String getNoteToSender() {
        return noteToSender;
    }

    public boolean isCanSign() {
        return canSign;
    }

    public boolean isCounterSigning() {
        return counterSigning;
    }

    @JsonIgnore
    public Optional<TransferExecutionException.EndUserMessage> getMessageBasedOnRejectionCause() {
        if (rejectionCauses == null || rejectionCauses.isEmpty()) {
            return Optional.empty();
        }

        if (rejectionCauses.size() > 1) {
            log.warn(
                    "Received multiple rejection causes for transfer which is not expected, consult debug logs to investigate.");
            return Optional.empty();
        }

        ErrorDetailsEntity errorDetails = rejectionCauses.get(0);

        switch (errorDetails.getCode()) {
            case SwedbankBaseConstants.ErrorCode.INSUFFICIENT_FUNDS:
                return Optional.of(TransferExecutionException.EndUserMessage.EXCESS_AMOUNT);
            case SwedbankBaseConstants.ErrorCode.DUPLICATION:
                return Optional.of(TransferExecutionException.EndUserMessage.DUPLICATE_PAYMENT);
            default:
                log.warn(
                        "Unknown transfer rejection cause. Code: {}, message {}",
                        errorDetails.getCode(),
                        errorDetails.getMessage());
                return Optional.empty();
        }
    }
}
