import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import java.io.*;


import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;
import sparta.checkers.quals.SpartaPermission;

//@skip-test
// TODO: method HttpClient.execute is no longer annotated as polymorphic.
// Therefore, that method invocation fails.
// Write your own little test classes to illustrate the point.
class TestPolyConstructors {
    @Sources(SpartaPermission.LOCATION) HttpResponse response;
    @Sources(SpartaPermission.LOCATION) HttpEntity entity;
    @Sources(SpartaPermission.LOCATION) InputStream tempStream;
    Reader responseReader;

    void sdf(@Sinks(SpartaPermission.INTERNET) @Sources(SpartaPermission.LOCATION) HttpGet request) {
        try {
            HttpClient client = new DefaultHttpClient();
            response = client.execute(request);
            entity = response.getEntity();
            tempStream = entity.getContent();
            responseReader = new InputStreamReader(tempStream);
        } catch(Exception e) {}
    }
}