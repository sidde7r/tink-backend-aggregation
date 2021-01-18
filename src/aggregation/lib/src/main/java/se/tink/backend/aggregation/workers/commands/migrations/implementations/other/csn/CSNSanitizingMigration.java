package se.tink.backend.aggregation.workers.commands.migrations.implementations.other.csn;

import com.google.common.base.Joiner;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.workers.commands.migrations.ClusterSafeAgentVersionMigration;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CSNSanitizingMigration extends ClusterSafeAgentVersionMigration {
    private static final String OLD_AGENT = "legacy.other.CSNAgent";
    private static final String NEW_AGENT = "nxgen.se.other.csn.CSNAgent";
    private static final String ANNUITY_LOAN_OLD_REGEX =
            "\\d{2}(\\d{10}): Lån efter 30 juni 2001 \\(annuitetslån\\)";
    private static final String ANNUITY_LOAN_NEW_REGEX = "$1annuitetslan";
    private static final String STUDENT_LOAN_OLD_REGEX =
            "\\d{2}(\\d{10}): Lån 1 januari 1989-30 juni 2001 \\(studielån\\)";
    private static final String STUDENT_LOAN_NEW_REGEX = "$1studielan";
    private static final String STUDENT_AID_OLD_REGEX =
            "\\d{2}(\\d{10}): Lån före 1989 \\(studiemedel\\)";
    private static final String STUDENT_AID_NEW_REGX = "$1studiemedel";
    private static final Joiner REGEXP_OR_JOINER = Joiner.on("|");

    @Override
    public boolean isOldAgent(Provider provider) {
        return provider.getClassName().equals(OLD_AGENT);
    }

    @Override
    public boolean isNewAgent(Provider provider) {
        return provider.getClassName().equals(NEW_AGENT);
    }

    @Override
    public String getNewAgentClassName(Provider oldProvider) {
        return NEW_AGENT;
    }

    @Override
    public boolean isDataMigrated(CredentialsRequest request) {
        return request.getAccounts().stream()
                .noneMatch(account -> matchOldRegex(account.getBankId()));
    }

    @Override
    public void migrateData(CredentialsRequest request) {
        request.getAccounts()
                .forEach(account -> account.setBankId(replaceOldRegex(account.getBankId())));
    }

    private boolean matchOldRegex(String bankId) {
        return bankId.matches(
                REGEXP_OR_JOINER.join(
                        ANNUITY_LOAN_OLD_REGEX, STUDENT_LOAN_OLD_REGEX, STUDENT_AID_OLD_REGEX));
    }

    private String replaceOldRegex(String bankId) {
        return bankId.replaceAll(ANNUITY_LOAN_OLD_REGEX, ANNUITY_LOAN_NEW_REGEX)
                .replaceAll(STUDENT_LOAN_OLD_REGEX, STUDENT_LOAN_NEW_REGEX)
                .replaceAll(STUDENT_AID_OLD_REGEX, STUDENT_AID_NEW_REGX);
    }
}
