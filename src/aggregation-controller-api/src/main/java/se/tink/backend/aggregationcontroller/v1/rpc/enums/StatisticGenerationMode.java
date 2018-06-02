package se.tink.backend.aggregationcontroller.v1.rpc.enums;

import se.tink.backend.firehose.v1.rpc.FirehoseMessage;

public enum StatisticGenerationMode {
    APPEND(FirehoseMessage.Type.UPDATE), REWRITE(FirehoseMessage.Type.READ);

    private final FirehoseMessage.Type messageType;

    StatisticGenerationMode(FirehoseMessage.Type messageType) {
        this.messageType = messageType;
    }

    public FirehoseMessage.Type getMessageType() {
        return messageType;
    }
}
