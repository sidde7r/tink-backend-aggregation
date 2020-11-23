package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class BaseV31Response<T> {

    private T data;

    private Map<String, String> links;

    private Map<String, String> meta;

    public Optional<T> getData() {
        return Optional.ofNullable(data);
    }

    @JsonProperty("Data")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private void setData(Map<String, T> dataWrapper) {
        data = dataWrapper.entrySet().stream().findAny().map(Map.Entry::getValue).orElse(null);
    }

    protected String getLink(String linkId) {
        return searchLink(linkId)
                .orElseThrow(() -> new IllegalStateException("No link with id: " + linkId));
    }

    protected Optional<String> searchLink(String linkId) {
        return Optional.ofNullable(links).map(links -> links.get(linkId));
    }
}
