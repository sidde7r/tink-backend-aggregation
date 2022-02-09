package se.tink.agent.runtime.instance;

import com.google.common.base.Preconditions;
import java.util.Optional;
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

    public static AgentInstance createFromInstance(
            Class<?> agentClass, Object agentInstance, Operation operation, Utilities utilities) {
        return new AgentInstance(agentClass, agentInstance, operation, utilities);
    }
}
