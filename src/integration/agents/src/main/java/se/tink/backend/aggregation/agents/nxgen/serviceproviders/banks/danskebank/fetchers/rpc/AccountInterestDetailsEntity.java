package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.Collections;
import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class AccountInterestDetailsEntity {
    private List<InterestDetailEntity> interestDetails = Collections.emptyList();
    private String interestIntervalType;
    private String bodyText;
    private String headline;
}
