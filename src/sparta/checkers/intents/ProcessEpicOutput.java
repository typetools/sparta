package sparta.checkers.intents;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProcessEpicOutput {
    public static void main(String[] args) {
        readFile("epic_output.txt","filters");
    }

    static File componentMap;
    static List<IntentFilter> filters;
    static File file;
    static FileWriter fw;
    
    static void matchFilters(String component, String filter, String filtersPath) {
        BufferedReader bufferedReaderFilters = null;
        String currentLine;
        try {
            bufferedReaderFilters = new BufferedReader(new FileReader(filtersPath));
            currentLine = bufferedReaderFilters.readLine();
            while(currentLine != null) {
                String[] filterToComponent = currentLine.split(" ");
                if(filterToComponent[0].equals(filter)) {
                    fw.write(component + " -> " + filterToComponent[2] + '\n');
                }
                currentLine = bufferedReaderFilters.readLine();
            }
            bufferedReaderFilters.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void readFile(String epiccOutputPath, String filtersPath) {
        filters = new ArrayList<IntentFilter>();
        file = new File("ComponentMap");
        if(file.exists()) {
            file.delete();
        }
        BufferedReader bufferedReaderEpicc = null;
        try {
            file.createNewFile();
            fw = new FileWriter(file.getAbsoluteFile());
            bufferedReaderEpicc = new BufferedReader(new FileReader(epiccOutputPath));
            String originalLine = bufferedReaderEpicc.readLine().trim();
            while (originalLine != null && !originalLine.startsWith("The following ICC")) {
                originalLine = bufferedReaderEpicc.readLine();
            }
            while (originalLine != null) {
                originalLine = originalLine.trim();
                if (originalLine.startsWith("-")) {
                    String component = originalLine.split("\\(")[0];
                    String[] componentFullPath = component.split("/");
                    component = componentFullPath[componentFullPath.length - 2];
                    if(component.contains("$")) {
                        int index = component.indexOf('$');
                        component = component.substring(0,index);
                    }
                    bufferedReaderEpicc.readLine();
                    String filter = processFilter(bufferedReaderEpicc.readLine()
                            .trim(), "");
                    matchFilters(component, filter, filtersPath);
                }
                originalLine = bufferedReaderEpicc.readLine();
            }
            bufferedReaderEpicc.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Consume string on the format: Action: act, Categories: [cat1,cat2], Data:
     * data, Package: pack, Extras: [value1,value2],
     * 
     * @param filter
     * @param output
     * @return (act,cat1|cat2,data)
     */

    static String processFilter(String filter, String output) {
        if (filter == null || filter.length() <= 1)
            return "(" + output + ")";
        if (filter.startsWith("Action")) {
            filter = filter.substring(7, filter.length()).trim();
            String action = filter.split(",")[0];
            output = action;
            return processFilter(filter.substring(action.length()+1).trim(),
                    output);
        } else if (filter.startsWith("Categories")) {
            filter = filter.substring(10, filter.length()).trim();
            String categories = filter.split("]")[0];
            categories = categories.substring(1).replace(',', '|');
            output = output + categories + ",";
            return processFilter(filter.substring(categories.length() + 2)
                    .trim(), output);
        } else if (filter.startsWith("Data")) {
            filter = filter.substring(5, filter.length()).trim();
            String data = filter.split(",")[0];
            output = output + data + ",";
            return processFilter(filter.substring(data.length()).trim(), output);
        } else if (filter.startsWith("Package")) {
            filter = filter.substring(8, filter.length()).trim();
            String package_ = filter.split(" ")[0];
            filter = filter.substring(package_.length()).trim();
            return processFilter(filter, output);
        } else if (filter.startsWith("Class")) {
            filter = filter.substring(6, filter.length()).trim();
            String clazz = filter.split(" ")[0];
            return clazz;
        } else if (filter.startsWith("Extras")) {
            return "(" + output + ")";
        } else if (filter.startsWith("Flags")) {
            return "(" + output + ")";
        }else {
            throw new RuntimeException("Unrecognized String: " + filter);
        }
    }

}
