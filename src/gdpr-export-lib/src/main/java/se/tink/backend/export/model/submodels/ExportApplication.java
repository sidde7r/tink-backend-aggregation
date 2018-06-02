package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportApplication implements DefaultSetter {

    private final String type;
    private final String status;
    private final String created;
    private final String updated;

    public ExportApplication(String type, String status, Date created, Date updated) {
        this.type = type;
        this.status = status;
        this.created = notNull(created);
        this.updated = notNull(updated);
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getCreated() {
        return created;
    }

    public String getUpdated() {
        return updated;
    }
}
