package se.tink.agent.runtime.instance;

import com.google.common.base.Preconditions;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.agent.sdk.authentication.features.AuthenticateBerlinGroup;
import se.tink.agent.sdk.authentication.features.AuthenticateGeneric;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2DecoupledApp;
import se.tink.agent.sdk.authentication.features.AuthenticateOauth2DecoupledSwedishMobileBankId;
import se.tink.agent.sdk.authentication.features.AuthenticateSwedishMobileBankId;
import se.tink.agent.sdk.authentication.features.AuthenticateThirdPartyApp;
import se.tink.agent.sdk.authentication.features.AuthenticateUsernameAndPassword;
import se.tink.agent.sdk.environment.Operation;
import se.tink.agent.sdk.environment.Utilities;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;

public class AgentInstance {
    private final Class<?> agentClass;
    private final Object instance;
    private final Operation operation;
    private final Utilities utilities;

    private AgentInstance(
            Class<?> agentClass, Object instance, Operation operation, Utilities utilities) {
        this.agentClass = Preconditions.checkNotNull(agentClass, "Agent class cannot be null.");
        this.instance = Preconditions.checkNotNull(instance, "Agent instance cannot be null.");
        this.operation = Preconditions.checkNotNull(operation, "Operation cannot be null.");
        this.utilities = Preconditions.checkNotNull(utilities, "Utilities cannot be null.");
    }

    public Operation getOperation() {
        return this.operation;
    }

    public Utilities getUtilities() {
        return this.utilities;
    }

    public boolean isInstanceOf(Class<?> cls) {
        return cls.isAssignableFrom(this.agentClass);
    }

    public <T> Optional<T> instanceOf(Class<T> cls) {
        if (!this.isInstanceOf(cls)) {
            return Optional.empty();
        }

        // It's not unchecked, the check is the line before.
        @SuppressWarnings("unchecked")
        T tCast = (T) this.instance;
        return Optional.of(tCast);
    }

    public boolean supportsBulkPaymentInitiation() {
        return isInstanceOf(InitiateBulkPaymentGeneric.class);
    }

    public boolean supportsAuthentication() {
        return Stream.of(
                        AuthenticateBerlinGroup.class,
                        AuthenticateGeneric.class,
                        AuthenticateOauth2.class,
                        AuthenticateOauth2DecoupledApp.class,
                        AuthenticateOauth2DecoupledSwedishMobileBankId.class,
                        AuthenticateSwedishMobileBankId.class,
                        AuthenticateThirdPartyApp.class,
                        AuthenticateUsernameAndPassword.class)
                .anyMatch(this::isInstanceOf);
    }

    public static AgentInstance createFromInstance(
            Class<?> agentClass, Object agentInstance, Operation operation, Utilities utilities) {
        return new AgentInstance(agentClass, agentInstance, operation, utilities);
    }
}
