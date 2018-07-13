package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessagesSummaryEntity {
    private String summaryResultCode;
    private String summaryResult;
    private MessagesDataEntity messagesData;

    public String getSummaryResultCode() {
        return summaryResultCode;
    }

    public String getSummaryResult() {
        return summaryResult;
    }

    public MessagesDataEntity getMessagesData() {
        return messagesData;
    }
}
