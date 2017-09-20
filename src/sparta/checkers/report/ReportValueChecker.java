package sparta.checkers.report;

import static sparta.checkers.FlowChecker.SPARTA_OUTPUT_DIR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.common.value.ValueChecker;

public class ReportValueChecker extends ValueChecker {
    public static final String URL_REGEX = "https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    public static  final String FILE_NAME_REGEX = ".*\\.[a-z]+";
    public static  final String FILE_PATH_REGEX = ".*/.*";
    public static  final String CONTENT_REGEX = "content:.*";
    public static  final String CLASSNAME_REGEX = "(android|java|com|org)\\.[a-zA-Z$0-9\\._]+";
    public static final String MESSAGE_REGEX = ".* .*";

    public static final List<Category> CATEGORIES = new ArrayList<Category>();
    private static final String OUTPUT_FILENAME = SPARTA_OUTPUT_DIR+"found-strings.txt";
    {
        //Order in which to test the regexs
        //Order matters because if a string matches more than one category
        //it is assigned to the first matching category
        CATEGORIES.add(new Category("URL", URL_REGEX, "found.url"));
        CATEGORIES.add(new Category("Content", CONTENT_REGEX, "found.content"));
        CATEGORIES.add(new Category("Class name", CLASSNAME_REGEX, "found.classname"));
        CATEGORIES.add(new Category("File name", FILE_NAME_REGEX, "found.filename"));
        CATEGORIES.add(new Category("File path", FILE_PATH_REGEX, "found.filepath"));
        CATEGORIES.add(new Category("SQL", "", "found.sql"){
            @Override
            public boolean match(String string) {
                String[] sqlKeyWords = { "delete", "from", "having", "insert", "join", "select", "merge", "order by",
                        "where", "table", "?", "index", "exists ", "drop", "lower", "like", "create", "= ?",
                        "collate", "localized", "asc"};
                int matches = 0;
                for(String keywork: sqlKeyWords){
                    if(string.toLowerCase().contains(keywork)){
                        matches++;
                    }
                }
                boolean found =  (matches>1) && string.contains(" ");
                if (found) this.found.add(string);
                return found;
            }
        });
        CATEGORIES.add(new Category("Message", MESSAGE_REGEX, "found.message"));
        CATEGORIES.add(new Category("No Category", ".*", "found.nocategory"));
    }

    private FileOutputStream writer;

    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new ReportValueVisitor(this);
    }

    @Override
    public void typeProcessingOver() {
        File outputDir = new File(SPARTA_OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
        if (outputDir.exists() && outputDir.isDirectory()) {
            try {
                writer = new FileOutputStream(OUTPUT_FILENAME);
                for (Category regex : CATEGORIES) {
                    if (!(regex.getFound().isEmpty()))
                        printStrings(regex.getFound(), regex.getName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.typeProcessingOver();
    }

    private void printStrings(Set<String> strings, String header) {
        write("\n#"+header);
        for (String s : strings) {

            if (s.trim().length() > 0)
                write(s);
        }

    }

    private void write(String string) {
        try {
            writer.write(string.getBytes());
            writer.write("\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class Category{
    //name, regex, set of found
    final String name;
    final String regex;
    final String errorKey;
    final Set<String> found;
    public Category(String name, String regex, String errorKey) {
        super();
        this.name = name;
        this.regex = regex;
        this.errorKey = errorKey;
        this.found = new HashSet<>();
    }
    public String getName() {
        return name;
    }
    public String getErrorKey() {
        return errorKey;
    }
    public Set<String> getFound() {
        return found;
    }
    public boolean match(String string){
        if (Pattern.matches(regex, string)) {
            found.add(string);
            return true;
        }
        return false;
    }
}
