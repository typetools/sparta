package sparta.checkers.intents.componentmap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
        FileInputStream fis = new FileInputStream(args[0]);
        parser.parse(fis,handler);

        File file = new File(args[1]);
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
                    String temp = comp.name;
                    if(filter.hasURI) {
                        temp = "1" + comp.name; // Appending 1 to the start of component name to identify components with URIs.
                    }
                    bw.write(filter.toString() + " -> " + temp + '\n');
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
    String pckg = "";

    @Override
    // Triggered when the start of tag is found.
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        switch (qName) {

        case "manifest":
            pckg = attributes.getValue("package");
            break;

        case "activity-alias":
            comp = new Component();
            comp.name = attributes.getValue("android:targetActivity");
            if(comp.name.startsWith(".")) {
                comp.name = pckg + comp.name;
            }
            break;

        case "activity":
            comp = new Component();
            comp.name = attributes.getValue("android:name");
            if(comp.name.startsWith(".")) {
                comp.name = pckg + comp.name;
            }
            break;
        case "service":
            comp = new Component();
            comp.name = attributes.getValue("android:name");
            if(comp.name.startsWith(".")) {
                comp.name = pckg + comp.name;
            }
            break;
        case "receiver":
            comp = new Component();
            comp.name = attributes.getValue("android:name");
            if(comp.name.startsWith(".")) {
                comp.name = pckg + comp.name;
            }
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
                intentFilter.addCategory(category);
            }
            break;
        case "data":
            intentFilter.setHasURI(true);
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

        default:
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