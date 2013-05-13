import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;

class MiscTests {
    double clean;

    void test() {
        // TODO: current flow inference stops as soon as the first qualifier hierarchy
        // has errors. Therefore, the sources are not propagated, resulting in
        // an assignment error. Let us work on this once the new flow is integrated.
        @SuppressWarnings("flow")
        @Sink({FlowPermission.INTERNET}) double lat = 1.0;
        //:: error: (assignment.type.incompatible)
        clean = lat;

        @SuppressWarnings("flow")
        @Sink({FlowPermission.INTERNET, FlowPermission.CONDITIONAL}) @Source(FlowPermission.LITERAL) double lat2 = 1.0;
        clean = lat2;
    }

    @SuppressWarnings("flow")
    @Sink({FlowPermission.INTERNET, FlowPermission.CONDITIONAL}) String WEBSERVICE_URL = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    @Source({FlowPermission.LITERAL, FlowPermission.ACCESS_FINE_LOCATION}) String WEBSERVICE_URL2 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    String WEBSERVICE_URL3 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
    @Source({FlowPermission.LITERAL, FlowPermission.ACCESS_FINE_LOCATION}) String result; //With FP effective Sink(INTERNET,CONDITIONAL}

    @Source({FlowPermission.LITERAL}) @Sink({FlowPermission.CONDITIONAL}) String result2;

    @Sink({FlowPermission.INTERNET}) String result3;


    void test_StringFormat_StringFlowPermission() {
        double lat = 1.0;
        double lon = 2.0;
        int days = 3;
        result = String.format(WEBSERVICE_URL, lat, lon, days);
    }

    void test_StringFormat_StringFlowPermission2() {
        double lat = 1.0;
        double lon = 2.0;
        int days = 3;
        //::error: (assignment.type.incompatible)
        result2 = String.format(WEBSERVICE_URL2, lat, lon, days);
    }

    void test_StringFormat_ObjectFlowPermissionSink() {
        @SuppressWarnings("flow")
        @Sink(FlowPermission.INTERNET) @Source double lat = 1.0;
        @SuppressWarnings("flow")
        @Sink(FlowPermission.INTERNET) @Source double lon = 2.0;
        @SuppressWarnings("flow")
        @Sink(FlowPermission.INTERNET) @Source int days = 3;
        result3 = String.format(WEBSERVICE_URL3, lat, lon, days);
    }

    void test_StringFormat_ObjectFlowPermissionSource() {
        //::error: (assignment.type.incompatible)
        @Source(FlowPermission.READ_CALENDAR) double lat = 1.0;
        //::error: (assignment.type.incompatible)
        @Source(FlowPermission.READ_CALENDAR) double lon = 2.0;
        //::error: (assignment.type.incompatible)
        @Source(FlowPermission.READ_CALENDAR) int days = 3;
        //::error: (assignment.type.incompatible)
        result = String.format(WEBSERVICE_URL3, lat, lon, days);
    }
}