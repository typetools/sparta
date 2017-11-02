import stubfile.ExampleApi;

public class NotReviewed{

// warning: 11 byte code methods have not been reviewed.
    void notReviewed() {
        // :: warning: (not.reviewed)
        ExampleApi api = new ExampleApi();
        String x = "";
        String s = "";
        // :: warning: (not.reviewed)
        api.notReviewed();
        // :: warning: (not.reviewed)
        x = api.notReviewed1();
        // :: warning: (not.reviewed)
        api.notReviewed2(s);
        // :: warning: (not.reviewed)
        x = api.notReviewed3(s);
        // :: warning: (not.reviewed)
        x = api.STATIC_FIELD;
    }

    void notReviewedStatic() {
        String x = "";
        String s = "";
        // :: warning: (not.reviewed)
        ExampleApi.ExampleStaticApi.notReviewed();
        // :: warning: (not.reviewed)
        x = ExampleApi.ExampleStaticApi.notReviewed1();
        // :: warning: (not.reviewed)
        ExampleApi.ExampleStaticApi.notReviewed2(s);
        // :: warning: (not.reviewed)
        x = ExampleApi.ExampleStaticApi.notReviewed3(s);
        // :: warning: (not.reviewed)
       String y = ExampleApi.ExampleStaticApi.STATIC_FIELD;
    }

    void reviewed(){
        Object o = new Object();
        Math.min(1, 2);
    }
}

// :: warning: (not.reviewed)
class SubExampleApi extends ExampleApi {
    @Override
    // :: warning: (not.reviewed.overrides)
    public void notReviewed() {
    }
    @Override
    // :: warning: (not.reviewed.overrides)
    public String notReviewed1() {
        return null;
    }
    @Override
    // :: warning: (not.reviewed.overrides)
    public void notReviewed2(String s) {

    }
    @Override
    // :: warning: (not.reviewed.overrides)
    public String notReviewed3(String s) {
        return null;
    }
}