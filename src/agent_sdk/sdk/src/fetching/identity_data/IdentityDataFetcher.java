package se.tink.agent.sdk.fetching.identity_data;

import se.tink.agent.sdk.models.identity_data.IdentityData;

public interface IdentityDataFetcher {
    IdentityData fetchIdentityData();
}
