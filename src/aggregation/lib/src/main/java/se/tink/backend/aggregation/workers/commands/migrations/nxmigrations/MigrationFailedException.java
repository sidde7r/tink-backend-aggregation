package se.tink.backend.aggregation.workers.commands.migrations.nxmigrations;

public class MigrationFailedException extends Exception {

    public MigrationFailedException() {}

    public MigrationFailedException(String message) {
        super(message);
    }

    public MigrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MigrationFailedException(Throwable cause) {
        super(cause);
    }
}
