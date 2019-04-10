package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v3.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GroupHeaderEntity {
    @JsonProperty("message_identification")
    private String messageIdentification;

    @JsonProperty("creation_date_time")
    private String creationDateTime;

    @JsonProperty("http_code")
    private int httpCode;

    @JsonProperty("message_pagination")
    private MessagePaginationEntity messagePagination;

    public String getMessageIdentification() {
        return messageIdentification;
    }

    public String getCreationDateTime() {
        return creationDateTime;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public MessagePaginationEntity getMessagePagination() {
        return messagePagination;
    }
}
