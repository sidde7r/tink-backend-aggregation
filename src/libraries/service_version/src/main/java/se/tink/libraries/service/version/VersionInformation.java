package se.tink.libraries.service.version;

import com.google.api.client.util.Charsets;
import com.google.common.io.Files;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class VersionInformation {

    private Version version;

    @SuppressWarnings("unused")
    public static class Version {
        private String version;
        private String commit;
        private Date date;

        private Version() {
            // Needed for JSON deserialization
        }

        private Version(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }

        public String getCommit() {
            return commit;
        }

        public Date getDate() {
            return date;
        }
    }

    @Inject
    public VersionInformation(MetricRegistry registry) throws IOException {
        version =
                SerializationUtils.deserializeFromString(
                        Files.toString(new File("data/version.json"), Charsets.UTF_8),
                        Version.class);

        // Create a counter that is always 1 with the labels we want to export
        MetricId metric = MetricId.newId("version").label("version", version.getVersion());
        if (version.getCommit() != null) {
            metric = metric.label("commit", version.getCommit());
        }
        if (version.getDate() != null) {
            // A common standard format with TZ is RFC3339. That's the format
            // used below.
            String date =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(version.getDate());
            metric = metric.label("date", date);
        }
        registry.meter(metric).inc();
    }

    public Version getVersion() {
        return version;
    }
}
