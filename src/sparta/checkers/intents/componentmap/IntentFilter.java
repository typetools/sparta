package sparta.checkers.intents.componentmap;
import java.util.ArrayList;
import java.util.List;

public class IntentFilter {

    List<String> action;
    List<String> categories;
    List<String> data;
    boolean hasURI = false;

    public IntentFilter() {
        action = new ArrayList<String>();
        categories = new ArrayList<String>();
        data = new ArrayList<String>();
    }

    public void setAction(List<String> action) {
        this.action = action;
    }
    
    public boolean hasAction(String action) {
        return this.action.contains(action);
    }
    
    public void addAction(String action) {
        this.action.add(action);
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void addCategory(String cat) {
        categories.add(cat);
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    public void addData(String data) {
        this.data.add(data);
    }
    
    public void setHasURI(boolean b) {
        hasURI = b;
    }

    @Override
    public String toString() {
        String output = "";
        if (action.size() > 0) {
            for (String action : this.action) {
                output += action + "|";

            }
            output = output.substring(0, output.length() - 1);
        }
        if (categories.size() > 0) {
            output = output + ",";
            for (String category : this.categories) {
                output += category + "|";

            }
            output = output.substring(0, output.length() - 1);
        }
        String URIContent = "";
        if (data.size() > 0) {
            for (String data : this.data) {
                URIContent += data + "|";

            }
            URIContent = URIContent.substring(0, URIContent.length() - 1);
            URIContent = URIContent.replace(",", "/");
        }
        
        output = "(" + output + ")";
        if(hasURI) {
            output += " :URI:" + URIContent;
        }
        return output;
    }

}
