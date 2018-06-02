package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;

public class CoordinationHealthCheck extends HealthCheck {

    private static final TimeUnit LOCK_TIMEOUT_UNIT = TimeUnit.SECONDS;
    private static final int LOCK_TIMEOUT = 10;
    private static final String LOCK_PREFIX = "/locks/healthcheck/";
    
    
    private final CuratorFramework coordinationClient;
    
    public CoordinationHealthCheck(CuratorFramework coordinationClient) {
        this.coordinationClient = coordinationClient;
    }
    
    @Override
    protected Result check() throws Exception {

        String lockPath = LOCK_PREFIX + new Random().nextInt(255);
        InterProcessSemaphoreMutex lock = new InterProcessSemaphoreMutex(coordinationClient, lockPath);
        
        try {
            if (!lock.acquire(LOCK_TIMEOUT, LOCK_TIMEOUT_UNIT)) {
                throw new Exception();
            }
        } catch (Exception e) {
            return Result.unhealthy("Unable to acquire lock");
        } finally {
            if (lock.isAcquiredInThisProcess()) {
                try {
                    lock.release();
                    return Result.healthy();
                } catch (Exception e) {
                    return Result.unhealthy("Unable to release lock");
                }
            }
        }
        
        return Result.unhealthy("This should never happen");
    }
}
