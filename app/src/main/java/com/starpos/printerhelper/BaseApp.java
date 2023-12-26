package com.starpos.printerhelper;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.starpos.printerhelper.logging.DebugLogTree;
import com.starpos.printerhelper.logging.FileLoggingTree;
import com.starpos.printerhelper.logging.ReleaseLogTree;
import com.starpos.printerhelper.utils.BluetoothUtil;
import com.starpos.printerhelper.utils.starPOSPrintHelper;

import timber.log.Timber;

public class BaseApp extends Application {

    private static BaseApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        init();

        if(BuildConfig.DEBUG){
            // new FileLoggingTree(this),
            Timber.plant(new DebugLogTree());
        } else{
            Timber.plant(new ReleaseLogTree());
        }
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
