package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.Merchant;
import se.tink.backend.core.MerchantSources;

import java.util.List;

public interface MerchantRepository extends JpaRepository<Merchant, String>, MerchantRepositoryCustom {
    
    public Merchant findOneByOrganizationId(String organizationId);
    
    public List<Merchant> findAllByReference(String reference);

    public List<Merchant> findAllByReferenceAndSource(String reference, MerchantSources source);
}
