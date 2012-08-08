import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import java.io.*;


import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;


class TestPolyResolve {
    float x1 = 2f;
    float factor;

    /** Ensure that the LUB of PolyFlowSource PolyFlowSink with unannotated
     * gives the right thing.
     */
    void mod2pi(@PolyFlowSources @PolyFlowSinks float x) {
        factor = x / x1;
    }
}

class TestPolyConstructors {
	private @FlowSources(FlowSource.LOCATION) HttpResponse response;
	private @FlowSources(FlowSource.LOCATION) HttpEntity entity;
	private @FlowSources(FlowSource.LOCATION) InputStream tempStream;
	private Reader responseReader;
	
	void sdf(@FlowSinks(FlowSink.NETWORK) @FlowSources(FlowSource.LOCATION) HttpGet request) {
		HttpClient client = new DefaultHttpClient();
		response = client.execute(request);
		entity = response.getEntity();
		tempStream = entity.getContent();
		responseReader = new InputStreamReader(tempStream);
	}
}