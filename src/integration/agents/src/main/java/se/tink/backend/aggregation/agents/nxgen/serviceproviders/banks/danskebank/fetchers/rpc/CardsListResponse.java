package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
public class CardsListResponse extends AbstractResponse {
    @Getter private List<CardEntity> cards;
}
