package se.tink.agent.sdk.authentication.authenticators.thirdparty_app;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;
import se.tink.agent.sdk.storage.SerializableReference;

@Builder
@Getter
public class ThirdPartyAppResult {
    private final ThirdPartyAppStatus status;
    @Nullable private final SerializableReference reference;
}
