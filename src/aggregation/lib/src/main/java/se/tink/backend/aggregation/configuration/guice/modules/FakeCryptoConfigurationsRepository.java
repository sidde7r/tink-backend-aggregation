package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;

public class FakeCryptoConfigurationsRepository implements CryptoConfigurationsRepository {
    private static final String UNSUPPORTED_OPERATION_MESSAGE = "Not Implemented";

    private final CryptoConfiguration cryptoConfiguration;

    @Inject
    public FakeCryptoConfigurationsRepository(CryptoConfiguration cryptoConfiguration) {
        this.cryptoConfiguration = cryptoConfiguration;
    }

    @Override
    public List<CryptoConfiguration> findByCryptoConfigurationIdClientName(String clientName) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public CryptoConfiguration findByCryptoConfigurationId(CryptoConfigurationId cryptoId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public List<CryptoConfiguration> findAll() {
        System.out.println("hoy DummyCryptoConfigurationsRepository.findAll");
        return Collections.singletonList(cryptoConfiguration);
    }

    @Override
    public List<CryptoConfiguration> findAll(Sort sort) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public Page<CryptoConfiguration> findAll(Pageable pageable) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public List<CryptoConfiguration> findAll(Iterable<CryptoConfigurationId> iterable) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public long count() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public void delete(CryptoConfigurationId cryptoConfigurationId) {

        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public void delete(CryptoConfiguration cryptoConfiguration) {

        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public void delete(Iterable<? extends CryptoConfiguration> iterable) {

        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public void deleteAll() {

        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> S save(S s) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> List<S> save(Iterable<S> iterable) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public CryptoConfiguration findOne(CryptoConfigurationId cryptoConfigurationId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public boolean exists(CryptoConfigurationId cryptoConfigurationId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public void flush() {

        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> S saveAndFlush(S s) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public void deleteInBatch(Iterable<CryptoConfiguration> iterable) {

        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public void deleteAllInBatch() {

        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public CryptoConfiguration getOne(CryptoConfigurationId cryptoConfigurationId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> S findOne(Example<S> example) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> long count(Example<S> example) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }

    @Override
    public <S extends CryptoConfiguration> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
    }
}
