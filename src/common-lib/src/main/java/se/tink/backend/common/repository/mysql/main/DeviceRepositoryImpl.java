package se.tink.backend.common.repository.mysql.main;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class DeviceRepositoryImpl implements DeviceRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Override
    public void deleteByUserId(String userId) {
        em.createQuery("delete from Device d where d.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }

    @Transactional
    @Override
    public void deleteByNotificationTokenAndNotDeviceToken(String notificationToken, String deviceToken) {
        em.createQuery("delete from Device d where d.notificationToken = :notificationToken and d.deviceToken != :deviceToken")
                .setParameter("notificationToken", notificationToken).setParameter("deviceToken", deviceToken).executeUpdate();
    }

    @Transactional
    @Override
    public void deleteByDeviceToken(String deviceToken) {
        em.createQuery("delete from Device d where d.deviceToken = :deviceToken")
               .setParameter("deviceToken", deviceToken).executeUpdate();
    }

    @Transactional
    @Override
    public void deleteByDeviceTokenAndNotUserId(String deviceToken, String userId) {
        em.createQuery("delete from Device d where d.deviceToken = :deviceToken and d.userId != :userId")
                .setParameter("deviceToken", deviceToken).setParameter("userId", userId).executeUpdate();
    }

    @Transactional
    @Override
    public void deleteByNotificationToken(String notificationToken) {
        em.createQuery("delete from Device d where d.notificationToken = :notificationToken")
                .setParameter("notificationToken", notificationToken).executeUpdate();
    }
}
