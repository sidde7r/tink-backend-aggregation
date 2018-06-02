package se.tink.backend.common.repository.mysql.main;

import com.google.common.collect.Lists;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.core.FraudDetailsContent;
import se.tink.backend.core.FraudDetailsContentContainer;
import se.tink.backend.utils.LogUtils;

public class FraudDetailsContentRepositoryImpl implements FraudDetailsContentRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<FraudDetailsContent> findByUserId(String userId, CacheClient cacheClient) {
        byte[] data = (byte[]) cacheClient.get(CacheScope.FRAUD_DETAILS_BY_USERID, userId);
        List<FraudDetailsContent> detailsContents = new FraudDetailsContentContainer(data).getDetailsContent();

        if (detailsContents == null) {
            // Cache miss.
            try {
                FraudDetailsContentContainer container = em
                        .createQuery("select c from FraudDetailsContentContainer as c where c.userId = :userId",
                                FraudDetailsContentContainer.class).setParameter("userId", userId).getSingleResult();

                cacheClient.set(CacheScope.FRAUD_DETAILS_BY_USERID, userId, FraudDetailsContent.CACHE_EXPIRY,
                        container.getData());

                detailsContents = container.getDetailsContent();
            } catch (NoResultException e) {
                return Lists.newArrayList();
            }
        }

        return detailsContents;
    }
}
