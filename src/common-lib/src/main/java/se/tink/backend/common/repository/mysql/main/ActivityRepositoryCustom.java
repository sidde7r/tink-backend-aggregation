package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import se.tink.backend.core.Activity;
import se.tink.backend.core.ActivityContainer;

public interface ActivityRepositoryCustom {
    List<Activity> findByUserId(String userId);

    void deleteByUserId(String userId);

    void insertOrUpdate(ActivityContainer activityContainer);
}
