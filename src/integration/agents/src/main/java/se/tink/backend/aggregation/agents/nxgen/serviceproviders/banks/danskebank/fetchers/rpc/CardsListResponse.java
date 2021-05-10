package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.List;
import lombok.EqualsAndHashCode;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode(callSuper = false)
public class CardsListResponse extends AbstractResponse {
    private List<CardEntity> cards;

    public List<CardEntity> getCards() {
        return ListUtils.emptyIfNull(cards);
    }
}
