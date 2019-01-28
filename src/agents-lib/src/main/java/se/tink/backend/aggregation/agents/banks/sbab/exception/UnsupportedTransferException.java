package se.tink.backend.aggregation.agents.banks.sbab.exception;

import se.tink.libraries.enums.TransferType;

public class UnsupportedTransferException extends RuntimeException {

    public UnsupportedTransferException(TransferType type) {
        super("Unsupported transfer type: " + type);
    }
}
