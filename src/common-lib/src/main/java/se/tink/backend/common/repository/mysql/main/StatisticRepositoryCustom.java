package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StatisticContainer;

public interface StatisticRepositoryCustom {
    void deleteByUserId(String userId);

    List<Statistic> findByUserId(String userId);

    void insertOrUpdate(StatisticContainer statisticsContainer);
}
