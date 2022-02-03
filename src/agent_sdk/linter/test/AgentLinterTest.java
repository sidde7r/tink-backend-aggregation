package src.agent_sdk.linter.test;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Test;
import se.tink.agent.linter.AgentLinter;

public class AgentLinterTest {

    @Test
    public void testLinter() {
        CompilationTestHelper compilationHelper =
                CompilationTestHelper.newInstance(AgentLinter.class, getClass());

        compilationHelper
                .addSourceFile("/RandomTestCases.java")
                .addSourceFile("/UuidTestCases.java")
                .addSourceFile("/DateTestCases.java")
                .addSourceFile("/SleepTestCases.java")
                .doTest();
    }
}
