package sparta.checkers.intents;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ProcessAndroidManifest {

    public static void main(String[] args) throws Exception {
        SAXParserFactory parserFactor = SAXParserFactory.newInstance();
        SAXParser parser = parserFactor.newSAXParser();
        SAXHandler handler = new SAXHandler();
        parser.parse(ClassLoader.getSystemResourceAsStream("AndroidManifest.xml"),
                handler);

        File file = new File("filters");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FileWriter fw;
        try {
            fw = new FileWriter(file.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (Component comp : handler.compList) {
                for(IntentFilter filter : comp.intentFilters) {
                    bw.write(filter.toString() + " -> " + comp.name + '\n');
                }
            }
            bw.close();
            fw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

/**
 * The Handler for SAX Events.
 */
class SAXHandler extends DefaultHandler {

    List<Component> compList = new ArrayList<>();
    Component comp = null;
    IntentFilter intentFilter = null;
    String content;

    @Override
    // Triggered when the start of tag is found.
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        switch (qName) {
        case "activity":
            comp = new Component();
            comp.name = attributes.getValue("android:name");
            break;
        case "service":
            comp = new Component();
            comp.name = attributes.getValue("android:name");
            break;
        case "receiver":
            comp = new Component();
            comp.name = attributes.getValue("android:name");
            break;
        case "intent-filter":
            intentFilter = new IntentFilter();
            break;
        case "action":
            intentFilter.addAction(attributes.getValue("android:name"));
            break;
        case "category":
            String category = attributes.getValue("android:name");
            if(!category.equals("android.intent.category.DEFAULT") && !category.equals("android.intent.category.LAUNCHER")) {
                intentFilter.addCategory(attributes.getValue("android:name"));
            }
            break;
        case "data":
            // MIMETYPE,<scheme>://<host>:<port>/<path>
            String data = "";
            String scheme = attributes.getValue("android:scheme");
            if (scheme != null) {
                data = scheme;
                String host = attributes.getValue("android:host");
                if (host != null) {
                    data = data + "://" + host;
                    String port = attributes.getValue("android:port");
                    if (port != null) {
                        data = data + ":" + port;
                        String path = attributes.getValue("android:path");
                        if (path != null) {
                            data = data + "/" + path;
                        }
                    }
                }
            }
            String mime = attributes.getValue("android:mimeType");
            if (mime != null) {
                data = mime + "," + data;
            }
            intentFilter.addData(data);
            break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName) {
        case "activity":
            compList.add(comp);
            comp = null;
            break;
        case "service":
            compList.add(comp);
            comp = null;
            break;
        case "receiver":
            compList.add(comp);
            comp = null;
            break;
        case "intent-filter":
            comp.addFilter(intentFilter);
            intentFilter = null;
            break;

        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        content = String.copyValueOf(ch, start, length).trim();
    }

}

class Component {

    String name;
    List<IntentFilter> intentFilters;

    @Override
    public String toString() {
        String output = name;
        for (IntentFilter filter : intentFilters) {
            output += "(" + filter + ",";
        }
        return output;
    }

    public Component() {
        intentFilters = new ArrayList<IntentFilter>();
    }

    public void addFilter(IntentFilter filter) {
        intentFilters.add(filter);
    }

}