package se.tink.backend.export.model.submodels;

public class ExportSubscription {

    private final String type;

    public ExportSubscription(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
