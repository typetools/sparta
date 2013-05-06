import sparta.checkers.quals.*;
import sparta.checkers.quals.SpartaPermission;

class MiscTests {
    double clean;

    void test() {
        // TODO: current flow inference stops as soon as the first qualifier hierarchy
        // has errors. Therefore, the sources are not propagated, resulting in
        // an assignment error. Let us work on this once the new flow is integrated.
        @SuppressWarnings("flow")
        @Sinks({SpartaPermission.INTERNET}) double lat = 1.0;
        //:: error: (assignment.type.incompatible)
        clean = lat;

        @SuppressWarnings("flow")
        @Sinks({SpartaPermission.INTERNET, SpartaPermission.CONDITIONAL}) @Sources(SpartaPermission.LITERAL) double lat2 = 1.0;
        clean = lat2;
    }

    @SuppressWarnings("flow")
    @Sinks({SpartaPermission.INTERNET, SpartaPermission.CONDITIONAL}) String WEBSERVICE_URL = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    @Sources({SpartaPermission.LITERAL, SpartaPermission.ACCESS_FINE_LOCATION}) String WEBSERVICE_URL2 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";

    String WEBSERVICE_URL3 = "[...]lat=%f&lon=%f&format=24+hourly&numDays=%d";
    @Sources({SpartaPermission.LITERAL, SpartaPermission.ACCESS_FINE_LOCATION}) String result; //With FP effective Sinks(INTERNET,CONDITIONAL}

    @Sources({SpartaPermission.LITERAL}) @Sinks({SpartaPermission.CONDITIONAL}) String result2;

    @Sinks({SpartaPermission.INTERNET}) String result3;


    void test_StringFormat_StringSpartaPermission() {
        double lat = 1.0;
        double lon = 2.0;
        int days = 3;
        result = String.format(WEBSERVICE_URL, lat, lon, days);
    }

    void test_StringFormat_StringSpartaPermission2() {
        double lat = 1.0;
        double lon = 2.0;
        int days = 3;
        //::error: (assignment.type.incompatible)
        result2 = String.format(WEBSERVICE_URL2, lat, lon, days);
    }

    void test_StringFormat_ObjectSpartaPermissionSink() {
        @SuppressWarnings("flow")
        @Sinks(SpartaPermission.INTERNET) @Sources double lat = 1.0;
        @SuppressWarnings("flow")
        @Sinks(SpartaPermission.INTERNET) @Sources double lon = 2.0;
        @SuppressWarnings("flow")
        @Sinks(SpartaPermission.INTERNET) @Sources int days = 3;
        result3 = String.format(WEBSERVICE_URL3, lat, lon, days);
    }

    void test_StringFormat_ObjectSpartaPermissionSource() {
        //::error: (assignment.type.incompatible)
        @Sources(SpartaPermission.READ_CALENDAR) double lat = 1.0;
        //::error: (assignment.type.incompatible)
        @Sources(SpartaPermission.READ_CALENDAR) double lon = 2.0;
        //::error: (assignment.type.incompatible)
        @Sources(SpartaPermission.READ_CALENDAR) int days = 3;
        //::error: (assignment.type.incompatible)
        result = String.format(WEBSERVICE_URL3, lat, lon, days);
    }
}