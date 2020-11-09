package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.data.entity.CardAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class CardAccountResponse {

    private List<CardAccountEntity> cardAccounts = Collections.emptyList();
}
