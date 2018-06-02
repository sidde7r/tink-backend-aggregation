package se.tink.backend.system.cli.seeding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.lang.ObjectUtils;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CategoryRepository;
import se.tink.backend.core.Category;
import se.tink.backend.core.CategoryTypes;
import se.tink.backend.system.cli.ExcelUtils;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class UpdateCategoryTreeCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static LinkedList<Category> categoryStack = new LinkedList<Category>();
    private List<Category> categoriesFromFile = Lists.newArrayList();
    private List<Category> categories = Lists.newArrayList();
    private static final LogUtils log = new LogUtils(UpdateCategoryTreeCommand.class);
    private static ObjectMapper mapper = new ObjectMapper();

    public UpdateCategoryTreeCommand() {
        super("update-category-tree",
                "Takes the categories.xls and merge it to what is in db, without changing any IDs");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        try {

            CategoryRepository categoryRepository = serviceContext.getRepository(CategoryRepository.class);

            categories = categoryRepository.findAll();
            categoriesFromFile = readCategoriesFromFile();

            for (Category c : categoriesFromFile) {
                Category sameCategory = findSameCategory(c);
                if (sameCategory != null) {
                    // update if changed
                    if (hasChanged(c, sameCategory)) {
                        c.setId(sameCategory.getId());
                        c.setParent(sameCategory.getParent());
                        log.info("Updating category");
                        log.info("\told:\t" + mapper.writeValueAsString(sameCategory));
                        log.info("\tnew:\t" + mapper.writeValueAsString(c));
                        categoryRepository.save(c);
                    }
                } 
                else {
                    // new category, add
                    // find correct parentId
                    String parentId = c.getParent();
                    for (Category cat : categoriesFromFile) {
                        if (cat.getParent() != null && ObjectUtils.equals(cat.getParent(), parentId)) {
                            Category same = findSameCategory(cat);
                            
                            if (same == null) {
                                log.error("Could not find parent id for " + c.getCode());
                                break;
                            }
                            
                            c.setParent(same.getId());
                            log.info("Adding category");
                            log.info("\t" + mapper.writeValueAsString(c));
                            categoryRepository.save(c);
                            break;
                        }
                    }
                }
            }

        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasChanged(Category c, Category sameCategory) {
        if (c.getCode() != null && sameCategory.getCode() != null) {
            if (!c.getCode().equals(sameCategory.getCode())) {
                return true;
            }
        }
        if (c.getCode() == null && sameCategory.getCode() != null) {
            return true;
        }
        
        if (c.getCode() != null && sameCategory.getCode() == null) {
            return true;
        }
        
        return false;
    }

    private Category findSameCategory(Category c) {

        // define the equals properties here
        for (Category catOnDisk : categories) {
            if (ObjectUtils.equals(c.getSortOrder(), catOnDisk.getSortOrder())) {
                return catOnDisk;
            }
        }
        return null;
    }

    private List<Category> readCategoriesFromFile() throws BiffException, IOException {
        Workbook workbook = ExcelUtils.openWorkbook(Files.asByteSource(
                new File("data/seeding/categories.xls")).openStream());

        Sheet sheet = workbook.getSheet(0);

        String type = null;
        String typeName = null;
        String cat1 = null;
        String cat2 = null;

        int rows = sheet.getRows();

        for (int i = 1; i < rows; i++) {
            if (sheet.getCell(0, i).getContents().length() == 0 && sheet.getCell(1, i).getContents().length() == 0
                    && sheet.getCell(2, i).getContents().length() == 0) {
                continue;
            }

            int currentLevel = 0;

            Category c = new Category();

            if (sheet.getCell(0, i).getContents().length() > 0) {
                type = sheet.getCell(0, i).getContents().toUpperCase();
                typeName = sheet.getCell(1, i).getContents();
                cat1 = null;
                cat2 = null;
                currentLevel = 0;
            }

            else if (sheet.getCell(1, i).getContents().length() > 0) {
                cat1 = sheet.getCell(1, i).getContents();
                cat2 = null;
                currentLevel = 1;
            }

            else if (sheet.getCell(2, i).getContents().length() > 0) {
                cat2 = sheet.getCell(2, i).getContents();
                currentLevel = 2;
            }

            if (sheet.getCell(4, i).getContents().length() > 0) {
                c.setCode(sheet.getCell(4, i).getContents());
                c.setDefaultChild(Boolean.parseBoolean(sheet.getCell(3, i).getContents()));
            }
            if (sheet.getCell(5, i).getContents().length() > 0) {
                String sortOrder = sheet.getCell(5, i).getContents();
                c.setSortOrder(Integer.valueOf(sortOrder));
            }
            if (sheet.getCell(7, i).getContents().length() > 0) {
                String searchTerms = sheet.getCell(7, i).getContents();
                c.setSearchTerms(searchTerms);
            }

            c.setType(CategoryTypes.valueOf(type));
            c.setTypeName(typeName);
            c.setPrimaryName(cat1);
            c.setSecondaryName(cat2);

            if (categoryStack.size() == 0) {
                categoriesFromFile.add(c);
                categoryStack.push(c);
            } else {
                int previousLevel = getLevel(categoryStack.peek());

                while (categoryStack.size() >= 1 && previousLevel >= currentLevel) {
                    categoryStack.pop();
                    previousLevel = getLevel(categoryStack.peek());
                }

                if (categoryStack.size() != 0) {
                    c.setParent(categoryStack.peek().getId());
                }

                categoriesFromFile.add(c);
                categoryStack.push(c);
            }

            log.info(indent(currentLevel) + mapper.writeValueAsString(c));
        }

        workbook.close();
        return categoriesFromFile;

    }

    private static String indent(int level) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < level; i++) {
            builder.append("\t\t");
        }

        return builder.toString();
    }

    public static int getLevel(Category c) {
        if (c == null) {
            return 0;
        } else if (c.getSecondaryName() != null) {
            return 2;
        } else if (c.getPrimaryName() != null) {
            return 1;
        } else {
            return 0;
        }
    }

}
