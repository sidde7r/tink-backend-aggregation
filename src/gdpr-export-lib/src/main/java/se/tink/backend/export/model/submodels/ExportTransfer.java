package se.tink.backend.export.model.submodels;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportTransfer implements DefaultSetter {
    private final String type;
    private final Double exactAmount;
    private final String currency;
    private final String destination;
    private final String destinationMessage;
    private final String source;
    private final String sourceMessage;
    private final String remoteIpAddress;
    private final String status;
    private final String created;
    private final String updated;

    public ExportTransfer(String type, Double exactAmount, String currency,
            String destination, String destinationMessage,
            String source, String sourceMessage, String remoteIpAddress, String status,
            Date created, Date updated) {
        this.type = type;
        this.exactAmount = exactAmount;
        this.currency = currency;
        this.destination = destination;
        this.destinationMessage = destinationMessage;
        this.source = source;
        this.sourceMessage = sourceMessage;
        this.remoteIpAddress = remoteIpAddress;
        this.status = status;
        this.created = notNull(created);
        this.updated = notNull(updated);
    }

    public String getType() {
        return type;
    }

    public Double getExactAmount() {
        return exactAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDestination() {
        return destination;
    }

    public String getDestinationMessage() {
        return destinationMessage;
    }

    public String getSource() {
        return source;
    }

    public String getSourceMessage() {
        return sourceMessage;
    }

    public String getRemoteIpAddress() {
        return remoteIpAddress;
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

    private <T> String mapToString(Map<T, String> map) {
        return map.entrySet().stream().map(e -> e.getKey().toString() + " " + e.getValue())
                .collect(Collectors.joining(", "));
    }
}
