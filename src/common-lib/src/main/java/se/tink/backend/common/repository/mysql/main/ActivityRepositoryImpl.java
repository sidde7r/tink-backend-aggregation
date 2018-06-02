package se.tink.backend.common.repository.mysql.main;

import com.google.common.collect.Lists;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.Activity;
import se.tink.backend.core.ActivityContainer;
import se.tink.backend.utils.LogUtils;

@Transactional
public class ActivityRepositoryImpl implements ActivityRepositoryCustom {
    private static final LogUtils log = new LogUtils(ActivityRepositoryImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Activity> findByUserId(String userId) {

        try {
            return em.createQuery("select c from ActivityContainer as c where c.userId = :userId",
                    ActivityContainer.class).setParameter("userId", userId).getSingleResult().getActivities();
        } catch (NoResultException e) {
            return Lists.newArrayList();
        }
    }

    @Transactional
    @Override
    public void deleteByUserId(String userId) {
        em.createQuery("delete from ActivityContainer c where c.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    @Transactional
    public void insertOrUpdate(ActivityContainer activityContainer) {
        em.createNativeQuery(
                "REPLACE INTO `activities` (`userid`, `data`) VALUES (?, ?)")
                .setParameter(1, activityContainer.getUserId())
                .setParameter(2, activityContainer.getData())
                .executeUpdate();
    }
}
