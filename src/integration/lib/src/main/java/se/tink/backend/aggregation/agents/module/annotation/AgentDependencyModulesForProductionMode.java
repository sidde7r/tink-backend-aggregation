package se.tink.backend.aggregation.agents.module.annotation;

import com.google.inject.Module;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to load additional dependencies for the agent in the form of guice modules for the
 * production mode. Supports loading one or more modules. <br>
 * <br>
 * Usage: <br>
 * <code>
 *     <br>@AgentDependencyModulesForProductionMode(modules = MyModule.class)
 *     <br>class MyAgent
 * </code>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AgentDependencyModulesForProductionMode {
    Class<? extends Module>[] modules();
}
