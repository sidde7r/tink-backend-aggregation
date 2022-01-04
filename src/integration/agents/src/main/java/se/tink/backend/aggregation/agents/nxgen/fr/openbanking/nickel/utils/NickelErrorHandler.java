package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NickelErrorHandler {

    public RuntimeException handle(RuntimeException exception) {
        log.error("[Nickel-ob] {}", exception.getLocalizedMessage());
        return exception;
    }
}
