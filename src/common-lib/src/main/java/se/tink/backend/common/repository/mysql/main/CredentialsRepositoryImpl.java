package se.tink.backend.common.repository.mysql.main;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;

@Transactional
public class CredentialsRepositoryImpl implements CredentialsRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    /*
     * Returns a single credential where credentialId matches
     * if no such credential id can be found, a null is returned
     * instead of throwing a NoResultException
     */
    @Override
    public Credentials findOne(String credentialsId) {
        try {
            return em.createQuery("SELECT c FROM Credentials c WHERE c.id=:credentialsId", Credentials.class)
                    .setParameter("credentialsId", credentialsId).getSingleResult();
        } catch(NoResultException e){
            return null;
        }
    }

    @Transactional
    @Override
    public void deleteByUserId(String userId) {
        em.createQuery("DELETE FROM Credentials c where c.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public Map<String, Map<CredentialsStatus, BigInteger>> findStatusDistribution() {
        @SuppressWarnings("unchecked")
        List<Object> results = em.createNativeQuery(
                "SELECT providername, status, count(id) FROM tink.credentials GROUP BY providername, status")
                .getResultList();

        Map<String, Map<CredentialsStatus, BigInteger>> statusDistribution = Maps.newHashMap();

        for (Object result : results) {
            Object[] resultArray = (Object[]) result;

            String providerName = (String) resultArray[0];
            CredentialsStatus status = CredentialsStatus.valueOf((String) resultArray[1]);
            BigInteger count = (BigInteger) resultArray[2];

            if (!statusDistribution.containsKey(providerName)) {
                statusDistribution.put(providerName, Maps.<CredentialsStatus, BigInteger>newHashMap());
            }

            statusDistribution.get(providerName).put(status, count);
        }

        return statusDistribution;
    }

    @Override
    public Collection<Credentials> getAllOldAuthenticationErrors(Date maxDate) {
        return em
                .createQuery(
                        "SELECT c FROM Credentials c WHERE status=:statusError AND statusupdated <= :maxDate",
                        Credentials.class).setParameter("maxDate", maxDate)
                .setParameter("statusError", CredentialsStatus.AUTHENTICATION_ERROR).getResultList();
    }

    private static Joiner COMMA_JOINER = Joiner.on(',');

    private static String sqlQuestionMarks(int n) {
        ArrayList<String> list = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            list.add("?");
        }
        return COMMA_JOINER.join(list);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Credentials> findCredentialsToUpdate(
            Range<Double> refreshFrequency, Set<CredentialsTypes> ignoredTypes,
            Set<CredentialsStatus> allowedAutomaticRefreshStatuses,
            Date maxNextUpdate, int limit) {

        // We don't handle unbounded ranges for now.
        Preconditions.checkArgument(refreshFrequency.hasLowerBound());
        Preconditions.checkArgument(refreshFrequency.hasUpperBound());

        String hql = "SELECT c.* FROM credentials c"
                + " JOIN providers p ON c.providername = p.name"
                + (" WHERE"
                // Need to use range since refreshfrequency is a double.
                + " p.refreshfrequency "
                + (refreshFrequency.lowerBoundType().equals(BoundType.CLOSED) ? ">=" : ">") + " ?"
                + " AND p.refreshfrequency "
                + (refreshFrequency.upperBoundType().equals(BoundType.CLOSED) ? "<=" : "<") + " ?"
                + " AND c.type NOT IN (" + sqlQuestionMarks(ignoredTypes.size()) + ")"
                + " AND (c.status!='DELETED' OR c.status!='UPDATED' OR c.nextupdate IS NULL OR ?>=c.nextupdate)"
                + " AND c.status IN (" + sqlQuestionMarks(allowedAutomaticRefreshStatuses.size()) + ")")
                + " ORDER BY c.statusupdated ASC"; // Oldest first

        final Query q = em.createNativeQuery(hql, Credentials.class);

        q.setParameter(1, refreshFrequency.lowerEndpoint());

        q.setParameter(2, refreshFrequency.upperEndpoint());

        int count = 3;
        for (CredentialsTypes type : ignoredTypes) {
            q.setParameter(count, type.toString());
            count++;
        }

        q.setParameter(count, maxNextUpdate);
        count++;

        for (CredentialsStatus status : allowedAutomaticRefreshStatuses) {
            q.setParameter(count, status.toString());
            count++;
        }

        return q.setMaxResults(limit).getResultList();
    }

    @Override
    public List<Credentials> findAllByUserId(String userId) {
        return em.createQuery("SELECT c FROM Credentials c WHERE userid=:userid", Credentials.class)
                .setParameter("userid", userId)
                .getResultList();
    }

    @Override
    public List<Credentials> findAllByProviderName(String providerName) {
        return em.createQuery("SELECT c FROM Credentials c WHERE providername=:providerName AND status!='DELETED'", Credentials.class)
                .setParameter("providerName", providerName)
                .getResultList();
    }

    @Override
    public Map<String, String> findAllIdsAndProviderNames() {
        List<Object> rows = em.createQuery("SELECT id,providerName FROM Credentials WHERE status!='DELETED'").getResultList();
        Map<String, String> map = Maps.newHashMap();

        for (Object row : rows) {
            Object[] res = (Object[]) row;
            map.put((String) (res[0]), (String) res[1]);
        }

        return map;
    }

    @Override
    public Map<String, String> findAllIdsAndProviderNamesByUserId(String userId) {
        List<Object> rows = em.createQuery("SELECT id,providerName FROM Credentials WHERE userid = :userId AND status!='DELETED'")
                .setParameter("userId", userId).getResultList();

        Map<String, String> map = Maps.newHashMap();

        for (Object row : rows) {
            Object[] res = (Object[]) row;
            map.put((String) (res[0]), (String) res[1]);
        }

        return map;
    }

}
