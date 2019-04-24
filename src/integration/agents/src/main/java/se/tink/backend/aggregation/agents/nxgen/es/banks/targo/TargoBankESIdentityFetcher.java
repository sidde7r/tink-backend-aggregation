package se.tink.backend.aggregation.agents.nxgen.es.banks.targo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants.Tags;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class TargoBankESIdentityFetcher implements IdentityDataFetcher {

    private final SessionStorage sessionStorage;
    private static final String TITLE_PATTERN = "[A-Z][a-z]{1,4} ([A-Z -]+)";
    private final Pattern pattern = Pattern.compile(TITLE_PATTERN);

    TargoBankESIdentityFetcher(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public IdentityData fetchIdentityData() {
        return sessionStorage
                .get(Tags.LOGIN_RESPONSE, LoginResponse.class)
                .map(
                        response ->
                                EsIdentityData.builder()
                                        .setFullName(preProcessName(response.getClientName()))
                                        .setDateOfBirth(null)
                                        .build())
                .orElse(null);
    }

    private String preProcessName(String name) {
        // Try to remove title from name response
        Matcher matcher = pattern.matcher(name);

        return matcher.matches() ? matcher.group(1) : name;
    }
}
