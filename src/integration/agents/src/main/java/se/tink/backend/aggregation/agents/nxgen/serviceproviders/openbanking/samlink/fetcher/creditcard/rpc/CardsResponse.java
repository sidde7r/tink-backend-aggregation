package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.samlink.fetcher.creditcard.entities.CardEntity;

@Getter
public class CardsResponse {

    private List<CardEntity> cardAccounts;
}
