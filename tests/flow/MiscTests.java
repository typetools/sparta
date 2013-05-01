import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.FlowSource;
import sparta.checkers.quals.Sinks.FlowSink;

class MiscTests {
    double clean;

    void test() {
        // TODO: current flow inference stops as soon as the first qualifier hierarchy
        // has errors. Therefore, the sources are not propagated, resulting in
        // an assignment error. Let us work on this once the new flow is integrated.
        @SuppressWarnings("flow")
        @Sinks({FlowSink.NETWORK}) double lat = 1.0;
        //:: error: (assignment.type.incompatible)
        clean = lat;

        @SuppressWarnings("flow")
        @Sinks({FlowSink.NETWORK, FlowSink.CONDITIONAL}) @Sources(FlowSource.LITERAL) double lat2 = 1.0;
        clean = lat2;
    }

    @SuppressWarnings("flow")
    @Sinks({FlowSink.NETWORK, FlowSink.CONDITIONAL}) String WEBSERVICE_URL = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    @Sources({FlowSource.LITERAL, FlowSource.LOCATION}) String WEBSERVICE_URL2 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    String WEBSERVICE_URL3 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
    @Sources({FlowSource.LITERAL, FlowSource.LOCATION}) String result; //With FP effective Sinks(NETWORK,CONDITIONAL}

    @Sources({FlowSource.LITERAL}) @Sinks({FlowSink.CONDITIONAL}) String result2;

    @Sinks({FlowSink.NETWORK}) String result3;


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
        @Sinks(FlowSink.NETWORK) @Sources double lat = 1.0;
        @SuppressWarnings("flow")
        @Sinks(FlowSink.NETWORK) @Sources double lon = 2.0;
        @SuppressWarnings("flow")
        @Sinks(FlowSink.NETWORK) @Sources int days = 3;
        result3 = String.format(WEBSERVICE_URL3, lat, lon, days);
    }

    void test_StringFormat_ObjectFlowSource() {
        //::error: (assignment.type.incompatible)
        @Sources(FlowSource.AUDIO) double lat = 1.0;
        //::error: (assignment.type.incompatible)
        @Sources(FlowSource.AUDIO) double lon = 2.0;
        //::error: (assignment.type.incompatible)
        @Sources(FlowSource.AUDIO) int days = 3;
        //::error: (assignment.type.incompatible)
        result = String.format(WEBSERVICE_URL3, lat, lon, days);
    }
}