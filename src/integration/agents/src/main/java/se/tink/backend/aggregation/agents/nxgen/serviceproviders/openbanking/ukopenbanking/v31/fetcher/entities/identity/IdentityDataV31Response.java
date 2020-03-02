package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.identity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class IdentityDataV31Response {

    private List<IdentityDataV31Entity> data;

    private Map<String, String> links;

    private Map<String, String> meta;

    public Optional<List<IdentityDataV31Entity>> getData() {
        return Optional.ofNullable(data);
    }

    @JsonProperty("Data")
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private void setData(Map<String, List<IdentityDataV31Entity>> dataWrapper) {
        data = dataWrapper.entrySet().stream().findAny().map(Map.Entry::getValue).orElse(null);
    }
}
