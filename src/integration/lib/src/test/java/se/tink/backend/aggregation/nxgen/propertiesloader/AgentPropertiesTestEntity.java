package se.tink.backend.aggregation.nxgen.propertiesloader;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import org.junit.Ignore;

@Ignore
@Getter
public class AgentPropertiesTestEntity {

    private String apiVersion;
    private String env;
    private List<PropertiesUrlEntity> urlList;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime certainDate;

    @Getter
    private static class PropertiesUrlEntity {
        private String serverUrl;
        private int port;
    }
}
