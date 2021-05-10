package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.identitydata;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.refresh.IdentityRefreshException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Tags;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

@Slf4j
@RequiredArgsConstructor
public class RuralviaIdentityDataFetcher implements IdentityDataFetcher {

    private final RuralviaApiClient apiClient;
    private final Credentials credentials;

    @Override
    public IdentityData fetchIdentityData() {
        String detailsToParse = navigateToIdentityDetails();

        return parseIdentityFromHtml(detailsToParse);
    }

    private IdentityData parseIdentityFromHtml(String detailsToParse) {
        if (detailsToParse.contains("disponible en estos momentos. Por favor, in")) {
            throw BankServiceError.DEFAULT_MESSAGE.exception(
                    "Identity is not available at this time due to bank unavailability");
        }
        Element dataContainer =
                Optional.ofNullable(Jsoup.parse(detailsToParse).getElementById("PORTLET-DATO"))
                        .orElseThrow(
                                () ->
                                        new IdentityRefreshException(
                                                "ERROR parsing the Identity data, element not found"));

        String name = dataContainer.select("td:containsOwn(Nombre) + td").first().ownText();
        String firtsSurname =
                dataContainer.select("td:containsOwn(Apellido 1) + td").first().ownText();
        String lastSurname =
                dataContainer.select("td:containsOwn(Apellido 2) + td").first().ownText();

        LocalDate birthDate = null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu");
            String date = dataContainer.select("td:containsOwn(Fecha) + td").first().ownText();
            birthDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            log.error("ERROR parsing the birth Date for Identity Data");
        }

        return EsIdentityData.builder()
                .setDocumentNumber(credentials.getField(Key.NATIONAL_ID_NUMBER))
                .addFirstNameElement(name)
                .addSurnameElement(firtsSurname)
                .addSurnameElement(lastSurname)
                .setDateOfBirth(birthDate)
                .build();
    }

    private String navigateToIdentityDetails() {
        Element html = new GlobalPositionResponse(apiClient.getGlobalPositionHtml()).getHtml();

        URL url =
                URL.of(
                        Urls.RURALVIA_SECURE_HOST
                                + html.select("a:containsOwn(Servicios)")
                                        .attr(Tags.ATTRIBUTE_TAG_HREF));

        return apiClient.navigateThroughIdentity(url);
    }
}
