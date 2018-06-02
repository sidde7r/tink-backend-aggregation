package se.tink.backend.common.template;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.common.config.TemplateConfiguration;
import se.tink.libraries.metrics.MetricRegistry;

public class PooledRythmProxyTest {

    private PooledRythmProxy pooledRythmClient;

    @Before
    public void setUp() throws Exception {
        pooledRythmClient = new PooledRythmProxy(new RythmEngineFactory(true),
                new TemplateConfiguration(),
                new MetricRegistry());
        pooledRythmClient.start();
    }

    @After
    public void tearDown() throws Exception {
        pooledRythmClient.stop();
    }

    @Test(expected = RuntimeException.class)
    public void testError() {
        pooledRythmClient.render(new File("file-does-not-exist.html"), ImmutableMap.of());
    }

}
