package sparta.checkers.intents;
import java.util.ArrayList;
import java.util.List;

public class IntentFilter {

    List<String> action;
    List<String> categories;
    List<String> data;

    public IntentFilter() {
        action = new ArrayList<String>();
        categories = new ArrayList<String>();
        data = new ArrayList<String>();
    }

    public void setAction(List<String> action) {
        this.action = action;
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
        if (data.size() > 0) {
            output = output + ",";
            for (String data : this.data) {
                output += data + "|";

            }
            output = output.substring(0, output.length() - 1);
        }
        output = "(" + output + ")";
        return output;
    }

}
