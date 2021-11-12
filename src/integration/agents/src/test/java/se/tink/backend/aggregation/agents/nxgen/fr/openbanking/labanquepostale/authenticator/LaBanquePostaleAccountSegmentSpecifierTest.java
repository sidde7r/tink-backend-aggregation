package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import se.tink.backend.agents.rpc.FinancialService;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleUserState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshScope;

@RunWith(JUnitParamsRunner.class)
public class LaBanquePostaleAccountSegmentSpecifierTest {
    @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

    private static final String ACCOUNT_SEGMENT_FIELD_NAME = "accountSegment";

    @Mock private ManualAuthenticateRequest manualAuthenticateRequest;
    @Mock private SupplementalInformationHelper supplementalInformationHelper;
    @Mock private RefreshScope refreshScope;
    @Mock private LaBanquePostaleUserState userState;
    private LaBanquePostaleAccountSegmentSpecifier laBanquePostaleAccountSegmentSpecifier;

    @Before
    public void setUp() {
        when(manualAuthenticateRequest.getRefreshScope()).thenReturn(refreshScope);
        when(userState.isAccountSegmentSpecified()).thenReturn(false);
        laBanquePostaleAccountSegmentSpecifier =
                new LaBanquePostaleAccountSegmentSpecifier(
                        supplementalInformationHelper, userState, manualAuthenticateRequest);
    }

    @Test
    public void shouldSetStateAsPersonalWhenRefreshScopesAreMissing() {
        // given
        when(manualAuthenticateRequest.getRefreshScope()).thenReturn(null);
        when(refreshScope.getFinancialServiceSegmentsIn()).thenReturn(Collections.emptySet());

        // when
        laBanquePostaleAccountSegmentSpecifier.specifyAccountSegment();

        // then
        verify(userState).specifyAccountSegment(LaBanquePostaleAccountSegment.PERSONAL);
    }

    @Test
    public void shouldSetStateAsBusinessWithBusinessScope() {
        // given
        when(refreshScope.getFinancialServiceSegmentsIn())
                .thenReturn(Sets.newSet(FinancialService.FinancialServiceSegment.BUSINESS));

        // when
        laBanquePostaleAccountSegmentSpecifier.specifyAccountSegment();

        // then
        verify(userState).specifyAccountSegment(LaBanquePostaleAccountSegment.BUSINESS);
    }

    @Test
    public void shouldSetStateAsPersonalWithPersonalScope() {
        // given
        when(refreshScope.getFinancialServiceSegmentsIn())
                .thenReturn(Sets.newSet(FinancialService.FinancialServiceSegment.PERSONAL));

        // when
        laBanquePostaleAccountSegmentSpecifier.specifyAccountSegment();

        // then
        verify(userState).specifyAccountSegment(LaBanquePostaleAccountSegment.PERSONAL);
    }

    @Test
    @Parameters(value = {"PERSONAL", "BUSINESS"})
    public void shouldRequestForSupplementalInformationWithPersonalAndBusinessScopes(
            String segment) {
        // given
        when(refreshScope.getFinancialServiceSegmentsIn())
                .thenReturn(
                        Sets.newSet(
                                FinancialService.FinancialServiceSegment.BUSINESS,
                                FinancialService.FinancialServiceSegment.PERSONAL));
        when(supplementalInformationHelper.askSupplementalInformation(any()))
                .thenReturn(Maps.newHashMap(ACCOUNT_SEGMENT_FIELD_NAME, segment));

        // when
        laBanquePostaleAccountSegmentSpecifier.specifyAccountSegment();

        // then
        verify(userState).specifyAccountSegment(LaBanquePostaleAccountSegment.valueOf(segment));
    }
}
