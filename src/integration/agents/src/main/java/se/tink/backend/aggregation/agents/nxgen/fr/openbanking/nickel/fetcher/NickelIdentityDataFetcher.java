package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.fetcher.rpc.NickelUserResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils.NickelErrorHandler;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class NickelIdentityDataFetcher implements IdentityDataFetcher {

    private final NickelApiClient apiClient;
    private final NickelErrorHandler errorHandler;

    @Override
    public IdentityData fetchIdentityData() {
        try {
            final NickelUserResponse userIdentityResponse =
                    apiClient
                            .getEndUserIdentity()
                            .orElseThrow(
                                    () -> new IllegalStateException("Can't fetch user identity"));
            return IdentityData.builder()
                    .addFirstNameElement(userIdentityResponse.getFirstName())
                    .addSurnameElement(userIdentityResponse.getLastName())
                    .setDateOfBirth(userIdentityResponse.getBirthDay().toLocalDate())
                    .build();
        } catch (RuntimeException e) {
            throw errorHandler.handle(e);
        }
    }

    public FetchIdentityDataResponse response() {
        try {
            return new FetchIdentityDataResponse(fetchIdentityData());
        } catch (RuntimeException e) {
            throw errorHandler.handle(e);
        }
    }
}
