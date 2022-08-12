package com.starpos.printerhelper.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.starpos.printerhelper.R;
import com.starpos.printerhelper.utils.BluetoothUtil;
import com.starpos.printerhelper.utils.BytesUtil;
import com.starpos.printerhelper.utils.ESCUtil;
import com.starpos.printerhelper.utils.starPOSPrintHelper;

import java.nio.charset.StandardCharsets;

import sunmi.sunmiui.dialog.DialogCreater;
import sunmi.sunmiui.dialog.EditTextDialog;

public abstract class BaseActivity extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
        initPrinterStyle();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler != null){
            handler.removeCallbacksAndMessages(null);
        }
    }

    /**
     *  Initialize the printer
     *  All style settings will be restored to default
     */
    private void initPrinterStyle() {
        if(BluetoothUtil.isBlueToothPrinter){
            String init = new String(ESCUtil.init_printer(), StandardCharsets.UTF_8);
            Log.i("BLE Printer", "== init == " + init);
            BluetoothUtil.sendData(ESCUtil.init_printer());
        }else{
            starPOSPrintHelper.getInstance().initPrinter();
        }
    }

    /**
     * set title
     * @param title title name
     */
    void setMyTitle(String title){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(title);
        }
    }

    /**
     * set title
     * @param title title res
     */
    void setMyTitle(@StringRes int title){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setTitle(title);
        }
        setSubTitle();
    }

    /**
     * set sub title
     */
    void setSubTitle(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            if(BluetoothUtil.isBlueToothPrinter){
                actionBar.setSubtitle("bluetooth®");
            }else{
                if(starPOSPrintHelper.getInstance().starPOSPrinter == starPOSPrintHelper.NoStarPOSPrinter){
                    actionBar.setSubtitle("no printer");
                }else if(starPOSPrintHelper.getInstance().starPOSPrinter == starPOSPrintHelper.CheckStarPOSPrinter){
                    actionBar.setSubtitle("connecting");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setSubTitle();
                        }
                    }, 2000);
                }else if(starPOSPrintHelper.getInstance().starPOSPrinter == starPOSPrintHelper.FoundStarPOSPrinter){
                    actionBar.setSubtitle("");
                }else{
                    starPOSPrintHelper.getInstance().initStarPOSPrinterService(this);
                }
            }
        }
    }

    /**
     * set back
     */
    void setBack(){
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.hexprint, menu);
        return true;
    }

    EditTextDialog mEditTextDialog;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_print:
                mEditTextDialog = DialogCreater.createEditTextDialog(this,
                    getResources().getString(R.string.cancel), getResources().getString(R.string.confirm), getResources().getString(R.string.input_order), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEditTextDialog.cancel();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text = mEditTextDialog.getEditText().getText().toString();
                        byte[] data = BytesUtil.getBytesFromHexString(text);
                        if(BluetoothUtil.isBlueToothPrinter){
                            BluetoothUtil.sendData(data);
                        }else{
                            starPOSPrintHelper.getInstance().sendRawData(data);
                        }
                        mEditTextDialog.cancel();
                    }
                }, null);
                mEditTextDialog.show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
