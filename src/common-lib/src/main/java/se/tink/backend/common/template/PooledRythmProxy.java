package se.tink.backend.common.template;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.apache.commons.pool.impl.GenericObjectPoolFactory;
import org.rythmengine.RythmEngine;
import se.tink.backend.common.config.TemplateConfiguration;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Gauge;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

/**
 * Locked down thread safe wrapper around multiple {@link RythmEngine}s.
 */
public class PooledRythmProxy implements Managed {

    private static final LogUtils log = new LogUtils(PooledRythmProxy.class);
    private final MetricId NUM_ACTIVE_METRIC_NAME = MetricId.newId("rythm_pool_active");
    private final MetricId NUM_IDLE_METRIC_NAME = MetricId.newId("rythm_pool_idle");

    private final GenericObjectPoolFactory poolFactory;
    private final TemplateConfiguration configuration;
    private final MetricRegistry metricRegistry;

    private ObjectPool pool;

    @Inject
    public PooledRythmProxy(final RythmEngineFactory rythmEngineFactory,
            TemplateConfiguration configuration,
            MetricRegistry metricRegistry) {
        this.configuration = configuration;
        this.metricRegistry = metricRegistry;

        Config poolConfig = new Config();
        poolConfig.maxActive = configuration.getNumEnginesMaxActive();
        poolConfig.maxIdle = configuration.getNumEnginesMaxActive();
        poolConfig.minIdle = 1;
        poolConfig.numTestsPerEvictionRun = 3;
        poolConfig.timeBetweenEvictionRunsMillis = TimeUnit.MINUTES.toMillis(2);
        poolConfig.minEvictableIdleTimeMillis = TimeUnit.MINUTES.toMillis(10);

        PoolableObjectFactory poolObjectFactory = new PoolableObjectFactory() {

            @Override
            public boolean validateObject(Object obj) {
                return true;
            }

            @Override
            public void passivateObject(Object obj) throws Exception {
                // Deliberately left empty.
            }

            @Override
            public Object makeObject() throws Exception {
                RythmEngine rythmEngine = rythmEngineFactory.build();
                log.debug("Instantiating object.");
                for (Template template : Template.values()) {
                    try {
                        rythmEngine.getTemplate(template.getFilePath());
                    } catch (Exception e) {
                        log.warn("Could not warm up some template. `Template` enum probably needs updating.", e);
                    }
                }
                log.debug("Instantiated object.");

                return rythmEngine;
            }

            @Override
            public void destroyObject(Object obj) throws Exception {
                ((RythmEngine) obj).shutdown();
            }

            @Override
            public void activateObject(Object obj) throws Exception {
                // Deliberately left empty.
            }
        };

        poolFactory = new GenericObjectPoolFactory(poolObjectFactory, poolConfig);
    }

    public String render(Template template, Map<String, Object> params) {
        RythmEngine rythmEngine;
        try {
            rythmEngine = (RythmEngine) pool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException("Could not get a pooled rythm engine.", e);
        }
        try {
            return rythmEngine.render(template.getFilePath(), params);
        } finally {
            try {
                pool.returnObject(rythmEngine);
            } catch (Exception e) {
                log.error("Could not return a borrowed rythm engine instance to pool. Ignoring.", e);
            }
        }
    }

    @PostConstruct
    @Override
    public void start() throws Exception {
        if (configuration.isEnabled()) {
            pool = poolFactory.createPool();

            metricRegistry.registerSingleton(NUM_ACTIVE_METRIC_NAME, new Gauge() {
                @Override
                public double getValue() {
                    return pool.getNumActive();
                }
            });
            metricRegistry.registerSingleton(NUM_IDLE_METRIC_NAME, new Gauge() {
                @Override
                public double getValue() {
                    return pool.getNumIdle();
                }
            });

            if (configuration.isWarmUp()) {
                ArrayList<Object> objects = Lists.newArrayListWithCapacity(configuration.getNumEnginesWarmUp());

                for (int i = 0; i < configuration.getNumEnginesWarmUp(); i++) {
                    objects.add(pool.borrowObject());
                }
                for (Object object : objects) {
                    pool.returnObject(object);
                }
            }
        }
    }

    @PreDestroy
    @Override
    public void stop() throws Exception {
        if (configuration.isEnabled()) {
            metricRegistry.remove(NUM_ACTIVE_METRIC_NAME);
            metricRegistry.remove(NUM_IDLE_METRIC_NAME);

            pool.close();
        }
    }

    public String render(File file, ImmutableMap<String, Object> params) {
        RythmEngine rythmEngine;
        try {
            rythmEngine = (RythmEngine) pool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException("Could not get a pooled rythm engine. Is rythm engine enabled?", e);
        }
        try {
            return rythmEngine.render(file, params);
        } finally {
            try {
                pool.returnObject(rythmEngine);
            } catch (Exception e) {
                log.error("Could not return a borrowed rythm engine instance to pool. Ignoring.", e);
            }
        }
    }
}
