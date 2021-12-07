package se.tink.agent.runtime.test.runtime;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.agent.runtime.AgentRuntime;

@Ignore
public class AgentRuntimeTest {

    @Test
    public void testRuntime() throws Exception {
        ImmutableMap<String, Class<?>> abc = ImmutableMap.of("test", TestAgent.class);
        AgentRuntime agentRuntime = new AgentRuntime(abc);

        agentRuntime.newInstance("test");

        System.out.println("HELLO! " + agentRuntime.getAgentIds());
    }

    @Ignore
    public static class TestAgent {

        @Inject
        public TestAgent() {
            System.out.println("Hello world!");
        }
    }
}
