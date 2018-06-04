package se.tink.backend.common.dao;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.categorization.api.SECategories;
import se.tink.backend.common.dao.transactions.TransactionCleaner;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.core.Transaction;

public class TransactionCleanerTest {

    private CategoryRepository categoryRepository;
    private ArrayList<Category> categories;
    private Category notToModify;
    private Category toModifyTo;
    private Category leafNodeCategory;
    private TransactionCleaner cleaner;

    private Category constructCategory(String code, CategoryTypes type) {
        Category c = new Category();
        c.setCode(code);
        c.setType(type);
        return c;
    }
    
    @Before
    public void setUp() {
        categoryRepository = Mockito.mock(CategoryRepository.class);
        
        categories = Lists.newArrayList();

        notToModify = constructCategory(SECategories.Codes.EXPENSES_ENTERTAINMENT_HOBBY, CategoryTypes.EXPENSES);
        categories.add(notToModify);

        toModifyTo = constructCategory(SECategories.Codes.EXPENSES_FOOD_OTHER, CategoryTypes.EXPENSES);
        categories.add(toModifyTo);
        leafNodeCategory = constructCategory(SECategories.Codes.EXPENSES_FOOD_OTHER.replace(".other", ""),
                CategoryTypes.EXPENSES);
        categories.add(leafNodeCategory);
        
        for (Category c : categories) {
            Mockito.when(categoryRepository.findById(c.getId())).thenReturn(c);
            Mockito.when(categoryRepository.findByCode(c.getCode())).thenReturn(c);
        }

        cleaner = new TransactionCleaner(categoryRepository);
    }
    
    @Test
    public void testCleaningCategories() {
        Assert.assertFalse(cleaner.getReplacementCategory(notToModify.getId()).isPresent());
        Assert.assertEquals(toModifyTo.getId(), cleaner.getReplacementCategory(leafNodeCategory.getId()).get().getId());
    }
    
    @Test
    public void testCleaningTransaction() {
        
        Transaction transaction = new Transaction();
        
        transaction.setCategory(notToModify);
        cleaner.clean(transaction);
        Assert.assertEquals(notToModify.getId(), transaction.getCategoryId()); // Unmodified
        
        transaction.setCategory(leafNodeCategory);
        cleaner.clean(transaction);
        Assert.assertEquals(toModifyTo.getId(), transaction.getCategoryId()); // Modified
        
    }
    
}
