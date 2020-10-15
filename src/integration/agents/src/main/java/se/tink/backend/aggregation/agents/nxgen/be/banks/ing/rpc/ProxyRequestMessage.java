package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.ProxyRequestHeaders;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.Base64Serializer;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public abstract class ProxyRequestMessage<T> {

    public ProxyRequestMessage(String path) {
        this.path = path;
        this.method = "GET";
        this.contentType = "application/json";
    }

    public ProxyRequestMessage(
            String path,
            String method,
            ProxyRequestHeaders headers,
            T content,
            String contentType) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.content = content;
        this.contentType = contentType;
    }

    private String path;

    private String method;

    private ProxyRequestHeaders headers;

    @JsonSerialize(using = Base64Serializer.class)
    private T content;

    private String contentType;

    private String query;
}
