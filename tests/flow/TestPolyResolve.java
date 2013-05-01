import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import java.io.*;


import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks.FlowSink;

//@skip-test
// TODO: method HttpClient.execute is no longer annotated as polymorphic.
// Therefore, that method invocation fails.
// Write your own little test classes to illustrate the point.
class TestPolyConstructors {
    @Sources(FlowSource.LOCATION) HttpResponse response;
    @Sources(FlowSource.LOCATION) HttpEntity entity;
    @Sources(FlowSource.LOCATION) InputStream tempStream;
    Reader responseReader;

    void sdf(@Sinks(FlowSink.NETWORK) @Sources(FlowSource.LOCATION) HttpGet request) {
        try {
            HttpClient client = new DefaultHttpClient();
            response = client.execute(request);
            entity = response.getEntity();
            tempStream = entity.getContent();
            responseReader = new InputStreamReader(tempStream);
        } catch(Exception e) {}
    }
}