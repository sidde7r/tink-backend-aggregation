package se.tink.agent.sdk.authentication.authenticators.thirdparty_app;

import javax.annotation.Nullable;
import se.tink.agent.sdk.storage.Reference;

public interface ThirdPartyAppPollStatus {
    ThirdPartyAppResult pollThirdPartyAppStatus(@Nullable Reference reference);
}
