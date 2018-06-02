package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.CityCoordinate;

@Repository
public interface CityCoordinateRepository extends JpaRepository<CityCoordinate, Long> {

    public CityCoordinate findOneByCityAndCountry(String city, String country);
}
