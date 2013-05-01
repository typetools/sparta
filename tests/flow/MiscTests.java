import sparta.checkers.quals.*;
import sparta.checkers.quals.Sources.SPARTA_Permission;
import sparta.checkers.quals.Sinks.SPARTA_Permission;

class MiscTests {
    double clean;

    void test() {
        // TODO: current flow inference stops as soon as the first qualifier hierarchy
        // has errors. Therefore, the sources are not propagated, resulting in
        // an assignment error. Let us work on this once the new flow is integrated.
        @SuppressWarnings("flow")
        @Sinks({SPARTA_Permission.NETWORK}) double lat = 1.0;
        //:: error: (assignment.type.incompatible)
        clean = lat;

        @SuppressWarnings("flow")
        @Sinks({SPARTA_Permission.NETWORK, SPARTA_Permission.CONDITIONAL}) @Sources(SPARTA_Permission.LITERAL) double lat2 = 1.0;
        clean = lat2;
    }

    @SuppressWarnings("flow")
    @Sinks({SPARTA_Permission.NETWORK, SPARTA_Permission.CONDITIONAL}) String WEBSERVICE_URL = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    @Sources({SPARTA_Permission.LITERAL, SPARTA_Permission.LOCATION}) String WEBSERVICE_URL2 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    String WEBSERVICE_URL3 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
    @Sources({SPARTA_Permission.LITERAL, SPARTA_Permission.LOCATION}) String result; //With FP effective Sinks(NETWORK,CONDITIONAL}

    @Sources({SPARTA_Permission.LITERAL}) @Sinks({SPARTA_Permission.CONDITIONAL}) String result2;

    @Sinks({SPARTA_Permission.NETWORK}) String result3;


    void test_StringFormat_StringSPARTA_Permission() {
        double lat = 1.0;
        double lon = 2.0;
        int days = 3;
        result = String.format(WEBSERVICE_URL, lat, lon, days);
    }

    void test_StringFormat_StringSPARTA_Permission() {
        double lat = 1.0;
        double lon = 2.0;
        int days = 3;
        //::error: (assignment.type.incompatible)
        result2 = String.format(WEBSERVICE_URL2, lat, lon, days);
    }

    void test_StringFormat_ObjectSPARTA_Permission() {
        @SuppressWarnings("flow")
        @Sinks(SPARTA_Permission.NETWORK) @Sources double lat = 1.0;
        @SuppressWarnings("flow")
        @Sinks(SPARTA_Permission.NETWORK) @Sources double lon = 2.0;
        @SuppressWarnings("flow")
        @Sinks(SPARTA_Permission.NETWORK) @Sources int days = 3;
        result3 = String.format(WEBSERVICE_URL3, lat, lon, days);
    }

    void test_StringFormat_ObjectSPARTA_Permission() {
        //::error: (assignment.type.incompatible)
        @Sources(SPARTA_Permission.AUDIO) double lat = 1.0;
        //::error: (assignment.type.incompatible)
        @Sources(SPARTA_Permission.AUDIO) double lon = 2.0;
        //::error: (assignment.type.incompatible)
        @Sources(SPARTA_Permission.AUDIO) int days = 3;
        //::error: (assignment.type.incompatible)
        result = String.format(WEBSERVICE_URL3, lat, lon, days);
    }
}