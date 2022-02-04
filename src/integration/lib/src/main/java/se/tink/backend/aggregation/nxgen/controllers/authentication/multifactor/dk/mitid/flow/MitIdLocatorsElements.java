package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.integration.webdriver.service.searchelements.ElementLocator;

public class MitIdLocatorsElements {

    private Map<MitIdLocator, ElementLocator> locatorsMapping = new HashMap<>();

    public ElementLocator getElementLocator(MitIdLocator mitIdLocator) {
        return locatorsMapping.get(mitIdLocator);
    }

    public MitIdLocator getMitIdLocatorByElementLocator(ElementLocator locator) {
        return locatorsMapping.entrySet().stream()
                .filter(entry -> entry.getValue() == locator)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Cannot find MitID locator by element locator: "
                                                + locator));
    }

    public void applyModifier(
            BiFunction<MitIdLocator, ElementLocator, ElementLocator> locatorFunction) {
        locatorsMapping =
                locatorsMapping.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry ->
                                                locatorFunction.apply(
                                                        entry.getKey(), entry.getValue())));
    }

    public MitIdLocatorsElements() {
        Stream.of(MitIdLocator.values())
                .forEach(
                        locator ->
                                locatorsMapping.put(locator, locator.getDefaultElementLocator()));
    }
}
