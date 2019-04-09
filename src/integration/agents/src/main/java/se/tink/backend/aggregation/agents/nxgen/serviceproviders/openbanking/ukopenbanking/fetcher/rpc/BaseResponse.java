package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseResponse<T> {

    private T data;

    @JsonProperty("Links")
    private Map<String, String> links;

    @JsonProperty("Meta")
    private Map<String, String> meta;

    protected T getData() {
        return data;
    }

    @JsonProperty("Data")
    private void setData(Map.Entry<String, T> dataWrapper) {
        data = dataWrapper.getValue();
    }

    protected boolean hasLink(String linkId) {
        return links.containsKey(linkId);
    }

    protected String getLink(String linkId) {
        return searchLink(linkId)
                .orElseThrow(() -> new IllegalStateException("No link with id: " + linkId));
    }

    protected Optional<String> searchLink(String linkId) {
        return Optional.ofNullable(links.get(linkId));
    }
}
