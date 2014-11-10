import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import java.io.*;


import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import sparta.checkers.quals.FlowPermission;

//@skip-test
// TODO: method HttpClient.execute is no longer annotated as polymorphic.
// Therefore, that method invocation fails.
// Write your own little test classes to illustrate the point.
class TestPolyConstructors {
    @Source(FlowPermissionString.ACCESS_FINE_LOCATION) HttpResponse response;
    @Source(FlowPermissionString.ACCESS_FINE_LOCATION) HttpEntity entity;
    @Source(FlowPermissionString.ACCESS_FINE_LOCATION) InputStream tempStream;
    Reader responseReader;

    void sdf(@Sink(FlowPermissionString.INTERNET) @Source(FlowPermissionString.ACCESS_FINE_LOCATION) HttpGet request) {
        try {
            HttpClient client = new DefaultHttpClient();
            response = client.execute(request);
            entity = response.getEntity();
            tempStream = entity.getContent();
            responseReader = new InputStreamReader(tempStream);
        } catch(Exception e) {}
    }
}