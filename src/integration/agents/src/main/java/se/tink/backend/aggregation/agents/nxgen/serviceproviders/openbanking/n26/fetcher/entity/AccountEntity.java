package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountEntity {

    private String id;
    private String name;
    private String bankId;
    private AccountFeaturesEntity accountFeatures;
    private AccountDetailsEntity accountDetails;
}
