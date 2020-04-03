package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security;

import java.util.Random;

public class SecurityReferenceGenerator {

    private static final int MIN_RANDOM = 1000000;
    private static final int MAX_RANDOM = 9999999;

    public int generate() {
        return MIN_RANDOM + new Random().nextInt(MAX_RANDOM - MIN_RANDOM + 1);
    }
}
