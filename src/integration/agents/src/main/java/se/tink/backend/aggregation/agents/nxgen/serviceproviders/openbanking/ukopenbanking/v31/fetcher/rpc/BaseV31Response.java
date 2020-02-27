package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseV31Response<T> {

    private T data;

    @JsonProperty("Links")
    private Map<String, String> links;

    @JsonProperty("Meta")
    private Map<String, String> meta;

    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    @JsonProperty("Data")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private void setData(Map<String, T> dataWrapper) {
        data = dataWrapper.entrySet().stream().findAny().map(Map.Entry::getValue).orElse(null);
    }

    protected boolean hasLink(String linkId) {
        return links.containsKey(linkId);
    }

    protected String getLink(String linkId) {
        return searchLink(linkId)
                .orElseThrow(() -> new IllegalStateException("No link with id: " + linkId));
    }

    protected Optional<String> searchLink(String linkId) {
        return links == null ? Optional.empty() : Optional.ofNullable(links.get(linkId));
    }
}
