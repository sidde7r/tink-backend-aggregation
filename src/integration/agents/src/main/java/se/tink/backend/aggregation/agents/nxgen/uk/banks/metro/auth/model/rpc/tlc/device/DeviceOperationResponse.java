package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ResponseDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class DeviceOperationResponse {
    protected ResponseDataEntity data;

    private List<Map<String, String>> headers;

    @JsonIgnore
    public String getChallenge() {
        return data.getChallenge();
    }

    @JsonIgnore
    public String getHeader(String key) {
        return this.headers.stream()
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> entry.getKey().equals(key))
                .findFirst()
                .map(Entry::getValue)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "Could not find header with key `%s`!", key)));
    }

    public abstract String getAssertionId();
}
