package se.tink.backend.common.scheduler;

import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;
import se.tink.backend.common.config.SchedulerConfiguration;

public class SchedulerFactory {
    @SuppressWarnings("unused")
    private SchedulerConfiguration configuration;

    public SchedulerFactory(SchedulerConfiguration configuration) {
        this.configuration = configuration;
    }

    public Scheduler build() {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

            scheduler.getListenerManager().addJobListener(new LoggingJobListener());
            scheduler.getListenerManager().addTriggerListener(new LoggingTriggerListener());

            scheduler.start();

            return scheduler;
        } catch (Exception e) {
            return null;
        }
    }
}
