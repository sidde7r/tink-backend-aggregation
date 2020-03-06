package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessCodeStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetchHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.SingleTokenFetcher;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class OAuth2TokenFetcher extends SingleTokenFetcher<OAuth2Token> {

    public OAuth2TokenFetcher(
            AccessTokenFetchHelper<OAuth2Token> accessTokenFetchHelper,
            OAuth2TokenStorage tokenStorage,
            AccessCodeStorage accessCodeStorage) {
        super(accessTokenFetchHelper, tokenStorage, accessCodeStorage);
    }
}
