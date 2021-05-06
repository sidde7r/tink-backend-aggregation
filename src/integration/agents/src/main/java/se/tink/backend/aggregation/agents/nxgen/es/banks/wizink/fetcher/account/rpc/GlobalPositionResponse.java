package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities.GlobalPositionDto;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities.GlobalPositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities.ProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class GlobalPositionResponse {

    @JsonProperty("GlobalPositionResponse")
    private GlobalPositionEntity globalPositionEntity;

    public List<ProductEntity> getProducts() {
        return Optional.ofNullable(globalPositionEntity)
                .flatMap(GlobalPositionEntity::getGlobalPositionDto)
                .map(GlobalPositionDto::getProducts)
                .orElseGet(Collections::emptyList);
    }
}
