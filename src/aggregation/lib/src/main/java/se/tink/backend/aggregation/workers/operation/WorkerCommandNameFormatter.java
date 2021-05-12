package se.tink.backend.aggregation.workers.operation;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WorkerCommandNameFormatter {

    public static String getCommandName(Class<? extends AgentWorkerCommand> clazz) {
        try {
            return formatCommandName(clazz.getSimpleName());
        } catch (RuntimeException e) {
            log.error("unknown error when generating command name", e);
            return "";
        }
    }

    private static String formatCommandName(String name) {
        String commandName =
                Stream.of("AgentWorkerCommand", "WorkerCommand", "Command")
                        .reduce(name, (className, label) -> className.replace(label, ""));
        return camelToKebabCase(commandName);
    }

    private static String camelToKebabCase(String str) {
        return str.replaceAll("([a-z])([A-Z]+)", "$1-$2").toLowerCase();
    }
}
