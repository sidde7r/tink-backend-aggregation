package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.identitydata;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaConstants.NOT_AVAILABLE_AT_THE_MOMENT;

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
        if (detailsToParse.contains(NOT_AVAILABLE_AT_THE_MOMENT)) {
            throw BankServiceError.DEFAULT_MESSAGE.exception(
                    "Identity is not available at this time due to bank unavailability");
        }
        Element dataContainer =
                Optional.ofNullable(Jsoup.parse(detailsToParse).getElementById("PORTLET-DATO"))
                        .orElseThrow(
                                () ->
                                        new IdentityRefreshException(
                                                "ERROR parsing the Identity data, element not found"));

        IdentityDataPage dataFetcher = new IdentityDataPage(dataContainer);

        return EsIdentityData.builder()
                .setDocumentNumber(credentials.getField(Key.NATIONAL_ID_NUMBER))
                .addFirstNameElement(dataFetcher.getName())
                .addSurnameElement(dataFetcher.getFirstSurname())
                .addSurnameElement(dataFetcher.getLastSurname())
                .setDateOfBirth(dataFetcher.getBirthDate())
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
