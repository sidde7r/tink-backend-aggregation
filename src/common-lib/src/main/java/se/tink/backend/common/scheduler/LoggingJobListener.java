package se.tink.backend.common.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import se.tink.backend.utils.LogUtils;

public class LoggingJobListener implements JobListener {
    private static final LogUtils log = new LogUtils(LoggingJobListener.class);

    @Override
    public String getName() {
        return "LoggingJobListener";
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        log.info("Job to be executed: " + jobExecutionContext.getJobDetail().getKey().getName());
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        log.info("Job execution vetoed: " + jobExecutionContext.getJobDetail().getKey().getName());
    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        log.info("Job was executed: " + jobExecutionContext.getJobDetail().getKey().getName());
    }
}
