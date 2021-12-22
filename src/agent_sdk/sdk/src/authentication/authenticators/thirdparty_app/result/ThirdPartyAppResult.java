package se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result;

import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import lombok.Getter;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.builder.ThirdPartyAppResultReferenceBuildStep;
import se.tink.agent.sdk.storage.SerializableReference;

@Getter
public class ThirdPartyAppResult {
    private final ThirdPartyAppStatus status;
    @Nullable private final SerializableReference reference;

    ThirdPartyAppResult(ThirdPartyAppStatus status, @Nullable SerializableReference reference) {
        this.status = status;
        this.reference = reference;
    }

    public static ThirdPartyAppResultReferenceBuildStep builder(ThirdPartyAppStatus status) {
        Preconditions.checkNotNull(status, "ThirdPartyAppResult status cannot be null!");
        return new ThirdPartyAppResultBuilder(status);
    }
}
