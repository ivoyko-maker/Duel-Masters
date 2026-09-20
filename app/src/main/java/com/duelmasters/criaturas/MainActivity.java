package com.duelmasters.criaturas;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/** Launches the self-contained, offline game canvas. */
public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new GameView(this));
    }
}
