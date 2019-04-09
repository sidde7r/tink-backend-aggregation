package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;

public enum CardType {

    /**
     * Below list is from aggregation log during one week where we list cards.
     *
     * <p>PREEM, PREMA, SHEMA, SEAT, VOLK, AUDI, SKODA, SKOVI, VIEW, HEMTX, BLIC, STAD, IKEACLUB,
     * IKEA, K?KS
     *
     * <p>Card providers not yet implemented:
     *
     * <p>ProviderType DeveloperInformation STADIUM("STAD", "STADP") - { Stadium Konto, Stadium
     * Personal } HEMTEX("HEMTX", "HEMTP") - { Studio Hemtex Konto, Hemtex Personal } IKEA("IKEA",
     * "IKEACLUB", "KÖKS") - { IKEA DELBETALA, IKEA FAMILY, IKEA LÅNA } SPECSAVERS("BLIC"), - {
     * Specsavers Optik } AUDI("AUDI", "SVWAUDI") - { Audi Visa, Audikortet }
     */
    PREEM("PREMA", "PREEM"),
    IKANOKORT("VIEW"),
    SEAT("SEAT"),
    SHELL("SHEMA"),
    SKODA("SKODA", "SKOVI"),
    VOLKSWAGEN("VOLK", "SVW");

    private final List<String> cardIdentifiers;

    CardType(String... cardIdentifiers) {
        this.cardIdentifiers = Lists.newArrayList(Arrays.asList(cardIdentifiers));
    }

    public boolean hasIdentifier(String identifier) {
        return this.cardIdentifiers.contains(identifier);
    }
}
