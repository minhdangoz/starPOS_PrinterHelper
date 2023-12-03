package com.starpos.printerhelper;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.starpos.printerhelper.utils.BluetoothUtil;
import com.starpos.printerhelper.utils.starPOSPrintHelper;

public class BaseApp extends Application {

    private static BaseApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        init();
    }

    public static BaseApp getInstance(){
        return mInstance;
    }

    /**
     * Connect print service through interface library
     */
    private void init(){
        starPOSPrintHelper.getInstance().initStarPOSPrinterService(this);
        BluetoothUtil.connectBlueTooth(this);
    }
}
