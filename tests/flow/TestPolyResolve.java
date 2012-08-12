import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import java.io.*;


import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;


class TestPolyConstructors {
    @FlowSources(FlowSource.LOCATION) HttpResponse response;
    @FlowSources(FlowSource.LOCATION) HttpEntity entity;
    @FlowSources(FlowSource.LOCATION) InputStream tempStream;
    Reader responseReader;

    void sdf(@FlowSinks(FlowSink.NETWORK) @FlowSources(FlowSource.LOCATION) HttpGet request) {
        try {
            HttpClient client = new DefaultHttpClient();
            response = client.execute(request);
            entity = response.getEntity();
            tempStream = entity.getContent();
            responseReader = new InputStreamReader(tempStream);
        } catch(Exception e) {}
    }
}