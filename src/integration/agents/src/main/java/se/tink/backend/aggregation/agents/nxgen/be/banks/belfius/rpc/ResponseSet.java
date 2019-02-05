package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc.TechnicalResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResponseSet {

    private int requestCounter;
    private String applicationId;

    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.WRAPPER_OBJECT, defaultImpl = Void.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value=SessionOpenedResponse.class, name= BelfiusConstants.Response.SESSION_OPENED),
            @JsonSubTypes.Type(value=ScreenUpdateResponse.class, name= BelfiusConstants.Response.SCREEN_UPDATE),
            @JsonSubTypes.Type(value=ExecuteMethodResponse.class, name= BelfiusConstants.Response.EXECUTE_METHOD_RESPONSE),
            @JsonSubTypes.Type(value=MessageResponse.class, name= BelfiusConstants.Response.MESSAGE_RESPONSE),
            @JsonSubTypes.Type(value=TechnicalResponse.class, name= BelfiusConstants.Response.TECHNICAL_RESPONSE)
    })
    private List<ResponseEntity> responses;

    public int getRequestCounter() {
        return this.requestCounter;
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    public List<ResponseEntity> getResponses() {
        return this.responses != null ? this.responses : Collections.emptyList();
    }
}
