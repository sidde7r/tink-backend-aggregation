package se.tink.backend.common.repository.mysql.main;

import com.google.common.collect.Range;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;

public interface CredentialsRepositoryCustom {
    public Credentials findOne(String credentialsId);

    public void deleteByUserId(String userId);

    public Map<String, Map<CredentialsStatus, BigInteger>> findStatusDistribution();

    public Collection<? extends Credentials> getAllOldAuthenticationErrors(Date date);

    public List<Credentials> findCredentialsToUpdate(
            Range<Double> refreshFrequency, Set<CredentialsTypes> ignoredTypes,
            Set<CredentialsStatus> allowedAutomaticRefreshStatuses,
            Date maxNextUpdate, int limit);

    public List<Credentials> findAllByUserId(String userId);

    List<Credentials> findAllByProviderName(String providerName);

    public Map<String, String> findAllIdsAndProviderNames();

    public Map<String, String> findAllIdsAndProviderNamesByUserId(String userId);
}
