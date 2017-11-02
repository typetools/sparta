import sparta.checkers.quals.*;
import sparta.checkers.quals.FlowPermission;
import static sparta.checkers.quals.FlowPermissionString.*;

class MiscTests {
    double clean;

    void test() {

        @Sink({FlowPermissionString.INTERNET}) double lat = 1.0;
        clean = lat;

        @Sink({FlowPermissionString.INTERNET}) @Source({}) double lat2 = 1.0;
        clean = lat2;
    }

    @SuppressWarnings("flow")
    @Sink({FlowPermissionString.INTERNET}) String WEBSERVICE_URL = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    @Source({ FlowPermissionString.ACCESS_FINE_LOCATION}) String WEBSERVICE_URL2 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    @Sink(ANY) String WEBSERVICE_URL3 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
    @Source({ FlowPermissionString.ACCESS_FINE_LOCATION}) String result; //With FP effective Sink(INTERNET}

    @Source({}) @Sink({}) String result2;

    @Sink({FlowPermissionString.INTERNET}) String result3;


    void test_StringFormat_StringFlowPermission() {
        Double lat = Double.valueOf(1.0);
        Double lon = Double.valueOf(2.0);
        Integer days = Integer.valueOf(3);
        result = String.format(WEBSERVICE_URL, lat, lon, days);
    }

    void test_StringFormat_StringFlowPermission2() {
        Double lat = 1.0;
        Double lon = 2.0;
        Integer days = 3;

        // ::error: (assignment.type.incompatible)
        result2 = String.format(WEBSERVICE_URL2, lat, lon, days);
    }

    void test_StringFormat_ObjectFlowPermissionSink() {
        @Sink(INTERNET)  Double lat = 1.0;
        @Sink(INTERNET)  Double lon = 2.0;
        @Sink(INTERNET)  Integer days = 3;
        result3 = String.format(WEBSERVICE_URL3, lat, lon, days);
    }

    void test_StringFormat_ObjectFlowPermissionSource() {
        Double lat = (@Source({READ_CALENDAR}) Double) Double.valueOf(1.0);
        Double lon = (@Source({READ_CALENDAR})Double) Double.valueOf(2.0);
        Integer days = (@Source({READ_CALENDAR})Integer) Integer.valueOf(3);

        // ::error: (assignment.type.incompatible)
        result = String.format(WEBSERVICE_URL3, lat, lon, days);
    }
}
