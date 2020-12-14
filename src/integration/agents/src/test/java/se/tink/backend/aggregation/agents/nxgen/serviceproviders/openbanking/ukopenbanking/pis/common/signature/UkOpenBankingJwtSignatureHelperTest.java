package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingJwtSignatureHelper.DATA;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.UkOpenBankingJwtSignatureHelper.RISK;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.SigningAlgorithm;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;

public class UkOpenBankingJwtSignatureHelperTest {

    private UkOpenBankingJwtSignatureHelper ukOpenBankingJwtSignatureHelper;

    private UkOpenBankingPaymentStorage paymentStorageMock;
    private UkOpenBankingRs256SignatureCreator rs256SignatureCreatorMock;
    private UkOpenBankingPs256SignatureCreator ps256SignatureCreatorMock;

    @Before
    public void setUp() {
        final ObjectMapper objectMapperMock = createObjectMapperMock();
        paymentStorageMock = mock(UkOpenBankingPaymentStorage.class);
        rs256SignatureCreatorMock = mock(UkOpenBankingRs256SignatureCreator.class);
        ps256SignatureCreatorMock = mock(UkOpenBankingPs256SignatureCreator.class);
        ukOpenBankingJwtSignatureHelper =
                new UkOpenBankingJwtSignatureHelper(
                        objectMapperMock,
                        paymentStorageMock,
                        rs256SignatureCreatorMock,
                        ps256SignatureCreatorMock);
    }

    @Test
    public void shouldCreatePs256Signature() {
        // given
        when(paymentStorageMock.getPreferredSigningAlgorithm()).thenReturn(SigningAlgorithm.PS256);
        when(ps256SignatureCreatorMock.createSignature(any())).thenReturn(SIGNATURE);

        // when
        final String returned = ukOpenBankingJwtSignatureHelper.createJwtSignature(new Object());

        // then
        assertThat(returned).isEqualTo(SIGNATURE);
        verify(ps256SignatureCreatorMock).createSignature(any());
        verifyZeroInteractions(rs256SignatureCreatorMock);
    }

    @Test
    public void shouldCreateRs256Signature() {
        // given
        when(paymentStorageMock.getPreferredSigningAlgorithm()).thenReturn(SigningAlgorithm.RS256);
        when(rs256SignatureCreatorMock.createSignature(any())).thenReturn(SIGNATURE);

        // when
        final String returned = ukOpenBankingJwtSignatureHelper.createJwtSignature(new Object());

        // then
        assertThat(returned).isEqualTo(SIGNATURE);
        verify(rs256SignatureCreatorMock).createSignature(any());
        verifyZeroInteractions(ps256SignatureCreatorMock);
    }

    @SuppressWarnings("unchecked")
    private ObjectMapper createObjectMapperMock() {
        final ObjectMapper objectMapperMock = mock(ObjectMapper.class);
        final Map<String, Object> map =
                ImmutableMap.of(
                        DATA, new Object(),
                        RISK, new Object());

        when(objectMapperMock.convertValue(any(), any(Class.class))).thenReturn(map);

        return objectMapperMock;
    }
}
