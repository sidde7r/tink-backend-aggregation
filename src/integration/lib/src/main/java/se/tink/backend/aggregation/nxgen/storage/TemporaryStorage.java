package se.tink.backend.aggregation.nxgen.storage;

public class TemporaryStorage extends Storage {
    @Override
    public void clear() {
        // do not allow to clear the storage
    }
}
