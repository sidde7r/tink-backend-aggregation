package se.tink.backend.aggregation.agents.agentplatform.authentication;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.AgentFieldValue;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentFieldDefinition;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class UserInteractionService {

    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";
    private final SupplementalInformationController supplementalInformationController;
    private final CredentialsRequest credentialsRequest;
    private AgentInteractionDataToSupplementalInformationMapper
            agentInteractionDataToSupplementalInformationMapper;

    public UserInteractionService(
            SupplementalInformationController supplementalInformationController,
            CredentialsRequest credentialsRequest) {
        this.supplementalInformationController = supplementalInformationController;
        this.credentialsRequest = credentialsRequest;
        agentInteractionDataToSupplementalInformationMapper =
                new AgentInteractionDataToSupplementalInformationMapper(
                        new SupplementalInformationFormer(credentialsRequest.getProvider()));
    }

    public List<AgentFieldValue> requestForFields(List<AgentFieldDefinition> requestedFields) {
        List<AgentFieldValue> values = new LinkedList<>();
        List<AgentFieldDefinition> fieldsToAskUser = new LinkedList<>();
        requestedFields.forEach(
                requestedField -> {
                    Optional<AgentFieldValue> fieldValue =
                            tryFetchFromCredentials(requestedField.getFieldIdentifier());
                    if (fieldValue.isPresent()) {
                        values.add(fieldValue.get());
                    } else {
                        fieldsToAskUser.add(requestedField);
                    }
                });
        if (!fieldsToAskUser.isEmpty()) {
            values.addAll(requestUserForFields(fieldsToAskUser));
        }
        return values;
    }

    public Optional<Map<String, String>> redirect(String redirectUrl) {
        ThirdPartyRequest thirdPartyRequest =
                agentInteractionDataToSupplementalInformationMapper.toThirdPartyRequest(
                        redirectUrl);
        final String waitOnKey =
                String.format(
                        UNIQUE_PREFIX_TPCB,
                        thirdPartyRequest.getSupplementalWaitRequest().getKey());
        supplementalInformationController.openThirdPartyApp(thirdPartyRequest.getPayload());
        return supplementalInformationController.waitForSupplementalInformation(
                waitOnKey,
                thirdPartyRequest.getSupplementalWaitRequest().getWaitFor(),
                thirdPartyRequest.getSupplementalWaitRequest().getTimeUnit());
    }

    private Optional<AgentFieldValue> tryFetchFromCredentials(final String fieldCode) {
        return Optional.ofNullable(credentialsRequest.getCredentials().getField(fieldCode))
                .map(fieldValue -> new AgentFieldValue(fieldCode, fieldValue));
    }

    private List<AgentFieldValue> requestUserForFields(List<AgentFieldDefinition> definitions) {
        try {
            return supplementalInformationController
                    .askSupplementalInformation(
                            agentInteractionDataToSupplementalInformationMapper.toFields(
                                    definitions))
                    .entrySet().stream()
                    .map(entry -> new AgentFieldValue(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        } catch (SupplementalInfoException e) {
            return new LinkedList<>();
        }
    }
}
