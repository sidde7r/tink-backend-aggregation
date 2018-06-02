package se.tink.backend.system.document.core;

import java.util.Optional;

public class Employment {

    private final String employer;
    private final Optional<String> since;

    public Employment(String employer, Optional<String> since) {
        this.employer = employer;
        this.since = since;
    }

    public String getEmployer() {
        return employer;
    }

    public Optional<String> getSince() {
        return since;
    }
}
