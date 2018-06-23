package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities.MessageEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorMessageResponse {
    private List<MessageEntity> messages;

    public List<MessageEntity> getMessages() {
        return messages == null ? Collections.emptyList() : messages;
    }
}
