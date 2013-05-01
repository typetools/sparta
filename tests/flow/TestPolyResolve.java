import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import java.io.*;


import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.Sinks.SPARTA_Permission;

//@skip-test
// TODO: method HttpClient.execute is no longer annotated as polymorphic.
// Therefore, that method invocation fails.
// Write your own little test classes to illustrate the point.
class TestPolyConstructors {
    @Sources(SPARTA_Permission.LOCATION) HttpResponse response;
    @Sources(SPARTA_Permission.LOCATION) HttpEntity entity;
    @Sources(SPARTA_Permission.LOCATION) InputStream tempStream;
    Reader responseReader;

    void sdf(@Sinks(SPARTA_Permission.NETWORK) @Sources(SPARTA_Permission.LOCATION) HttpGet request) {
        try {
            HttpClient client = new DefaultHttpClient();
            response = client.execute(request);
            entity = response.getEntity();
            tempStream = entity.getContent();
            responseReader = new InputStreamReader(tempStream);
        } catch(Exception e) {}
    }
}