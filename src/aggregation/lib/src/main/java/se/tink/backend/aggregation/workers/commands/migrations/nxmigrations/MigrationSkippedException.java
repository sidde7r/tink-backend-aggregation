package se.tink.backend.aggregation.workers.commands.migrations.nxmigrations;

public class MigrationSkippedException extends Exception {

    public MigrationSkippedException() {}

    public MigrationSkippedException(String message) {
        super(message);
    }

    public MigrationSkippedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationSkippedException(Throwable cause) {
        super(cause);
    }
}
