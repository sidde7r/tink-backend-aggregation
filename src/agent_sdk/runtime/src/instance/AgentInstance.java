package se.tink.agent.runtime.instance;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.agent.runtime.environment.AgentEnvironment;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;

public class AgentInstance {
    private final AgentEnvironment environment;
    private final Class<?> agentClass;
    private final Object instance;

    private AgentInstance(AgentEnvironment environment, Class<?> agentClass, Object instance) {
        this.environment =
                Preconditions.checkNotNull(environment, "Agent environment cannot be null.");
        this.agentClass = Preconditions.checkNotNull(agentClass, "Agent class cannot be null.");
        this.instance = Preconditions.checkNotNull(instance, "Agent instance cannot be null.");
    }

    public AgentEnvironment getEnvironment() {
        return environment;
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

    public static AgentInstance createFromInstance(
            AgentEnvironment environment, Class<?> agentClass, Object agentInstance) {
        return new AgentInstance(environment, agentClass, agentInstance);
    }
}
