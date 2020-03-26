package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc;

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.MetaDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseResponse {
    private ErrorEntity error;
    private MetaDataEntity metaData;

    public ErrorEntity getError() {
        return error;
    }

    public MetaDataEntity getMetaData() {
        return Optional.ofNullable(metaData)
                .orElseThrow(() -> new NoSuchElementException("Expected metadata but it was null"));
    }
}
