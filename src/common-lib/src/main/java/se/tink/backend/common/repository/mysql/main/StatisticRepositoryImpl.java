package se.tink.backend.common.repository.mysql.main;

import com.google.common.collect.Lists;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticContainer;

public class StatisticRepositoryImpl implements StatisticRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        em.createQuery("delete from StatisticContainer s where s.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public List<Statistic> findByUserId(String userId) {
        try {
            return em.createQuery("select c from StatisticContainer as c where c.userId = :userId",
                            StatisticContainer.class).setParameter("userId", userId).getSingleResult().getStatistics();

        } catch (NoResultException e) {
            return Lists.newArrayList();
        }
    }

    @Override
    @Transactional
    public void insertOrUpdate(StatisticContainer statisticsContainer) {
        em.createNativeQuery(
                "REPLACE INTO `statistics` (`userid`, `data`) VALUES (?, ?)")
                .setParameter(1, statisticsContainer.getUserId())
                .setParameter(2, statisticsContainer.getData())
                .executeUpdate();
    }
}
