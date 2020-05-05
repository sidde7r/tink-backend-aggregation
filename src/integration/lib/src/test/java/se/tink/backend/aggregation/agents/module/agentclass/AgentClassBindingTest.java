package se.tink.backend.aggregation.agents.module.agentclass;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.module.agentclass.testobject.TestAgentClass;

public final class AgentClassBindingTest {

    @Test
    public void agentClassIsBoundForNonParameterizedClass() {

        // given
        final Class<TestAgentClass> expectedClass = TestAgentClass.class;
        final Injector injector = Guice.createInjector(new AgentClassModule(TestAgentClass.class));

        // when
        Class resultingClass = injector.getInstance(Key.get(Class.class, AgentClass.class));

        // then
        Assert.assertEquals(expectedClass, resultingClass);
    }

    @Test
    public void agentClassIsBoundForParameterizedClass() {

        // given
        final Class<TestAgentClass> expectedClass = TestAgentClass.class;
        final Injector injector = Guice.createInjector(new AgentClassModule(TestAgentClass.class));

        // when
        Class resultingClass =
                injector.getInstance(
                        Key.get(new TypeLiteral<Class<? extends Agent>>() {}, AgentClass.class));

        // then
        Assert.assertEquals(expectedClass, resultingClass);
    }
}
