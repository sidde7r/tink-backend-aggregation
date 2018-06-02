package se.tink.backend.common.repository.mysql.main;

import java.util.Map;

public interface UserDemographicsRepositoryCustom {
    Map<String, String> findAllUserIdsAndPostalCodes(String marketCode);
}
