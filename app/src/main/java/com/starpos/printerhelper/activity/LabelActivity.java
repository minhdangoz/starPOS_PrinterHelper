package com.starpos.printerhelper.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.starpos.printerhelper.R;
import com.starpos.printerhelper.utils.starPOSPrintHelper;

/**
 * <pre>
 *     Currently, V2 and V2pro model support the function of label printing
 *     现在，V2和V2pro机型支持标签打印的功能，可以单独打印或者连续打印标签
 *  </pre>
 *
 * @author kaltin
 * @since create at 2020-05-21
 */
public class LabelActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_label);
        setMyTitle(R.string.label_title);
        setBack();
    }

    public void testOne(View view) {
        if(!starPOSPrintHelper.getInstance().isLabelMode()){
            Toast.makeText(this, R.string.toast_12, Toast.LENGTH_LONG).show();
            return;
        }

        starPOSPrintHelper.getInstance().printOneLabel();
    }


    public void testMore(View view) {
        if(!starPOSPrintHelper.getInstance().isLabelMode()){
            Toast.makeText(this, R.string.toast_12, Toast.LENGTH_LONG).show();
            return;
        }

        starPOSPrintHelper.getInstance().printMultiLabel(5);
    }
}
