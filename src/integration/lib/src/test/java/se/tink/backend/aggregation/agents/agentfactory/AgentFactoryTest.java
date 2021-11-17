package se.tink.backend.aggregation.agents.agentfactory;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentFactoryUtils;
import se.tink.backend.aggregation.agents.agentfactory.testclasses.ClassWithInjectAnnotation;
import se.tink.backend.aggregation.agents.agentfactory.testclasses.ClassWithNoInjectAnnotation;

public final class AgentFactoryTest {

    @Test
    public void ensureHasInjectAnnotatedConstructorReturnsTrueIfAnnotatedConstructorExist() {

        // Given
        Class classWithInjectAnnotation = ClassWithInjectAnnotation.class;

        // When
        boolean result = AgentFactoryUtils.hasInjectAnnotatedConstructor(classWithInjectAnnotation);

        // Then
        Assert.assertTrue(result);
    }

    @Test
    public void ensureHasInjectAnnotatedConstructorReturnsFalseIfAnnotatedConstructorExist() {

        // Given
        Class classWithNoInjectAnnotation = ClassWithNoInjectAnnotation.class;

        // When
        boolean result =
                AgentFactoryUtils.hasInjectAnnotatedConstructor(classWithNoInjectAnnotation);

        // Then
        Assert.assertFalse(result);
    }
}
