package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.identity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.IdentityEntity;
import se.tink.libraries.identitydata.IdentityData;

public class BoursoramaIdentityFetcher {
    private final Pattern surnamePattern = Pattern.compile("[A-Z]{2,}");

    public BoursoramaIdentityFetcher(BoursoramaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private final BoursoramaApiClient apiClient;

    public FetchIdentityDataResponse fetchIdentity() {
        IdentityEntity identityEntity = apiClient.fetchIdentityData();

        String[] splitStr = identityEntity.getConnectedPsu().split("\\s+");
        List<String> surnameList = new ArrayList<>();
        List<String> firstNameList = new ArrayList<>();
        Arrays.stream(splitStr)
                .forEach(
                        str -> {
                            if (isSurname(str)) {
                                surnameList.add(str);
                            } else {
                                firstNameList.add(str);
                            }
                        });

        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .addFirstNameElement(parseListToString(firstNameList))
                        .addSurnameElement(parseListToString(surnameList))
                        .setDateOfBirth(null)
                        .build());
    }

    private boolean isSurname(String s) {
        return surnamePattern.matcher(s).matches();
    }

    private String parseListToString(List<String> list) {
        return String.join(" ", list);
    }
}
