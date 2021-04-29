package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.account.entities;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class GlobalPositionDto {
    private List<ProductEntity> products;
}
