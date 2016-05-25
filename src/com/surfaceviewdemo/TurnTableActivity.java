
package com.surfaceviewdemo;

import android.app.Activity;
import android.graphics.Camera;
import android.os.Bundle;

/**
 * @author liyong
 */
public class TurnTableActivity extends Activity {

    private Camera mCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new TurnTableSurfaceView(this));
        
        mCamera = new Camera();
    }

}
