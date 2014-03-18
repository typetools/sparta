package sparta.checkers.intents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ComponentMap {

	public static final String COMPONENT_MAP_FILE_OPTION = "componentMap";
	
    public static final String EMPTY = "{}";
    public static final String EMPTY_REGEX = "\\{\\}";

    private final Map<String, Set<String>> componentMap;

    public ComponentMap(final String filename) {
        if (filename != null) {
            File componentMapFile = new File(filename);
            componentMap = new HashMap<String, Set<String>>();
            if (componentMapFile != null && componentMapFile.exists()) {
                readComponentMapFile(componentMapFile);
            }
        } else {
            componentMap = new HashMap<String, Set<String>>();
        }
    }
   
    public static String stripComment(final String input) {
        int commentIndex = input.indexOf('#');

        final String out;
        if (commentIndex > -1) {
            if (commentIndex == 0) {
                out = "";
            } else {
                out = input.substring(0, commentIndex).trim();
            }
        } else {
            out = input;
        }

        return out;
    }


    private void readComponentMapFile(final File mapFile) {

        final Pattern linePattern = Pattern.compile("^\\s*((?:\\S+|" + 
            EMPTY_REGEX + "))\\s*->\\s*((?:\\S+)(?:\\s*,\\s*\\S+)*)\\s*$");

        final List<String> errors = new ArrayList<String>();

        BufferedReader bufferedReader = null;
        try {
            int lineNum = 1;
            bufferedReader = new BufferedReader(new FileReader(mapFile));
            String originalLine = bufferedReader.readLine();
            
            while (originalLine != null) {
                originalLine = originalLine.trim();
                // Remove anything from # on in the line
                final String line = stripComment(originalLine);

                if (!line.isEmpty() && !isWhiteSpaceLine(line)) {
                    final Matcher matcher = linePattern.matcher(line);

                    Set<String> receivers = new HashSet<String>();
                    if (matcher.matches()) {
                        final String senderStr = matcher.group(1).trim();
                        final String[] receiversStrs = matcher.group(2).split(",");
                        if (senderStr.equals(EMPTY)) {
                            errors.add(formatComponentMapError(mapFile, 
                                lineNum, "Sender missing.", originalLine));
                            receivers = null;

                        } else {
                            try {
                                for(String s : receiversStrs) {
                                    s = s.trim();
                                    receivers.add(s);
                                }
                                if (receivers != null && !receivers.isEmpty()) {
                                    if(componentMap.containsKey(senderStr)) {
                                        receivers.addAll(componentMap.get(senderStr));
                                    }
                                    componentMap.put(senderStr, receivers);
                                }
                            } catch (final IllegalArgumentException iaExc) {
                                errors.add(formatComponentMapError(mapFile, lineNum,
                                    "Unrecognized class: " + iaExc.getMessage(),
                                    originalLine));
                                receivers = null;
                            }
                        }

                    } else {
                        errors.add(formatComponentMapError(
                                mapFile,
                                lineNum,
                                "Syntax error, Lines are of the form: Sender -> Receiver1, Receiver2, ..., ReceiverN ",
                                originalLine));
                    }
                }

                ++lineNum;
                originalLine = bufferedReader.readLine();
            }

        } catch (final IOException ioExc) {
            throw new RuntimeException(ioExc);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException ignoredCloseExc) {
            }
        }

        if (!errors.isEmpty()) {
            System.out.println("\nErrors parsing map file:");
            for (final String error : errors) {
                System.out.println(error);
                System.out.println();
            }

            System.out.flush();
            throw new RuntimeException("Errors parsing map file: "
                    + mapFile.getAbsolutePath());
        }
    }

    private static final Pattern WHITE_SPACE_PATTERN = Pattern.compile("^\\s*$");

    public static boolean isWhiteSpaceLine(final String line) {
        return WHITE_SPACE_PATTERN.matcher(line).matches();
    }

    private static String formatComponentMapError(final File file, final int lineNum,
            final String message, final String line) {
        return file.getAbsolutePath() + ":" + lineNum + ": " + message + "\n" + line;
    }
    
    public Map<String, Set<String>> getIntentMap() {
        return componentMap;
    }

  }
