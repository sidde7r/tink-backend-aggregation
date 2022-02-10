package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.FinancialService;
import se.tink.backend.agents.rpc.FinancialService.FinancialServiceSegment;
import se.tink.backend.agents.rpc.SelectOption;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleUserState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@Slf4j
public class LaBanquePostaleAccountSegmentSpecifier {
    private static final String ACCOUNT_SEGMENT_FIELD_NAME = "accountSegment";
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final LaBanquePostaleUserState userState;
    private final CredentialsRequest credentialsRequest;

    public LaBanquePostaleAccountSegmentSpecifier(
            SupplementalInformationHelper supplementalInformationHelper,
            LaBanquePostaleUserState userState,
            CredentialsRequest credentialsRequest) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.userState = userState;
        this.credentialsRequest = credentialsRequest;
    }

    public void specifyAccountSegment() {
        if (!userState.isAccountSegmentSpecified()) {
            if (credentialsRequest instanceof HasRefreshScope
                    && ((HasRefreshScope) credentialsRequest).getRefreshScope() != null) {
                Set<FinancialServiceSegment> financialServiceSegments =
                        ((HasRefreshScope) credentialsRequest)
                                .getRefreshScope()
                                .getFinancialServiceSegmentsIn();

                if (financialServiceSegments.contains(
                                FinancialService.FinancialServiceSegment.BUSINESS)
                        && !financialServiceSegments.contains(
                                FinancialService.FinancialServiceSegment.PERSONAL)) {
                    userState.specifyAccountSegment(LaBanquePostaleAccountSegment.BUSINESS);
                } else if (financialServiceSegments.contains(
                                FinancialService.FinancialServiceSegment.PERSONAL)
                        && !financialServiceSegments.contains(
                                FinancialService.FinancialServiceSegment.BUSINESS)) {
                    userState.specifyAccountSegment(LaBanquePostaleAccountSegment.PERSONAL);
                } else if (financialServiceSegments.contains(
                                FinancialService.FinancialServiceSegment.PERSONAL)
                        && financialServiceSegments.contains(
                                FinancialService.FinancialServiceSegment.BUSINESS)) {
                    log.info("FinancialServiceSegment passed: {}", financialServiceSegments);
                    requestForSupplementalInformation();
                }
            } else {
                userState.specifyAccountSegment(LaBanquePostaleAccountSegment.PERSONAL);
            }
        }
    }

    private void requestForSupplementalInformation() {
        String value =
                supplementalInformationHelper
                        .askSupplementalInformation(prepareAccountSegmentField())
                        .get(ACCOUNT_SEGMENT_FIELD_NAME);
        userState.specifyAccountSegment(LaBanquePostaleAccountSegment.valueOf(value));
    }

    private Field prepareAccountSegmentField() {
        return Field.builder()
                .name(ACCOUNT_SEGMENT_FIELD_NAME)
                .selectOptions(
                        Lists.newArrayList(
                                new SelectOption(new LocalizableKey("Personal").get(), "PERSONAL"),
                                new SelectOption(new LocalizableKey("Business").get(), "BUSINESS")))
                .description(
                        new LocalizableKey("Which account segment do you want to aggregate?").get())
                .sensitive(false)
                .build();
    }
}
