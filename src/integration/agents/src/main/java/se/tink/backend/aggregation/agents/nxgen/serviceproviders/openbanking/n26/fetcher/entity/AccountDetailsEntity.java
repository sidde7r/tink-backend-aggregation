package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.fetcher.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountDetailsEntity {

    private String identifier;
    private String type;
    private String status;
    private AccountMetadataEntity metadata;
    private ProviderAccountDetailsEntity providerAccountDetails;
}
