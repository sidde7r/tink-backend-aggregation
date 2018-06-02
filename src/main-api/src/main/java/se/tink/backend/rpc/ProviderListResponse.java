package se.tink.backend.rpc;

import io.protostuff.Tag;
import java.util.List;
import se.tink.backend.core.Provider;

public class ProviderListResponse {
    @Tag(1)
    private List<Provider> providers;

    public ProviderListResponse(List<Provider> providers) {
        this.providers = providers;
    }

    public List<Provider> getProviders() {
        return providers;
    }
}
