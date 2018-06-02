package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import se.tink.backend.core.Subscription;

public class SubscriptionRepositoryImpl implements SubscriptionRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Subscription> findAllByUserId(String userId) {
        return em.createQuery(
                String.format("select o from %s o where o.userId=:userId", Subscription.class.getSimpleName()),
                Subscription.class).setParameter("userId", userId).getResultList();
    }

    @Override
    public void deleteByUserId(String userId) {
        em.createQuery(String.format("delete from %s where userId=:userId", Subscription.class.getSimpleName()))
                .setParameter("userId", userId).executeUpdate();
    }

}
