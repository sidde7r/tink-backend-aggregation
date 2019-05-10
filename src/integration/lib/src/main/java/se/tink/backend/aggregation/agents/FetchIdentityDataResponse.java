package se.tink.backend.aggregation.agents;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import se.tink.libraries.identitydata.IdentityData;

public final class FetchIdentityDataResponse {
    private final IdentityData identityData;

    public FetchIdentityDataResponse(@Nonnull final IdentityData identityData) {
        this.identityData = Preconditions.checkNotNull(identityData);
    }

    public IdentityData getIdentityData() {
        return identityData;
    }
}
