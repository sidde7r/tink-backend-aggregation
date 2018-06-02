package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportConsent implements DefaultSetter {
    private final String version;
    private final String action;
    private final String locale;
    private final String timestamp;

    public ExportConsent(String version, String action, String locale,
            Date timestamp) {
        this.version = version;
        this.action = action;
        this.locale = locale;
        this.timestamp = notNull(timestamp);
    }


    public String getVersion() {
        return version;
    }

    public String getAction() {
        return action;
    }

    public String getLocale() {
        return locale;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
