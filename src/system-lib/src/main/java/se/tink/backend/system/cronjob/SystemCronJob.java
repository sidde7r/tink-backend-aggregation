package se.tink.backend.system.cronjob;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.LogUtils;

/**
 * Abstract system job. Public for testability.
 */
public abstract class SystemCronJob implements Job {
    protected static SystemServiceFactory systemServiceFactory;
    private static MetricRegistry metricRegistry;
    private LogUtils log;
    private Timer timer;
    private AtomicBoolean isLeader;

    private static enum CronJobState {
        NOT_RUNNING,
        RUNNING,
    }

    private static final LoadingCache<Class<?>, AtomicReference<CronJobState>> cronjobStates = CacheBuilder
            .newBuilder().build(new CacheLoader<Class<?>, AtomicReference<CronJobState>>() {

                @Override
                public AtomicReference<CronJobState> load(Class<?> key) throws Exception {
                    return new AtomicReference<CronJobState>(CronJobState.NOT_RUNNING);
                }

            });

    static LoadingCache<Class<?>, Timer> cronjobTimers;

    private static class TimerCacheLoader extends CacheLoader<Class<?>, Timer> {

        private MetricRegistry registry;

        public TimerCacheLoader(MetricRegistry registry) {
            this.registry = registry;
        }

        @Override
        public Timer load(Class<?> cls) throws Exception {
            return registry.timer(MetricId.newId("job_duration").label("class", cls.getSimpleName()));
        }
    };

    public static void setSystemServiceFactory(SystemServiceFactory serviceFactory) {
        systemServiceFactory = serviceFactory;
    }

    public static void setMetricRegistry(MetricRegistry registry) {
        metricRegistry = registry;
    }

    public SystemCronJob(Class<?> cls, AtomicBoolean isLeader) {
        cronjobTimers = CacheBuilder.newBuilder().build(new TimerCacheLoader(metricRegistry));
        log = new LogUtils(cls);
        try {
            timer = cronjobTimers.get(cls);
        } catch (ExecutionException e) {
            log.error("Could not instantiate timer.", e);
        }
        this.isLeader = isLeader;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!isLeader.get()) {
            // Note that this check is only executing before starting the job. Leader could get
            // reelected while a Job is executing. This could start an identical job on another
            // system instance.
            log.info("Instance is not leader. Ignoring cronjob.");
            return;
        }
        log.info("Instance is leader. Starting cronjob.");

        AtomicReference<CronJobState> cronjobState = null;
        try {
            cronjobState = cronjobStates.get(this.getClass());
        } catch (ExecutionException e) {
            log.error("Could not get execution state. Not running.", e);
        }

        if (!cronjobState.compareAndSet(CronJobState.NOT_RUNNING, CronJobState.RUNNING)) {
            log.warn("Cronjob already running. Not starting.");
            return;
        }
        Timer.Context timerContext = null;
        if (timer != null) {
            timerContext = timer.time();
        }
        log.info("Starting cronjob.");
        Stopwatch watch = Stopwatch.createStarted();
        try {
            executeIsolated(context);
        } finally {
            cronjobState.set(CronJobState.NOT_RUNNING);
            log.info(String.format("Cronjob done running (%s).", watch.stop()));
            if (timerContext != null) {
                timerContext.stop();
            }
        }
    }

    /**
     * Execute the job fully isolated so that no other concurrent jobs (of the same class) can run.
     */
    public abstract void executeIsolated(JobExecutionContext context);
}
