package se.tink.backend.aggregation.agents.agentfactory.impl;

import com.google.inject.Inject;
import java.lang.reflect.Constructor;

public final class AgentFactoryUtils {

    private AgentFactoryUtils() {
        throw new AssertionError();
    }

    public static boolean hasInjectAnnotatedConstructor(Class cls) {

        Constructor[] constructors = cls.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getAnnotation(Inject.class) != null) {
                return true;
            }
        }

        return false;
    }
}
