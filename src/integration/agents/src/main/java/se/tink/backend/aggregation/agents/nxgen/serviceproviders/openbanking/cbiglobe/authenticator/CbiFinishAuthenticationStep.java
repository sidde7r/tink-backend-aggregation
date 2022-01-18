package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentDetailsResponse;

@RequiredArgsConstructor
@Slf4j
public class CbiFinishAuthenticationStep {

    private final CbiGlobeAuthApiClient authApiClient;
    private final Credentials credentials;
    private final CbiStorage storage;

    public void storeConsentValidUntilDateInCredentials() {
        ConsentDetailsResponse consentDetails =
                authApiClient.fetchConsentDetails(storage.getConsentId());

        LocalDate consentValidUntil = parseConsentsValidUntil(consentDetails);

        credentials.setSessionExpiryDate(consentValidUntil);
    }

    // CBI Banks can return validUntil in two different formats, supposedly.
    // Lets leave behaviour as it was, adding lots of logging to check if it still the case.
    private LocalDate parseConsentsValidUntil(ConsentDetailsResponse consentDetails) {
        Optional<LocalDate> parsedDate =
                tryToGetValidUntil(
                        () ->
                                consentDetails
                                        .getValidUntil(
                                                DateTimeFormatter.ofPattern(
                                                        "yyyy-MM-dd'T'HH:mm:ss'Z'"))
                                        .toLocalDate());
        if (!parsedDate.isPresent()) {
            log.info("[CBI] Default parsing of validUntil failed, moving to backup.");
            parsedDate = tryToGetValidUntil(consentDetails::getValidUntil);
        }

        // None of the expected format fits, mark as internal exception.
        return parsedDate.orElseThrow(
                () ->
                        new IllegalStateException(
                                "[CBI] Could not parse the consent validUntil field using any of the expected formats!"));
    }

    private Optional<LocalDate> tryToGetValidUntil(Supplier<LocalDate> supplier) {
        try {
            return Optional.of(supplier.get());
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
