package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.FinTsSegment;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message.HNHBK;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message.HNHBS;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message.HNSHA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message.HNSHK;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message.HNVSD;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.segments.message.HNVSK;

public class FinTsRequest {
    private static final int SECREF_MIN_RANDOM = 1000000;
    private static final int SECREF_MAX_RANDOM = 9999999;
    private static final String DEFAULT_SECURITY_FUNC = "999";
    private int securityReference;
    private FinTsConfiguration configuration;
    private String systemId;
    private String dialogId;
    private int messageNumber;
    private List<FinTsSegment> segments = new ArrayList<>();

    private List<FinTsSegment> encryptedSegments = new ArrayList<>();
    private int profileVersion;
    private String securityFunction;
    private HNVSD encEnvelop;

    public FinTsRequest(FinTsConfiguration configuration, String dialogId, int messageNumber, String systemId,
            FinTsSegment... encryptedSegments) {
        this(configuration, dialogId, messageNumber, systemId, null, encryptedSegments);
    }

    public FinTsRequest(FinTsConfiguration configuration, String dialogId, int messageNumber, String systemId,
            List<String> tanMechs, FinTsSegment... encryptedSegments) {

        this.configuration = configuration;
        this.systemId = systemId;
        this.dialogId = dialogId;
        this.messageNumber = messageNumber;

        this.profileVersion = 1;
        this.securityFunction = DEFAULT_SECURITY_FUNC;
        if (tanMechs != null && tanMechs.size() != 0 && !tanMechs.contains(this.securityFunction)) {
            this.profileVersion = 2;
            this.securityFunction = tanMechs.get(0);
        }

        FinTsSegment signatureHeader = this.buildSignatureHead();
        FinTsSegment encryptionHead = this.buildEncryptionHead();

        this.segments.add(encryptionHead);

        this.encEnvelop = new HNVSD(999);

        this.segments.add(this.encEnvelop);

        this.appendEncSegment(signatureHeader);

        for (FinTsSegment segment : encryptedSegments) {
            this.appendEncSegment(segment);
        }

        // 3 header segments + # encrypted (command specific) segments
        int segmentCount = 3 + encryptedSegments.length;

        HNSHA signatureEnd = new HNSHA(segmentCount++, securityReference, configuration.getPassword());
        encEnvelop.appendEncryptedSegment(signatureEnd);

        HNHBS endSegment = new HNHBS(segmentCount, messageNumber);
        this.segments.add(endSegment);
    }

    public List<FinTsSegment> getEncryptedSegments() {
        return encryptedSegments;
    }

    private void appendEncSegment(FinTsSegment segment) {
        this.encryptedSegments.add(segment);
        this.encEnvelop.appendEncryptedSegment(segment);
    }

    private HNVSK buildEncryptionHead() {
        return new HNVSK(998, profileVersion, systemId, configuration.getBlz(), configuration.getUsername());
    }

    private HNSHK buildSignatureHead() {
        securityReference = SECREF_MIN_RANDOM + new Random().nextInt(SECREF_MAX_RANDOM - SECREF_MIN_RANDOM + 1);
        return new HNSHK(2, profileVersion, securityReference,
                securityFunction, systemId, configuration.getBlz(), configuration.getUsername());
    }

    public int getSecurityReference() {
        return securityReference;
    }

    private HNHBK buildHeader() {
        int l = 0;
        for (FinTsSegment segment : this.segments) {
            l += segment.toString().length();
        }

        return new HNHBK(l, this.dialogId, this.messageNumber);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder(this.buildHeader().toString());
        for (FinTsSegment segment : this.segments) {
            output.append(segment.toString());
        }
        return output.toString();
    }
}