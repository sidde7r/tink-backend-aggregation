package se.tink.agent.runtime;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.ClassPath;
import java.io.IOException;
import java.util.function.Function;

public final class AgentClassResolver {
    private static final String AGENTS_PACKAGE = "se.tink.agent.agents.";

    public static ImmutableMap<String, Class<?>> findAgentClasses() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try {
            return ClassPath.from(classLoader).getTopLevelClasses().stream()
                    .filter(info -> info.getPackageName().startsWith(AGENTS_PACKAGE))
                    .map(ClassPath.ClassInfo::load)
                    .collect(
                            ImmutableMap.toImmutableMap(
                                    AgentClassResolver::packageNameIntoAgentId,
                                    Function.identity()));
        } catch (IOException e) {
            return new ImmutableMap.Builder<String, Class<?>>().build();
        }
    }

    private static String packageNameIntoAgentId(Class<?> agentClass) {
        return agentClass.getName().substring(AGENTS_PACKAGE.length());
    }
}
