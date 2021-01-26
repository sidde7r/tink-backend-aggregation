package se.tink.backend.aggregation.agents.abnamro.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AbnAmroIcsCredentials {
    public static final String ABN_AMRO_ICS_PROVIDER_NAME = "nl-abnamro-ics-abstract";

    public static final String CONTRACT_NUMBERS_FIELD_NAME = "contracts";
    public static final String BC_NUMBER_FIELD_NAME = "bcNumber";

    private static final TypeReference<Set<Long>> LONG_SET_TYPE_REFERENCE =
            new TypeReference<Set<Long>>() {};

    private final Credentials credentials;

    public AbnAmroIcsCredentials(Credentials credentials) {
        Preconditions.checkNotNull(credentials, "Credentials can't be null");
        Preconditions.checkArgument(
                isAbnAmroIcsCredentials(credentials), "Credentials is of wrong provider");

        this.credentials = credentials;
    }

    public static AbnAmroIcsCredentials create(String userId, String bcNumber) {
        return create(userId, bcNumber, Sets.<Long>newHashSet());
    }

    public static AbnAmroIcsCredentials create(
            String userId, String bcNumber, Set<Long> contracts) {

        Preconditions.checkNotNull(bcNumber);
        Preconditions.checkNotNull(userId);

        Credentials credentials = new Credentials();

        credentials.setField(BC_NUMBER_FIELD_NAME, bcNumber);
        credentials.setProviderName(ABN_AMRO_ICS_PROVIDER_NAME);
        credentials.setType(CredentialsTypes.PASSWORD);
        credentials.setUserId(userId);

        AbnAmroIcsCredentials abnAmroIcsCredentials = new AbnAmroIcsCredentials(credentials);

        abnAmroIcsCredentials.addContractNumbers(contracts);

        return abnAmroIcsCredentials;
    }

    public static boolean isAbnAmroIcsCredentials(Credentials credentials) {
        return Objects.equals(credentials.getProviderName(), ABN_AMRO_ICS_PROVIDER_NAME);
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void addContractNumber(Long contractNumber) {
        addContractNumbers(Sets.newHashSet(contractNumber));
    }

    public void addContractNumbers(Set<Long> newContracts) {

        Set<Long> contracts = getContractNumbers();

        if (newContracts != null) {
            contracts.addAll(newContracts);
        }

        credentials.setField(
                CONTRACT_NUMBERS_FIELD_NAME, SerializationUtils.serializeToString(contracts));

        // Set the status depending on if we have contracts on not. It is only possible to add
        // contracts so the state
        // machine is one of
        // a) NULL => CREATED
        // b) NULL => DISABLED => CREATED
        if (contracts.isEmpty()) {
            credentials.setStatus(CredentialsStatus.DISABLED);
        } else {
            credentials.setStatus(CredentialsStatus.CREATED);
        }
    }

    public Set<Long> getContractNumbers() {
        String contracts = credentials.getField(CONTRACT_NUMBERS_FIELD_NAME);

        if (Strings.isNullOrEmpty(contracts)) {
            return Sets.newHashSet();
        }

        return SerializationUtils.deserializeFromString(contracts, LONG_SET_TYPE_REFERENCE);
    }

    public boolean hasContracts() {
        return !getContractNumbers().isEmpty();
    }

    public String getBcNumber() {
        return credentials.getField(BC_NUMBER_FIELD_NAME);
    }
}
