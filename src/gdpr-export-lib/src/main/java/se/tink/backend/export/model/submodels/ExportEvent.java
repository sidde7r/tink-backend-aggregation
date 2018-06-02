package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportEvent implements DefaultSetter {
    private final String date;
    private final String type;
    private final String remoteAddress;

    public ExportEvent(Date date, String type, String remoteAddress) {
        this.date = notNull(date);
        this.type = type;
        this.remoteAddress = remoteAddress;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }
}
