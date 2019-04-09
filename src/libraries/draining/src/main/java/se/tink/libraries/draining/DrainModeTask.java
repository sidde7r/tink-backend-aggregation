package se.tink.libraries.draining;

import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;

public class DrainModeTask extends Task {
    private final ApplicationDrainMode applicationDrainMode;

    @Inject
    public DrainModeTask(ApplicationDrainMode applicationDrainMode) {
        super("drainmode");
        this.applicationDrainMode = applicationDrainMode;
    }

    @Override
    public void execute(
            ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter)
            throws Exception {
        if (immutableMultimap.size() == 1) {
            if (immutableMultimap.containsKey("enable")) {
                applicationDrainMode.enable();
            } else if (immutableMultimap.containsKey("disable")) {
                applicationDrainMode.disable();
            }
        }
        printWriter.println(applicationDrainMode.toString());
    }
}
