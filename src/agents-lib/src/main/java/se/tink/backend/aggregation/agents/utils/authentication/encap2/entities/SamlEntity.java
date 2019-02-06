package se.tink.backend.aggregation.agents.utils.authentication.encap2.entities;

import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SamlEntity {
    private int responseType;
    private String responseContent;
    private Object plugin;

    public int getResponseType() {
        return responseType;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public String getSamlObjectAsBase64() {
        return EncodingUtils.encodeAsBase64String(responseContent);
    }
}
