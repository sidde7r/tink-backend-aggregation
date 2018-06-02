package se.tink.backend.common.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.TriggerListener;
import se.tink.backend.utils.LogUtils;

public class LoggingTriggerListener implements TriggerListener {
    private static final LogUtils log = new LogUtils(LoggingTriggerListener.class);

    @Override
    public String getName() {
        return "LoggingTriggerListener";
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext jobExecutionContext) {
        log.info("Trigger fired: " + jobExecutionContext.getJobDetail().getKey().getName());
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext jobExecutionContext) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        log.info("Trigger misfired: " + trigger.getJobKey().getName());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext jobExecutionContext,
            Trigger.CompletedExecutionInstruction completedExecutionInstruction) {
        log.info("Trigger complete: " + trigger.getJobKey().getName());
    }
}
