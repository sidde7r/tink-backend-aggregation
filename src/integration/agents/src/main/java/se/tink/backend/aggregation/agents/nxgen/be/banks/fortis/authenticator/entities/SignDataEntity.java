package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class SignDataEntity {
    private final List<DataSetEntity> dataSets;

    public SignDataEntity(DataSetEntity singleDataSet) {
        this.dataSets = Collections.singletonList(singleDataSet);
    }
}
