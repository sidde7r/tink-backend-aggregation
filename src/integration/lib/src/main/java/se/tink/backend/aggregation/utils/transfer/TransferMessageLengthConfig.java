package se.tink.backend.aggregation.utils.transfer;

public class TransferMessageLengthConfig {
    private final int betweenSameUserDestinationMessageMaxLength;
    private final int destinationMessageMaxLength;
    private final int sourceMessageMaxLength;

    public static TransferMessageLengthConfig createWithMaxLength(Integer messageMaxLength) {
        return new TransferMessageLengthConfig(messageMaxLength, messageMaxLength, messageMaxLength);
    }

    public static TransferMessageLengthConfig createWithMaxLength(Integer sourceMessageMaxLength, Integer destinationMessageMaxLength) {
        return new TransferMessageLengthConfig(sourceMessageMaxLength, destinationMessageMaxLength,
                destinationMessageMaxLength);
    }

    public static TransferMessageLengthConfig createWithMaxLength(Integer sourceMessageMaxLength,
            Integer destinationMessageMaxLength, Integer betweenSameUserDestinationMessageMaxLength) {
        return new TransferMessageLengthConfig(sourceMessageMaxLength, destinationMessageMaxLength,
                betweenSameUserDestinationMessageMaxLength);
    }

    private TransferMessageLengthConfig(Integer sourceMessageMaxLength, Integer destinationMessageMaxLength,
            Integer betweenSameUserDestinationMessageMaxLength) {
        if (sourceMessageMaxLength == null || destinationMessageMaxLength == null
                || betweenSameUserDestinationMessageMaxLength == null) {
            throw new IllegalArgumentException("Null thresholds are not allowed!");
        }

        this.sourceMessageMaxLength = sourceMessageMaxLength;
        this.destinationMessageMaxLength = destinationMessageMaxLength;
        this.betweenSameUserDestinationMessageMaxLength = betweenSameUserDestinationMessageMaxLength;
    }

    public int getDestinationMessageMaxLength(boolean isTransferBetweenSameUserAccounts) {
        if (isTransferBetweenSameUserAccounts) {
            return betweenSameUserDestinationMessageMaxLength;
        } else {
            return destinationMessageMaxLength;
        }
    }

    public int getSourceMessageMaxLength() {
        return sourceMessageMaxLength;
    }
}
