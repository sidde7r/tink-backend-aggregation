package se.tink.agent.sdk.authentication.existing_consent;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.ToString;

@ToString
public class ExistingConsentResponse {
    private final boolean completed;
    @Nullable private final ConsentStatus consentStatus;
    @Nullable private final Class<? extends ExistingConsentStep> nextStep;

    private ExistingConsentResponse(
            boolean completed,
            @Nullable ConsentStatus consentStatus,
            @Nullable Class<? extends ExistingConsentStep> nextStep) {
        this.completed = completed;
        this.consentStatus = consentStatus;
        this.nextStep = nextStep;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Optional<ConsentStatus> getConsentStatus() {
        return Optional.ofNullable(consentStatus);
    }

    public Optional<Class<? extends ExistingConsentStep>> getNextStep() {
        return Optional.ofNullable(nextStep);
    }

    public static ExistingConsentResponse step(Class<? extends ExistingConsentStep> nextStep) {
        Preconditions.checkNotNull(nextStep, "nextStep cannot be null!");
        return new ExistingConsentResponse(false, null, nextStep);
    }

    public static ExistingConsentResponse done(ConsentStatus consentStatus) {
        Preconditions.checkNotNull(consentStatus, "consentStatus cannot be null!");
        return new ExistingConsentResponse(true, consentStatus, null);
    }
}
