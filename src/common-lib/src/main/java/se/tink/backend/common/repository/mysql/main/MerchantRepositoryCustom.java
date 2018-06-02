package se.tink.backend.common.repository.mysql.main;

import java.util.List;

import se.tink.backend.core.Merchant;

public interface MerchantRepositoryCustom {

    public void index(Merchant merchant);
    
    public Merchant saveAndIndex(Merchant merchant);
    
    public void saveAndIndex(List<Merchant> merchants);
    
    public void removeFromIndex(String id);
}
