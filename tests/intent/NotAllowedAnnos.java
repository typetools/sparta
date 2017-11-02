package tests;

import sparta.checkers.quals.*;
import android.content.Intent;

public class NotAllowedAnnos {

    // ::error: (annotation.not.allowed.in.src)
    @SendIntent void m() {
      // ::error: (annotation.not.allowed.in.src)
        @IntentMapNew Intent intent = new Intent();
    }
}
