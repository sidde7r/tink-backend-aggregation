package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultEntity {
    private Boolean flushMessages;
    private List<String> messages;
    private String outcome;
    private String requestId;
    private Boolean result;
}
