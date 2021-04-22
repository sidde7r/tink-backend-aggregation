package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.DocumentTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.utils.WizinkDecoder;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData.EsIdentityDataBuilder;

@Slf4j
public class WizinkIdentityDataFetcher implements IdentityDataFetcher {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final WizinkStorage wizinkStorage;

    public WizinkIdentityDataFetcher(WizinkStorage wizinkStorage) {
        this.wizinkStorage = wizinkStorage;
    }

    @Override
    public IdentityData fetchIdentityData() {
        final LoginResponse loginResponse = wizinkStorage.getLoginResponse();
        EsIdentityDataBuilder builder = EsIdentityData.builder();
        if (DocumentTypes.NIF.equalsIgnoreCase(loginResponse.getDocumentType())) {
            builder.setNifNumber(
                    WizinkDecoder.decodeMaskedNumber(
                            loginResponse.getNif(), wizinkStorage.getXTokenUser()));
        } else {
            log.warn("Unhandled document type: {}", loginResponse.getDocumentType());
        }
        return builder.setFullName(loginResponse.getName())
                .setDateOfBirth(
                        getDecodedDateOfBirth(
                                loginResponse.getGlobalPosition().getEncodedDateOfBirth(),
                                wizinkStorage.getXTokenUser()))
                .build();
    }

    private LocalDate getDecodedDateOfBirth(String encodedDateOfBirth, String xUserToken) {
        return LocalDate.parse(
                WizinkDecoder.decodeMaskedNumber(encodedDateOfBirth, xUserToken), DATE_FORMATTER);
    }
}
