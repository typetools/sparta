package sparta.checkers.intents.componentmap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ProcessEpicOutput {
   

    static File componentMapFile;
    static List<IntentFilter> filters;
    static File file;
    static FileWriter fw;
    static HashMap<String,Set<String>> componentMap;
    static HashMap<String,Set<String>> componentMapURI;
    
    static void matchFilters(String component, String filter, String filtersPath) {
        BufferedReader bufferedReaderFilters = null;
        String currentLine;
        try {
            bufferedReaderFilters = new BufferedReader(new FileReader(filtersPath));
            currentLine = bufferedReaderFilters.readLine();
            while(currentLine != null) {
                String[] filterToComponent = currentLine.split(" ");
                if(filterToComponent[0].equals(filter)) {
                    if(filterToComponent[filterToComponent.length-1].startsWith("1")) {
                        addCMEntryURI(component+filter,filterToComponent[filterToComponent.length-1],currentLine);
                    } else {
                        addCMEntry(component+filter,filterToComponent[filterToComponent.length-1]);
                    }
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
    
    private static void addCMEntry(String component, String receiver) {
        Set<String> receivers = null;
        if(componentMap.containsKey(component)) {
            receivers = componentMap.get(component);
        } else {
            receivers = new HashSet<String>();
        }
        receivers.add(receiver);
        componentMap.put(component, receivers);
    }
    
    private static void addCMEntryURI(String component, String receiver, String filter) {
        Set<String> receivers = null;
        if(componentMapURI.containsKey(component)) {
            receivers = componentMapURI.get(component);
        } else {
            receivers = new HashSet<String>();
        }
        receiver = receiver.substring(1,receiver.length()); // Removing appended 1 to identify URIs filters.
        String[] uri = filter.split(":URI:");
        if(uri.length > 1) {
            receiver += "(" + uri[1].split(" ->")[0] + ")";
        }
        receivers.add(receiver);
        componentMapURI.put(component, receivers);
    }

    static void readFile(String epiccOutputPath, String filtersPath, String cmPath) {
        filters = new ArrayList<IntentFilter>();
        file = new File(cmPath);
        if(file.exists()) {
            file.delete();
        }
        BufferedReader bufferedReaderEpicc = null;
        try {
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
                    String nextLine = bufferedReaderEpicc.readLine();
                    if(nextLine != null && nextLine.startsWith("Type:")) {
                        while(nextLine != null && !nextLine.startsWith("Intent Filter")) {
                            nextLine = bufferedReaderEpicc.readLine();
                        }
                    }
                    String filter = processFilter(bufferedReaderEpicc.readLine()
                            .trim(), "", component);
                    matchFilters(component, filter, filtersPath);
                }
                originalLine = bufferedReaderEpicc.readLine();
            }
            file.createNewFile();
            fw = new FileWriter(file.getAbsoluteFile());
            writeLines();
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

    static String processFilter(String filter, String output, String component) {
        if (filter == null || filter.length() <= 1)
            return "(" + output + ")";
        if (filter.startsWith("Action") || filter.startsWith("Actions")) {
            filter = filter.substring(7, filter.length()).trim();
            String action = filter.split(",")[0];
            output = action;
            return processFilter(filter.substring(action.length()+1).trim(),
                    output,component);
        } else if (filter.startsWith("Categories")) {
            filter = filter.substring(10, filter.length()).trim();
            String categories = filter.split("]")[0];
            categories = categories.substring(1).replace(',', '|');
            output = output + categories + ",";
            return processFilter(filter.substring(categories.length() + 2)
                    .trim(), output,component);
            //Commented out since Epicc currently does not handle URIs.
//        } else if (filter.startsWith("Data")) {
//            filter = filter.substring(5, filter.length()).trim();
//            String data = filter.split(",")[0];
//            output = output + data + ",";
//            return processFilter(filter.substring(data.length()).trim(), output,component);
        } else if (filter.startsWith("Package")) {
            filter = filter.substring(8, filter.length()).trim();
            String package_ = filter.split(" ")[0];
            filter = filter.substring(package_.length()).trim();
            filter = filter.replace("/", ".");
            return processFilter(filter, output,component);
        } else if (filter.startsWith("Class")) {
            filter = filter.substring(6, filter.length()).trim();
            String clazz = filter.split(" ")[0];
            clazz = clazz.substring(0,clazz.length()-1);
            addCMEntry(component, clazz);
            return clazz;
        } else if (filter.startsWith("Extras")) {
            return "(" + output + ")";
        } else if (filter.startsWith("Flags")) {
            return "(" + output + ")";
        }else {
            throw new RuntimeException("Unrecognized String: " + filter);
        }
    }
    
    static void writeLines() {
        try {
            for(String component : componentMap.keySet()) {
                Set<String> receivers = componentMap.get(component);
                
                if(componentMapURI.containsKey(component)) {
                    Set<String> receiversURI = componentMapURI.get(component);
                    receiversURI.addAll(receivers);
                    componentMapURI.put(component, receiversURI);
                } else {
                    String receiversString = receivers.toString();
                    receiversString = receiversString.substring(1,receiversString.length()-1); // Removing [] from set
                    fw.write(component + " -> " + receiversString);
                    fw.write("\n");
                }
            }
            if(!componentMapURI.isEmpty()) {
                fw.write("\n");
                fw.write("#The following communication occurs with uses of URIs \n");
                fw.write("#Please verify the code manually \n");
            }
            
            for(String component : componentMapURI.keySet()) {
                String receivers = componentMapURI.get(component).toString();
                receivers = receivers.substring(1,receivers.length()-1); // Removing [] from set
                for(String receiver : receivers.split(",")) {
                    fw.write("# "+ component + " -> " + receiver);
                    fw.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        componentMap = new HashMap<String,Set<String>>();
        componentMapURI = new HashMap<String,Set<String>>();
        readFile(args[0],args[1],args[2]);
    }

}
