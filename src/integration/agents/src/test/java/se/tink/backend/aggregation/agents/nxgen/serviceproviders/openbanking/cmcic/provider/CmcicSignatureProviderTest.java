package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.DATE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.DIGEST;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.EXPECTED_GET_SIGNATURE_HEADER_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.EXPECTED_GET_STRING_TO_SIGN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.EXPECTED_POST_SIGNATURE_HEADER_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.EXPECTED_POST_STRING_TO_SIGN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.KEY_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.REQUEST_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.SERVER_URI;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicTestFixtures.SIGNATURE;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;

public class CmcicSignatureProviderTest {

    private CmcicSignatureProvider cmcicSignatureProvider;

    private QsealcSigner qsealcSignerMock;

    @Before
    public void setUp() {
        qsealcSignerMock = mock(QsealcSigner.class);

        cmcicSignatureProvider = new CmcicSignatureProvider(qsealcSignerMock);
    }

    @Test
    public void shouldGetSignatureHeaderValueForGet() {
        // given
        final ArgumentCaptor<byte[]> signingDataArgumentCaptor =
                ArgumentCaptor.forClass(byte[].class);
        when(qsealcSignerMock.getSignatureBase64(signingDataArgumentCaptor.capture()))
                .thenReturn(SIGNATURE);

        // when
        final String result =
                cmcicSignatureProvider.getSignatureHeaderValueForGet(
                        KEY_ID, SERVER_URI, DATE, REQUEST_ID);

        // then
        assertThat(result).isEqualTo(EXPECTED_GET_SIGNATURE_HEADER_VALUE);

        final String actualStringToSign = new String(signingDataArgumentCaptor.getValue());

        assertThat(actualStringToSign).isEqualTo(EXPECTED_GET_STRING_TO_SIGN);
    }

    @Test
    public void shouldGetSignatureHeaderValueForPost() {
        // given
        final ArgumentCaptor<byte[]> signingDataArgumentCaptor =
                ArgumentCaptor.forClass(byte[].class);
        when(qsealcSignerMock.getSignatureBase64(signingDataArgumentCaptor.capture()))
                .thenReturn(SIGNATURE);

        // when
        final String result =
                cmcicSignatureProvider.getSignatureHeaderValueForPost(
                        KEY_ID, SERVER_URI, DATE, DIGEST, REQUEST_ID);

        // then
        assertThat(result).isEqualTo(EXPECTED_POST_SIGNATURE_HEADER_VALUE);

        final String actualStringToSign = new String(signingDataArgumentCaptor.getValue());

        assertThat(actualStringToSign).isEqualTo(EXPECTED_POST_STRING_TO_SIGN);
    }
}
