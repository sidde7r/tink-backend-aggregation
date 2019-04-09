package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.TransferExecutionException;
import se.tink.backend.aggregation.agents.banks.sbab.util.SBABDateUtils;
import se.tink.backend.aggregation.agents.banks.sbab.util.SBABDestinationAccountIdentifierFormatter;
import se.tink.backend.aggregation.agents.banks.sbab.util.SBABTransferUtils;
import se.tink.backend.aggregation.utils.transfer.TransferMessageFormatter;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.strings.StringUtils;
import se.tink.libraries.transfer.rpc.Transfer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferEntity {
    private static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER =
            new DefaultAccountIdentifierFormatter();
    private static final SBABDestinationAccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER =
            new SBABDestinationAccountIdentifierFormatter();

    private String positiveAmount;
    private double negativeAmount;
    private String date;
    private String sourceMessage;
    private String destinationAccountNumber;
    private String destinationMessage;
    private String fromAccountNumber;
    private String id;

    public double getNegativeAmount() {
        return negativeAmount;
    }

    public void setNegativeAmount(String negativeAmount) {
        String formattedAmount = negativeAmount.toLowerCase().replace("kr", "").replace("sek", "");
        this.negativeAmount = StringUtils.parseAmount(formattedAmount);
        if (Objects.equal(this.negativeAmount, 0)) {
            throw TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                    .setMessage(
                            "The transfer amount was 0, probably due to parsing error. New format? Amount string: "
                                    + negativeAmount)
                    .build();
        }
    }

    public String getPositiveAmount() {
        return positiveAmount;
    }

    public void setPositiveAmount(String positiveAmount) {
        this.positiveAmount = positiveAmount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public void setSourceMessage(String sourceMessage) {
        this.sourceMessage = sourceMessage.trim();
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber.replaceAll("[^\\d]", "");
    }

    public String getDestinationMessage() {
        return destinationMessage;
    }

    public void setDestinationMessage(String destinationMessage) {
        this.destinationMessage = destinationMessage;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber.replaceAll("[^\\d]", "");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object compareObject) {
        if (this == compareObject) {
            return true;
        }

        if (compareObject == null || getClass() != compareObject.getClass()) {
            return false;
        }

        TransferEntity other = (TransferEntity) compareObject;

        return Objects.equal(other.getNegativeAmount(), getNegativeAmount())
                && Objects.equal(other.getDate(), getDate())
                && Objects.equal(other.getDestinationAccountNumber(), getDestinationAccountNumber())
                && Objects.equal(other.getFromAccountNumber(), getFromAccountNumber())
                && Objects.equal(
                        other.getSourceMessage() == null
                                ? null
                                : other.getSourceMessage().toLowerCase(),
                        getSourceMessage() == null ? null : getSourceMessage().toLowerCase())
                && Objects.equal(
                        other.getDestinationMessage() == null
                                ? null
                                : other.getDestinationMessage().toLowerCase(),
                        getDestinationMessage() == null
                                ? null
                                : getDestinationMessage().toLowerCase());
    }

    private static boolean isWithinSBAB(Transfer transfer) {
        boolean isWithinSBAB = false;
        AccountIdentifier destination = transfer.getDestination();

        if (Objects.equal(destination.getType(), AccountIdentifier.Type.SE)) {
            Optional<ClearingNumber.Details> clearingNumber =
                    ClearingNumber.get(destination.to(SwedishIdentifier.class).getClearingNumber());
            isWithinSBAB =
                    clearingNumber.isPresent()
                            && Objects.equal(
                                    clearingNumber.get().getBank(), ClearingNumber.Bank.SBAB);
        }

        return isWithinSBAB;
    }

    public static TransferEntity create(
            Transfer transfer, TransferMessageFormatter.Messages messages) {
        TransferEntity transferEntity = new TransferEntity();

        String date = SBABDateUtils.nextPossibleTransferDate(new Date(), isWithinSBAB(transfer));

        transferEntity.setNegativeAmount(SBABTransferUtils.formatNegativeAmount(transfer));
        transferEntity.setPositiveAmount(SBABTransferUtils.formatAmount(transfer));
        transferEntity.setDate(date);
        transferEntity.setDestinationAccountNumber(
                transfer.getDestination().getIdentifier(ACCOUNT_IDENTIFIER_FORMATTER));
        transferEntity.setDestinationMessage(messages.getDestinationMessage());
        transferEntity.setFromAccountNumber(transfer.getSource().getIdentifier(DEFAULT_FORMATTER));
        transferEntity.setSourceMessage(messages.getSourceMessage());

        return transferEntity;
    }
}
