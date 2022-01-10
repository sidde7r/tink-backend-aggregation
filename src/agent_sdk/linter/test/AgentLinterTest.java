package src.agent_sdk.linter.test;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.agent.linter.AgentLinter;

// The tests do not work for jdk8. Cannot currently configure this test to only run using jdk11.
// Run it manually until that is solved.
@Ignore
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
