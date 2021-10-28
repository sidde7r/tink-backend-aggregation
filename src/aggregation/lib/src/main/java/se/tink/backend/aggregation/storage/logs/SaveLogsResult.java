package se.tink.backend.aggregation.storage.logs;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class SaveLogsResult {

    private final SaveLogsStatus status;
    private final String storageDescription;

    public boolean isSaved() {
        return status == SaveLogsStatus.SAVED;
    }

    public static SaveLogsResult saved(String storageDescription) {
        return SaveLogsResult.builder()
                .status(SaveLogsStatus.SAVED)
                .storageDescription(storageDescription)
                .build();
    }

    public static SaveLogsResult of(SaveLogsStatus status) {
        return SaveLogsResult.builder().status(status).build();
    }
}
