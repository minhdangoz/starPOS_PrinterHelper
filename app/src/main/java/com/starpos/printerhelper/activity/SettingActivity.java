package com.starpos.printerhelper.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.starpos.printerhelper.R;
import com.starpos.printerhelper.utils.BluetoothUtil;
import com.starpos.printerhelper.utils.starPOSPrintHelper;

import sunmi.sunmiui.dialog.DialogCreater;
import sunmi.sunmiui.dialog.ListDialog;

/**
 * Settings
 *
 * @author kaltin
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {

    String[] method = new String[]{"API", "Bluetooth"};

    private TextView mTextView1, mTextView2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setMyTitle(R.string.setting_title);
        setBack();

        findViewById(R.id.setting_connect).setOnClickListener(this);
        findViewById(R.id.setting_info).setOnClickListener(this);

        mTextView1 = findViewById(R.id.setting_conected);
        mTextView2 = findViewById(R.id.setting_disconected);
        mTextView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starPOSPrintHelper.getInstance().initStarPOSPrinterService(SettingActivity.this);
                setService();
            }
        });
        mTextView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                starPOSPrintHelper.getInstance().deInitStarPOSPrinterService(SettingActivity.this);
                setService();
            }
        });

        if (BluetoothUtil.isBlueToothPrinter) {
            ((TextView) findViewById(R.id.setting_textview1)).setText("Bluetooth");
        } else {
            ((TextView) findViewById(R.id.setting_textview1)).setText("API");
        }
        setService();
    }

    @Override
    public void onClick(View v) {
        final ListDialog listDialog;
        switch (v.getId()) {
            case R.id.setting_connect:
                listDialog = DialogCreater.createListDialog(this, getResources().getString(R.string.connect_method), getResources().getString(R.string.cancel), method);
                listDialog.setItemClickListener(new ListDialog.ItemClickListener() {
                    @Override
                    public void OnItemClick(int position) {
                        ((TextView) findViewById(R.id.setting_textview1)).setText(method[position]);
                        setMethod(position);
                        setMyTitle(R.string.setting_title);
                        listDialog.cancel();
                    }
                });
                listDialog.show();
                break;
            case R.id.setting_info:
                startActivity(new Intent(SettingActivity.this, PrinterInfoActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Configure printer control via Bluetooth or API
     */
    private void setMethod(int position) {
        if (position == 0) {
            BluetoothUtil.disconnectBlueTooth(SettingActivity.this);
            BluetoothUtil.isBlueToothPrinter = false;
        } else {
            if (!BluetoothUtil.connectBlueTooth(SettingActivity.this)) {
                ((TextView) findViewById(R.id.setting_textview1)).setText(method[0]);
                BluetoothUtil.isBlueToothPrinter = false;
            } else {
                BluetoothUtil.isBlueToothPrinter = true;
            }
        }
    }

    /**
     * Set print service connection status
     */
    private void setService() {
        starPOSPrintHelper.getInstance().showPrinterStatus(this);

        if (starPOSPrintHelper.getInstance().starPOSPrinter == starPOSPrintHelper.FoundStarPOSPrinter) {
            mTextView1.setTextColor(getResources().getColor(R.color.white1));
            mTextView1.setEnabled(false);
            mTextView2.setTextColor(getResources().getColor(R.color.white));
            mTextView2.setEnabled(true);
        } else if (starPOSPrintHelper.getInstance().starPOSPrinter == starPOSPrintHelper.CheckStarPOSPrinter) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setService();
                }
            }, 2000);
        } else if (starPOSPrintHelper.getInstance().starPOSPrinter == starPOSPrintHelper.LostStarPOSPrinter) {
            mTextView1.setTextColor(getResources().getColor(R.color.white));
            mTextView1.setEnabled(true);
            mTextView2.setTextColor(getResources().getColor(R.color.white1));
            mTextView2.setEnabled(false);
        } else {
            mTextView1.setTextColor(getResources().getColor(R.color.white1));
            mTextView1.setEnabled(true);
            mTextView2.setTextColor(getResources().getColor(R.color.white1));
            mTextView2.setEnabled(false);
        }
    }
}
