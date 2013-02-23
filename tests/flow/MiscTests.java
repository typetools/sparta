import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowSources.FlowSource;
import sparta.checkers.quals.FlowSinks.FlowSink;

class MiscTests {
    double clean;

    void test() {
        // TODO: current flow inference stops as soon as the first qualifier hierarchy
        // has errors. Therefore, the sources are not propagated, resulting in
        // an assignment error. Let us work on this once the new flow is integrated.
        @SuppressWarnings("flow")
        @FlowSinks({FlowSink.NETWORK}) double lat = 1.0;
        //:: error: (assignment.type.incompatible)
        clean = lat;

        @SuppressWarnings("flow")
        @FlowSinks({FlowSink.NETWORK, FlowSink.CONDITIONAL}) @FlowSources(FlowSource.LITERAL) double lat2 = 1.0;
        clean = lat2;
    }

    @SuppressWarnings("flow")
    @FlowSinks({FlowSink.NETWORK, FlowSink.CONDITIONAL}) String WEBSERVICE_URL = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    @FlowSources({FlowSource.LITERAL, FlowSource.LOCATION}) String WEBSERVICE_URL2 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    String WEBSERVICE_URL3 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
    @FlowSources({FlowSource.LITERAL, FlowSource.LOCATION}) String result; //With FP effective FlowSinks(NETWORK,CONDITIONAL}

    @FlowSources({FlowSource.LITERAL}) @FlowSinks({FlowSink.CONDITIONAL}) String result2;

    @FlowSinks({FlowSink.NETWORK}) String result3;


    void test_StringFormat_StringFlowSink() {
        double lat = 1.0;
        double lon = 2.0;
        int days = 3;
        result = String.format(WEBSERVICE_URL, lat, lon, days);
    }

    void test_StringFormat_StringFlowSource() {
        double lat = 1.0;
        double lon = 2.0;
        int days = 3;
        //::error: (assignment.type.incompatible)
        result2 = String.format(WEBSERVICE_URL2, lat, lon, days);
    }

    void test_StringFormat_ObjectFlowSink() {
        @SuppressWarnings("flow")
        @FlowSinks(FlowSink.NETWORK) @FlowSources double lat = 1.0;
        @SuppressWarnings("flow")
        @FlowSinks(FlowSink.NETWORK) @FlowSources double lon = 2.0;
        @SuppressWarnings("flow")
        @FlowSinks(FlowSink.NETWORK) @FlowSources int days = 3;
        result3 = String.format(WEBSERVICE_URL3, lat, lon, days);
    }

    void test_StringFormat_ObjectFlowSource() {
        //::error: (assignment.type.incompatible)
        @FlowSources(FlowSource.AUDIO) double lat = 1.0;
        //::error: (assignment.type.incompatible)
        @FlowSources(FlowSource.AUDIO) double lon = 2.0;
        //::error: (assignment.type.incompatible)
        @FlowSources(FlowSource.AUDIO) int days = 3;
        //::error: (assignment.type.incompatible)
        result = String.format(WEBSERVICE_URL3, lat, lon, days);
    }
}