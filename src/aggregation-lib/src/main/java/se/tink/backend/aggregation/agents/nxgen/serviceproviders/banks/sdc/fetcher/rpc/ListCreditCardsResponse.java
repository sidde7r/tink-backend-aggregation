package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCreditCardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListCreditCardsResponse extends ArrayList<SdcCreditCardEntity> {

    @JsonIgnore
    public List<SdcCreditCardEntity> getCreditCards() {
        return stream()
                .filter(SdcCreditCardEntity::isCreditCard)
                .collect(Collectors.toList());
    }
}
