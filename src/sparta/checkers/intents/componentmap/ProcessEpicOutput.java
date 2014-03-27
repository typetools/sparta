package sparta.checkers.intents.componentmap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    static HashMap<String,List<String>> receivers;
    static HashMap<String,List<String>> senderFilterMap;
    static List<String> unresolvedIntents;
    
    static String UNKNOWN = "unknown";
    
    static void matchFilters(String component, IntentFilter filter, String filtersPath) {
        
        List<String> actions = filter.action;
        for(String action : actions) {
            List<String> temp;
            if(senderFilterMap.containsKey(action)) {
                temp = senderFilterMap.get(action);
            } else {
                temp = new ArrayList<String>();
            }
            temp.add(component);
            senderFilterMap.put(action, temp);
        }
        
        BufferedReader bufferedReaderFilters = null;
        String currentLine;
        boolean found = false;
        try {
            bufferedReaderFilters = new BufferedReader(new FileReader(filtersPath));
            currentLine = bufferedReaderFilters.readLine();
            while(currentLine != null) {
                String[] filterToComponent = currentLine.split(" ");
                if(filterToComponent[0].contains("|")) {
                    //handling several actions case
                    String[] filterActions = filterToComponent[0].split("|");
                    filterActions[0] += ")";
                    filterActions[filterActions.length-1] = "(" + filterActions[filterActions.length-1];
                    for(int i = 1; i < filterActions.length-1; i++) {
                        filterActions[i] = "(" + filterActions[i] + ")";
                    }
                    for(int i = 0; i < filterActions.length; i++) {
                        if(filterActions[i].equals(filter.toString())) {
                            found = true;
                            if(filterToComponent[filterToComponent.length-1].startsWith("1")) {
                                addCMEntryURI(component,filterToComponent[filterToComponent.length-1],currentLine);
                            } else {
                                addCMEntry(component,filterToComponent[filterToComponent.length-1]);
                            }
                        }
                    }
                    
                }
                else if(filterToComponent[0].equals(filter.toString())) {
                    found = true;
                    if(filterToComponent[filterToComponent.length-1].startsWith("1")) {
                        addCMEntryURI(component,filterToComponent[filterToComponent.length-1],currentLine);
                    } else {
                        addCMEntry(component,filterToComponent[filterToComponent.length-1]);
                    }
                }
                currentLine = bufferedReaderFilters.readLine();
            }
            if(!found) {
                if(!unresolvedIntents.contains(component)) {
                    unresolvedIntents.add(component);
                }
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
    
    private static String bytecodeDescriptorToSimpleJava(String descriptor) {
        String output = "";
        for(int i = 0; i < descriptor.length(); i++) {
            char c = descriptor.charAt(i);
            switch(c) {
                case 'B':
                    output += "byte,";
                    break;
                case 'C':
                    output += "char,";
                    break;
                case 'D':
                    output += "double,";
                    break;
                case 'F':
                    output += "float,";
                    break;
                case 'I':
                    output += "int,";
                    break;
                case 'J':
                    output += "long,";
                    break;
                case 'S':
                    output += "short,";
                    break;
                case 'Z':
                    output += "boolean,";
                    break;
                case '[':
                    break;
                case ']':
                    break;
                case 'L':
                    StringBuffer temp = new StringBuffer();
                    i++;
                    while(i < descriptor.length() && descriptor.charAt(i) != ';') {
                        temp.append(descriptor.charAt(i++));
                    }
                    output += temp + ",";
                    break;
                case ')':
                    continue;
                default:
                    System.err.println("Unrecognized char: " + c + "; in descriptor: " + descriptor );
                    break;    
            }
            
        }
        if(output.length() == 0) {
            return ")";
        }
        return output.substring(0,output.length()-1) + ")";
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
            boolean doesntBreakLine = false;
            while (originalLine != null) {
                doesntBreakLine = false;
                originalLine = originalLine.trim();
                if (originalLine.startsWith("-")) {
                    String component = originalLine.substring(2)
                            .replaceAll("\\/", "\\.").replaceAll("(\\[)(.*);", "$2[];");
                    int openParanthesisIndex = component.indexOf('(');
                    String simpleJava = bytecodeDescriptorToSimpleJava(component.substring(openParanthesisIndex+1));
                    component = component.substring(0,openParanthesisIndex+1) + simpleJava;
//                    String component = originalLine.split("\\(")[0];
//                    String[] componentFullPath = component.split("/");
//                    component = componentFullPath[componentFullPath.length - 2];
//                    if(component.contains("$")) {
//                        int index = component.indexOf('$');
//                        component = component.substring(0,index);
//                    }
                    
                    String nextLine = bufferedReaderEpicc.readLine();
                    
                    while(nextLine != null && nextLine.length() > 0) {
                      //BRreceivers are not correctly identified.  
                        if(nextLine.startsWith("Type: android.content.BroadcastReceiver")) {
                            while(nextLine != null && nextLine.length() > 0) {
                                if(nextLine.startsWith("Actions:")) {
                                    int startActions = nextLine.indexOf("[");
                                    int endActions = nextLine.indexOf("]");
                                    if(startActions != -1 && endActions != -1) {
                                        String[] actions = nextLine.substring(startActions+1,endActions).split(",");
                                        for(String action : actions) {
                                            action = action.trim();
                                            List<String> temp;
                                            if(receivers.containsKey(action)) {
                                                temp = receivers.get(action);
                                            } else {
                                                temp = new ArrayList<String>();
                                            }
                                            temp.add(component);
                                            receivers.put(action, temp);
                                        }
                                    }
                                }
                                nextLine = bufferedReaderEpicc.readLine();
                            }
                            break; 
                        }
                        
                        if(nextLine.startsWith("No field set") || nextLine.startsWith("No value found.") || 
                                nextLine.startsWith("Found top element")) {
                            nextLine = bufferedReaderEpicc.readLine();
                            if(!unresolvedIntents.contains(component)) {
                                unresolvedIntents.add(component);
                            }
                            continue;
                        }
                        
                        if(nextLine.startsWith("No permission") || nextLine.startsWith("Possible permissions") ||
                                nextLine.startsWith("Intent value") || nextLine.startsWith("Intent Filter") || 
                                nextLine.startsWith("Type:")) {
                            nextLine = bufferedReaderEpicc.readLine();
                            continue;
                        }
                        if(nextLine.trim().startsWith("-")) {
                            originalLine = nextLine;
                            doesntBreakLine = true;
                            break;
                        }
                        IntentFilter filter = processFilter(nextLine.trim(), null, component);
                        if(filter != null) {
                            matchFilters(component, filter, filtersPath);
                        }
                        nextLine = bufferedReaderEpicc.readLine();
                        
                    }
                    
                }
                if(!doesntBreakLine) {
                    originalLine = bufferedReaderEpicc.readLine();
                }
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

    static IntentFilter processFilter(String filter, IntentFilter output, String component) {
        if(output == null) {
            output = new IntentFilter();
        }
        if (filter == null || filter.length() <= 1) {
            return output;
        }
        filter = filter.trim();
        if (filter.startsWith("Action")) {
            filter = filter.substring(7, filter.length()).trim();
            String action = filter.split(",")[0];
            output.addAction(action);
            return processFilter(filter.substring(action.length()+1).trim(),
                    output,component);
        } else if (filter.startsWith("Actions")) {
            //Should only happen in broadcastreceiveirs
            filter = filter.substring(8, filter.length()).trim();
            String action = filter.split(",")[0];
            output.addAction(action);
            return processFilter(filter.substring(action.length()+1).trim(),
                    output,component);
        } else if (filter.startsWith("Categories")) {
            filter = filter.substring(10, filter.length()).trim();
            String categories = filter.split("]")[0];
            categories = categories.substring(1).replace(',', '|');
            output.addCategory(categories);
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
            return null;
        } else if (filter.startsWith("Extras")) {
            return output;
        } else if (filter.startsWith("Flags")) {
            return output;
        } else if (filter.startsWith("Type")) {
            filter = filter.substring(5, filter.length()).trim();
            String type = filter.split(",")[0];
            return processFilter(filter.substring(type.length()+1)
                    .trim(), output,component);
        } else if (filter.startsWith(",")) {
            return processFilter(filter.substring(1), output, component);
        } else {
//            throw new RuntimeException("Unrecognized String: " + filter);
            System.err.println("Unrecognized String: " + filter);
            return output;
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
            
            if(!receivers.isEmpty()) {
                fw.write("\n");
                fw.write("#Broadcast Receivers: \n");
                        
                for(String action : receivers.keySet()) {
                    
                    //senderFilterMap: action -> list<components>
                    //receivers : action -> list<whereComponenets>
                    List<String> whereComponents = receivers.get(action);
                    List<String> senderComponents = senderFilterMap.get(action);
                    
                    if(senderComponents == null || senderComponents.size() == 0) {
                        fw.write("#" + UNKNOWN + " -> " + "BroadcastReceiver registered in " + whereComponents.toString() );
                        fw.write("\n");
                        fw.write(UNKNOWN + " -> " + UNKNOWN);
                        fw.write("\n");
                        continue;
                    } else {
                        for(String sender : senderComponents) {
                            fw.write("#" + sender + " -> " + "BroadcastReceiver registered in " + whereComponents.toString() );
                            fw.write("\n");
                            fw.write(sender + " -> " + UNKNOWN);
                            fw.write("\n");
                        }
                    }
                }
            }
            
            if(!unresolvedIntents.isEmpty()) {
                fw.write("\n");
                fw.write("#Unresolved Intents:");
                fw.write("\n");
                for(String component : unresolvedIntents) {
                    fw.write(component + " -> " + UNKNOWN);
                    fw.write("\n");
                }
            }
            
            if(!componentMapURI.isEmpty()) {
                fw.write("\n");
                fw.write("#The following communication occurs with uses of URIs \n");
                fw.write("#Please verify the code manually \n");
                
                for(String component : componentMapURI.keySet()) {
                    String receivers = componentMapURI.get(component).toString();
                    receivers = receivers.substring(1,receivers.length()-1); // Removing [] from set
                    for(String receiver : receivers.split(",")) {
                        fw.write("# "+ component + " -> " + receiver);
                        fw.write("\n");
                    }
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        componentMap = new HashMap<String,Set<String>>();
        componentMapURI = new HashMap<String,Set<String>>();
        receivers = new HashMap<String,List<String>>();
        senderFilterMap = new HashMap<String,List<String>>();
        unresolvedIntents = new ArrayList<String>();
        readFile(args[0],args[1],args[2]);
    }

}
