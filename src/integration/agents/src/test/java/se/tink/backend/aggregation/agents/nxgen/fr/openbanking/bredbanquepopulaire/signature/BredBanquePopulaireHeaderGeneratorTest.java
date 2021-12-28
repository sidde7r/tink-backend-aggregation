package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.DIGEST;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.KEY_ID;

import org.junit.Before;
import org.junit.Test;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireConstants;
import se.tink.backend.aggregation.eidassigner.FakeQsealcSigner;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BredBanquePopulaireHeaderGeneratorTest {
    private static final QsealcAlgorithm qsealcAlgorithm = QsealcAlgorithm.RSA_SHA256;
    private static final String REQUEST_ID = "0000";
    private static final String REQUEST_BODY = "requestBody";

    private final SessionStorage sessionStorage = new SessionStorage();
    private final QsealcSigner qsealcSigner = new FakeQsealcSigner();
    private BredBanquePopulaireHeaderGenerator bredBanquePopulaireHeaderGenerator;

    @Before
    public void setUp() {
        bredBanquePopulaireHeaderGenerator =
                new BredBanquePopulaireHeaderGenerator(
                        qsealcSigner, qsealcAlgorithm, sessionStorage, KEY_ID);
    }

    @Test
    public void shouldBuildDigestHeaderValue() {
        // when
        final String resultDigestHeaderValue =
                bredBanquePopulaireHeaderGenerator.getDigestHeaderValue(REQUEST_BODY);

        // then
        assertThat(resultDigestHeaderValue).isEqualTo(DIGEST);
    }

    @Test
    public void shouldBuildSignatureHeaderForGetMethod() {
        // given
        sessionStorage.put(BredBanquePopulaireConstants.StorageKeys.DIGEST, DIGEST);

        // when
        final String resultSignatureHeader =
                bredBanquePopulaireHeaderGenerator.buildSignatureHeader(
                        HttpMethod.GET, new URL("https://server/accounts"), REQUEST_ID);

        // then
        final String expectedSignatureHeader =
                "keyId=\"key-id\",algorithm=\"rsa-sha256\",headers=\"(request-target) x-request-id\",signature=\"RkFLRV9TSUdOQVRVUkUK\"";
        assertThat(resultSignatureHeader).isEqualTo(expectedSignatureHeader);
    }

    @Test
    public void shouldBuildSignatureHeaderForGetMethodAndUrlWithQueryParams() {
        // given
        sessionStorage.put(BredBanquePopulaireConstants.StorageKeys.DIGEST, DIGEST);

        // when
        final String url = "/accounts/1234/transactions?dateFrom=2020-01-01&dateTo=2020-01-02";

        // when
        final String resultSignatureHeader =
                bredBanquePopulaireHeaderGenerator.buildSignatureHeader(
                        HttpMethod.GET, new URL("https://server" + url), REQUEST_ID);

        // then
        final String expectedSignatureHeader =
                "keyId=\"key-id\",algorithm=\"rsa-sha256\",headers=\"(request-target) x-request-id\",signature=\"RkFLRV9TSUdOQVRVUkUK\"";
        assertThat(resultSignatureHeader).isEqualTo(expectedSignatureHeader);
    }

    @Test
    public void shouldBuildSignatureHeaderForPutMethod() {
        // given
        sessionStorage.put(BredBanquePopulaireConstants.StorageKeys.DIGEST, DIGEST);

        // when
        final String resultSignatureHeader =
                bredBanquePopulaireHeaderGenerator.buildSignatureHeader(
                        HttpMethod.PUT, new URL("https://server/consents"), REQUEST_ID);

        // then
        final String expectedSignatureHeader =
                "keyId=\"key-id\",algorithm=\"rsa-sha256\",headers=\"(request-target) x-request-id digest content-type\",signature=\"RkFLRV9TSUdOQVRVUkUK\"";
        assertThat(resultSignatureHeader).isEqualTo(expectedSignatureHeader);
    }
}
