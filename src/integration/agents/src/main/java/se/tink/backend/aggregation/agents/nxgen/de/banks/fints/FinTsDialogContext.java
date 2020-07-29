package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsSecretsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.BaseResponsePart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.SecurityReferenceGenerator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.TanByOperationLookup;

public class FinTsDialogContext {

    // Constants
    private static final String DEFAULT_SECURITY_FUNCTION = "999";
    private static final String DEFAULT_TAN_MEDIIUM = "DUMMY";

    private static final int ONE_FACTOR = 1;
    private static final int TWO_FACTOR = 2;

    private static final String UNINITIALIZED_ID = "0";

    // Dependencies
    private SecurityReferenceGenerator securityReferenceGenerator;
    @Getter private FinTsConfiguration configuration;
    @Getter private FinTsSecretsConfiguration secretsConfiguration;

    // Dialog metadata
    @Getter @Setter private String systemId = UNINITIALIZED_ID;
    @Getter @Setter private String dialogId = UNINITIALIZED_ID;
    @Getter @Setter private int messageNumber = 1;
    @Getter private int securityReference; // Unique number per message

    // Bank + user parameters
    @Getter @Setter private Map<String, String> allowedSecurityFunctions;
    @Getter @Setter private String chosenSecurityFunction = DEFAULT_SECURITY_FUNCTION;
    @Getter private List<String> tanMediumList = new ArrayList<>();
    @Getter @Setter private String chosenTanMedium = DEFAULT_TAN_MEDIIUM;
    private Map<Pair<SegmentType, Integer>, BaseResponsePart> supportedOperationVersionsLookup =
            new HashMap<>();
    @Setter private TanByOperationLookup tanByOperationLookup;

    @Getter private List<FinTsAccountInformation> accounts = new ArrayList<>();

    @Getter @Setter private String taskReference;
    @Getter @Setter private String tanAnswer;

    public FinTsDialogContext(
            FinTsConfiguration configuration, FinTsSecretsConfiguration secretsConfiguration) {
        this(configuration, secretsConfiguration, new SecurityReferenceGenerator());
    }

    public FinTsDialogContext(
            FinTsConfiguration configuration,
            FinTsSecretsConfiguration secretsConfiguration,
            SecurityReferenceGenerator securityReferenceGenerator) {
        this.configuration = configuration;
        this.secretsConfiguration = secretsConfiguration;
        this.securityReferenceGenerator = securityReferenceGenerator;
        generateNewSecurityReference();
    }

    public void generateNewSecurityReference() {
        this.securityReference = securityReferenceGenerator.generate();
    }

    public boolean isDialogIdUninitialized() {
        return UNINITIALIZED_ID.equals(dialogId);
    }

    public void resetDialogId() {
        dialogId = UNINITIALIZED_ID;
    }

    public int getSecurityProcedureVersion() {
        return DEFAULT_SECURITY_FUNCTION.equals(chosenSecurityFunction) ? ONE_FACTOR : TWO_FACTOR;
    }

    public void addOperationSupportedByBank(SegmentType segmentType, BaseResponsePart details) {
        supportedOperationVersionsLookup.put(
                Pair.of(segmentType, details.getSegmentVersion()), details);
    }

    public List<Integer> getVersionsOfOperationSupportedByBank(SegmentType segmentType) {
        return supportedOperationVersionsLookup.keySet().stream()
                .filter(pair -> pair.getLeft().equals(segmentType))
                .map(Pair::getRight)
                .collect(Collectors.toList());
    }

    public BaseResponsePart getDetailsOfSupportedOperation(SegmentType segmentType, int version) {
        return supportedOperationVersionsLookup.get(Pair.of(segmentType, version));
    }

    public boolean doesOperationRequireTAN(SegmentType segmentType) {
        return tanByOperationLookup.doesOperationRequireTAN(segmentType);
    }

    public boolean isOperationSupported(SegmentType segmentType) {
        return tanByOperationLookup.isOperationSupported(segmentType);
    }
}
