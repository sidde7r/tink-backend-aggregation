package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultEntity<T> {
    private T data;
    private List<T> items;

    public T getData() {
        return Optional.ofNullable(data)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Expected data to be present but it was null"));
    }

    public List<T> getItems() {
        return Optional.ofNullable(items)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Expected items to be present but it was null"));
    }
}
