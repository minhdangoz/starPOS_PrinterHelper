package com.starpos.printerhelper.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.starpos.printerhelper.R;
import com.starpos.printerhelper.utils.BitmapUtil;
import com.starpos.printerhelper.utils.BluetoothUtil;
import com.starpos.printerhelper.utils.ESCUtil;
import com.starpos.printerhelper.utils.starPOSPrintHelper;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;

import sunmi.sunmiui.dialog.DialogCreater;
import sunmi.sunmiui.dialog.ListDialog;

/**
 * Example of printing text
 */
public class TextActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "TextActivity";

    private TextView mTextView1, mTextView2;
    private CheckBox mCheckBox1, mCheckBox2, mCheckBox3;
    private EditText mEditText;
    private LinearLayout mLayout, mLinearLayout;
    private int record;
    private boolean isBold, isUnderLine, isTrueTypeFont;

    private String[] mStrings = new String[]{"CP437", "CP850", "CP860", "CP863", "CP865", "CP857", "CP737", "CP928", "Windows-1252","Windows-1258", "CP866", "CP852", "CP858", "CP874", "Windows-775", "CP855", "CP862", "CP864", "GB18030", "BIG5", "KSC5601", "UTF-8"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);
        setMyTitle(R.string.text_title);
        setBack();

        record = 17;
        isBold = false;
        isUnderLine = false;
        isTrueTypeFont = true;
        mTextView1 = findViewById(R.id.text_text_character);
        mTextView2 = findViewById(R.id.text_text_size);
        mCheckBox1 = findViewById(R.id.text_bold);
        mCheckBox2 = findViewById(R.id.text_underline);
        mCheckBox3 = findViewById(R.id.text_truetype);
        mEditText = findViewById(R.id.text_text);

        mLinearLayout = findViewById(R.id.text_all);
        mLayout = findViewById(R.id.text_set);

        mLinearLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                mLinearLayout.getWindowVisibleDisplayFrame(r);
                if(r.bottom < 800){
                    mLayout.setVisibility(View.GONE);
                }else{
                    mLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        mCheckBox1.setOnCheckedChangeListener(this);
        mCheckBox2.setOnCheckedChangeListener(this);
        mCheckBox3.setOnCheckedChangeListener(this);


        findViewById(R.id.text_character).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ListDialog listDialog = DialogCreater.createListDialog(TextActivity.this, getResources().getString(R.string.characterset), getResources().getString(R.string.cancel), mStrings);
                listDialog.setItemClickListener(new ListDialog.ItemClickListener() {
                    @Override
                    public void OnItemClick(int position) {
                        mTextView1.setText(mStrings[position]);
                        record = position;
                        listDialog.cancel();
                    }
                });
                listDialog.show();
            }
        });

        findViewById(R.id.text_size).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSeekBarDialog(TextActivity.this, getResources().getString(R.string.size_text), 12, 36, mTextView2);
            }
        });
    }

    public void onClick(View view) {
        String content = mEditText.getText().toString();

        float size = Integer.parseInt(mTextView2.getText().toString());
        if (!BluetoothUtil.isBlueToothPrinter) {
            starPOSPrintHelper.getInstance().printText(content, size, isBold, isUnderLine);
            starPOSPrintHelper.getInstance().feedPaper();
        } else {
            printByBluTooth(content);
        }
    }

    private void printByBluTooth(String content) {
        if (isBold) {
            BluetoothUtil.sendData(ESCUtil.boldOn());
        } else {
            BluetoothUtil.sendData(ESCUtil.boldOff());
        }

        if (isUnderLine) {
            BluetoothUtil.sendData(ESCUtil.underlineWithOneDotWidthOn());
        } else {
            BluetoothUtil.sendData(ESCUtil.underlineOff());
        }

        if (record < 17) {
            BluetoothUtil.sendData(ESCUtil.singleByte());
            BluetoothUtil.sendData(ESCUtil.setCodeSystemSingle(codeParse(record)));
        } else {
            BluetoothUtil.sendData(ESCUtil.singleByteOff());
            BluetoothUtil.sendData(ESCUtil.setCodeSystem(codeParse(record)));
        }

        if(isTrueTypeFont){
            // print bitmap
            Bitmap bmp = BitmapUtil.textToBitmap(content, 384, 21, Typeface.SANS_SERIF, false);
            printImage(bmp);
        } else {
            BluetoothUtil.sendData(content.getBytes());
        }

        BluetoothUtil.sendData(ESCUtil.nextLine(3));
    }

    private BitSet getBitsImageData(Bitmap image) {
        int threshold = 127;
        int index = 0;
        int dimenssions = image.getWidth() * image.getHeight();
        BitSet imageBitsData = new BitSet(dimenssions);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getPixel(x, y);
                int red = (color & 0x00ff0000) >> 16;
                int green = (color & 0x0000ff00) >> 8;
                int blue = color & 0x000000ff;
                int luminance = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                //dots[index] = (luminance < threshold);
                imageBitsData.set(index, (luminance < threshold));
                index++;
            }
        }

        return imageBitsData;
    }


    public void printImage(Bitmap image) {
        BitSet imageBits = getBitsImageData(image);

        BluetoothUtil.sendData(ESCUtil.init_printer());
        BluetoothUtil.sendData(ESCUtil.setLineSpace(24));

        int offset = 0;
        while (offset < image.getHeight()) {
            BluetoothUtil.sendData(ESCUtil.printBitmapMode(image.getWidth()));

            int imageDataLineIndex = 0;
            byte[] imageDataLine = new byte[3 * image.getWidth()];

            for (int x = 0; x < image.getWidth(); ++x) {

                // Remember, 24 dots = 24 bits = 3 bytes.
                // The 'k' variable keeps track of which of those
                // three bytes that we're currently scribbling into.
                for (int k = 0; k < 3; ++k) {
                    byte slice = 0;

                    // A byte is 8 bits. The 'b' variable keeps track
                    // of which bit in the byte we're recording.
                    for (int b = 0; b < 8; ++b) {
                        // Calculate the y position that we're currently
                        // trying to draw. We take our offset, divide it
                        // by 8 so we're talking about the y offset in
                        // terms of bytes, add our current 'k' byte
                        // offset to that, multiple by 8 to get it in terms
                        // of bits again, and add our bit offset to it.
                        int y = (((offset / 8) + k) * 8) + b;

                        // Calculate the location of the pixel we want in the bit array.
                        // It'll be at (y * width) + x.
                        int i = (y * image.getWidth()) + x;

                        // If the image (or this stripe of the image)
                        // is shorter than 24 dots, pad with zero.
                        boolean v = false;
                        if (i < imageBits.length()) {
                            v = imageBits.get(i);
                        }
                        // Finally, store our bit in the byte that we're currently
                        // scribbling to. Our current 'b' is actually the exact
                        // opposite of where we want it to be in the byte, so
                        // subtract it from 7, shift our bit into place in a temp
                        // byte, and OR it with the target byte to get it into there.
                        slice |= (byte) ((v ? 1 : 0) << (7 - b));
                    }

                    imageDataLine[imageDataLineIndex + k] = slice;

                    // Phew! Write the damn byte to the buffer
                    //printOutput.write(slice);
                }

                imageDataLineIndex += 3;
            }

            BluetoothUtil.sendData(imageDataLine);

            offset += 24;
            BluetoothUtil.sendData(ESCUtil.nextLine(1));

        }

        BluetoothUtil.sendData(ESCUtil.setLineSpace(30));
        BluetoothUtil.sendData(ESCUtil.nextLine(1));

    }

    private byte codeParse(int value) {
        byte res = 0x00;
        switch (value) {
            case 0:
                res = 0x00;
                break;
            case 1:
            case 2:
            case 3:
            case 4:
                res = (byte) (value + 1);
                break;
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                res = (byte) (value + 8);
                break;
            case 12:
                res = 21;
                break;
            case 13:
                res = 33;
                break;
            case 14:
                res = 34;
                break;
            case 15:
                res = 36;
                break;
            case 16:
                res = 37;
                break;
            case 17:
            case 18:
            case 19:
                res = (byte) (value - 17);
                break;
            case 20:
                res = (byte) 0xff;
                break;
            default:
                break;
        }
        return (byte) res;
    }

    /**
     * 自定义的seekbar dialog
     *
     * @param context
     * @param title
     * @param min
     * @param max
     * @param set
     */
    private void showSeekBarDialog(Context context, String title, final int min, final int max, final TextView set) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.widget_seekbar, null);
        builder.setView(view);
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        TextView tv_title = view.findViewById(R.id.sb_title);
        TextView tv_start = view.findViewById(R.id.sb_start);
        TextView tv_end = view.findViewById(R.id.sb_end);
        final TextView tv_result = view.findViewById(R.id.sb_result);
        TextView tv_ok = view.findViewById(R.id.sb_ok);
        TextView tv_cancel = view.findViewById(R.id.sb_cancel);
        SeekBar sb = view.findViewById(R.id.sb_seekbar);
        tv_title.setText(title);
        tv_start.setText(min + "");
        tv_end.setText(max + "");
        tv_result.setText(set.getText());
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        tv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                set.setText(tv_result.getText());
                dialog.cancel();
            }
        });
        sb.setMax(max - min);
        sb.setProgress(Integer.parseInt(set.getText().toString()) - min);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int rs = min + progress;
                tv_result.setText(rs + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dialog.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.text_bold:
                isBold = isChecked;
                break;
            case R.id.text_underline:
                isUnderLine = isChecked;
                break;
            case R.id.text_truetype:
                isTrueTypeFont = isChecked;
                break;
            default:
                break;
        }
    }



}
