package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MessagesDataEntity {
    private int numberOfUnreadMessages;

    public int getNumberOfUnreadMessages() {
        return numberOfUnreadMessages;
    }
}
