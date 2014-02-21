package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

public class DeviceAdminAdd extends Activity {

    @Override
    public @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
            sink={BIND_DEVICE_ADMIN}),
            @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
                    sink={BIND_DEVICE_ADMIN})
    }) Intent getIntent() {
        @SuppressWarnings("")
        @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
        sink={BIND_DEVICE_ADMIN}), @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
                sink={BIND_DEVICE_ADMIN})}) Intent output = super.getIntent();
        return output;
    }

}