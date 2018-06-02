package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportTransaction implements DefaultSetter {
    private final String date;
    private final String originalDate;
    private final String description;
    private final String originalDescription;
    private final String exactAmount;
    private final String exactOriginalAmount;
    private final String merchantName;
    private final String note;
    private final String payload;
    private final String type;
    private final String category;

    public ExportTransaction(
            Date date,
            Date originalDate,
            String description,
            String originalDescription,
            Double exactAmount,
            Double exactOriginalAmount,
            String merchantName,
            String note,
            String payload,
            String type,
            String category) {
        this.date = notNull(date);
        this.originalDate = notNull(originalDate);
        this.description = notNull(description);
        this.originalDescription = notNull(originalDescription);
        this.exactAmount = notNull(exactAmount);
        this.exactOriginalAmount = notNull(exactOriginalAmount);
        this.merchantName = notNull(merchantName);
        this.note = notNull(note);
        this.payload = notNull(payload);
        this.type = notNull(type);
        this.category = notNull(category);
    }

    public String getDate() {
        return date;
    }

    public String getOriginalDate() {
        return originalDate;
    }

    public String getDescription() {
        return description;
    }

    public String getOriginalDescription() {
        return originalDescription;
    }

    public String getExactAmount() {
        return exactAmount;
    }

    public String getExactOriginalAmount() {
        return exactOriginalAmount;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getNote() {
        return note;
    }

    public String getPayload() {
        return payload;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }
}
