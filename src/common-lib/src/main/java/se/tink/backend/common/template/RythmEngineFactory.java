package se.tink.backend.common.template;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.File;
import org.rythmengine.Rythm;
import org.rythmengine.RythmEngine;
import se.tink.backend.common.utils.LogUtils;

public class RythmEngineFactory {

    private static final LogUtils log = new LogUtils(RythmEngineFactory.class);
    private final ImmutableMap<String, Object> buildConfiguration;

    @Inject
    public RythmEngineFactory(@Named("developmentMode") boolean developmentMode) {
        String userDir = System.getProperty("user.dir");
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "__rythm_" + System.getProperty("user.name"));
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        buildConfiguration = ImmutableMap.<String, Object>builder()
                .put("rythm.engine.mode", developmentMode ? Rythm.Mode.dev : Rythm.Mode.prod)
                .put("home.template", userDir + "/data/templates")
                .put("home.tmp.dir", tempDir)
                .build();

        log.info(String.format("Rythm options: %s", buildConfiguration));
    }

    public RythmEngine build() {
        return new RythmEngine(buildConfiguration);
    }

}
