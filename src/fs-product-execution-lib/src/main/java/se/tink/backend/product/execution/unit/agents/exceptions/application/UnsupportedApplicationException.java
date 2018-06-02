package se.tink.backend.product.execution.unit.agents.exceptions.application;

import se.tink.libraries.application.ApplicationType;

@SuppressWarnings("serial")
public class UnsupportedApplicationException extends RuntimeException {

    public UnsupportedApplicationException(ApplicationType type) {
        super("Unsupported application type: " + type);
    }
}
