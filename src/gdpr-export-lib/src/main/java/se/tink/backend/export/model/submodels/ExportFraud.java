package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportFraud implements DefaultSetter {

    private final String type;
    private final String status;
    private final String detailsContent;
    private final String date;
    private final String updated;

    public ExportFraud(String type, String status, String detailsContent, Date date, Date updated) {
        this.type = type;
        this.status = status;
        this.detailsContent = detailsContent;
        this.date = notNull(date);
        this.updated = notNull(updated);
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getDetailsContent() {
        return detailsContent;
    }

    public String getDate() {
        return date;
    }

    public String getUpdated() {
        return updated;
    }
}
