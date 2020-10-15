package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.ProxyResponseHeaders;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.Base64Deserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public abstract class ProxyResponseMessage<T> {

    private int status;

    private String contentType;

    private ProxyResponseHeaders headers;

    @JsonDeserialize(using = Base64Deserializer.class)
    private T content;
}
