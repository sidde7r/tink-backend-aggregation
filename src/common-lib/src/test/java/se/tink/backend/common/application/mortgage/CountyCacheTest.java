package se.tink.backend.common.application.mortgage;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import se.tink.backend.core.Municipality;
import static org.assertj.core.api.Assertions.assertThat;

public class CountyCacheTest {
    @Test
    public void cachesCounties() throws ExecutionException {
        // Not only values, but also the instance is the same
        assertThat(CountyCache.getCounties())
                .isSameAs(CountyCache.getCounties());
    }

    @Test
    public void countiesExist() throws ExecutionException {
        assertThat(CountyCache.getCounties()).isNotEmpty();
    }

    @Test
    public void ronnebyCanBeFoundAmongMunicipalities() {
        Municipality ronneby = CountyCache.findMunicipality("1081").orElse(null);

        assertThat(ronneby).isNotNull();
        assertThat(ronneby.getCode()).isEqualTo("1081");
        assertThat(ronneby.getName()).isEqualTo("Ronneby");
    }

    @Test
    public void ronnebyNameCanBeFoundAmongMunicipalities() {
        Optional<String> ronneby = CountyCache.findMunicipalityName("1081");
        assertThat(ronneby.orElse(null)).isEqualTo("Ronneby");
    }

    @Test
    public void absentNameCannotBeFoundAmongMunicipalities() {
        Optional<String> ronneby = CountyCache.findMunicipalityName("999999999");
        assertThat(ronneby.isPresent()).isFalse();
    }
}
