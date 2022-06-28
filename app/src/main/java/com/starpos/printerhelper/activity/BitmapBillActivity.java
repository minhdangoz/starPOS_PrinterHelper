package com.starpos.printerhelper.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.starpos.printerhelper.R;
import com.starpos.printerhelper.utils.BluetoothUtil;
import com.starpos.printerhelper.utils.ESCUtil;
import com.starpos.printerhelper.utils.starPOSPrintHelper;

import java.util.BitSet;

import androidx.annotation.Nullable;
import sunmi.sunmiui.dialog.DialogCreater;
import sunmi.sunmiui.dialog.ListDialog;

/**
 * Example of printing pic
 *
 * @author Jimmy
 */
public class BitmapBillActivity extends BaseActivity {
    ImageView mImageView;
    TextView mTextView1, mTextView2, mTextView3;
    LinearLayout ll, ll1, ll2;
    Bitmap bitmap, bitmap1;
    CheckBox mCheckBox1, mCheckBox2;

    int mytype;
    int myorientation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bitmap);
        setMyTitle("Bitmap bill");
        setBack();
        initView();
        if (!BluetoothUtil.isBlueToothPrinter) {
            ll.setVisibility(View.GONE);
            ll1.setVisibility(View.GONE);
            ll2.setVisibility(View.VISIBLE);
        } else {
            ll.setVisibility(View.VISIBLE);
            ll1.setVisibility(View.VISIBLE);
            ll2.setVisibility(View.GONE);
        }
    }

    private void initView() {
        mTextView1 = findViewById(R.id.pic_align_info);
        mTextView2 = findViewById(R.id.pic_type_info);
        mTextView3 = findViewById(R.id.pic_orientation_info);
        mCheckBox1 = findViewById(R.id.pic_width);
        mCheckBox2 = findViewById(R.id.pic_height);
        ll = findViewById(R.id.pic_style);
        ll1 = findViewById(R.id.pic_type);
        ll2 = findViewById(R.id.pic_orientation);
        findViewById(R.id.pic_align).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] pos = new String[]{getResources().getString(R.string.align_left), getResources().getString(R.string.align_mid), getResources().getString(R.string.align_right)};
                final ListDialog listDialog = DialogCreater.createListDialog(BitmapBillActivity.this, getResources().getString(R.string.align_form), getResources().getString(R.string.cancel), pos);
                listDialog.setItemClickListener(new ListDialog.ItemClickListener() {
                    @Override
                    public void OnItemClick(int position) {
                        mTextView1.setText(pos[position]);
                        if (!BluetoothUtil.isBlueToothPrinter) {
                            starPOSPrintHelper.getInstance().setAlign(position);
                        } else {
                            byte[] send;
                            if (position == 0) {
                                send = ESCUtil.alignLeft();
                            } else if (position == 1) {
                                send = ESCUtil.alignCenter();
                            } else {
                                send = ESCUtil.alignRight();
                            }
                            BluetoothUtil.sendData(send);
                        }
                        listDialog.cancel();
                    }
                });
                listDialog.show();
            }
        });

        ll2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] orientation = new String[]{getResources().getString(R.string.pic_hor), getResources().getString(R.string.pic_ver)};
                final ListDialog listDialog = DialogCreater.createListDialog(BitmapBillActivity.this, getResources().getString(R.string.pic_pos), getResources().getString(R.string.cancel), orientation);
                listDialog.setItemClickListener(new ListDialog.ItemClickListener() {
                    @Override
                    public void OnItemClick(int position) {
                        mTextView3.setText(orientation[position]);
                        myorientation = position;
                        listDialog.cancel();
                    }
                });
                listDialog.show();
            }
        });

        ll1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String[] type = new String[]{getResources().getString(R.string.pic_mode1), getResources().getString(R.string.pic_mode2), getResources().getString(R.string.pic_mode3), getResources().getString(R.string.pic_mode4), getResources().getString(R.string.pic_mode5)};
                final ListDialog listDialog = DialogCreater.createListDialog(BitmapBillActivity.this, getResources().getString(R.string.pic_mode), getResources().getString(R.string.cancel), type);
                listDialog.setItemClickListener(new ListDialog.ItemClickListener() {
                    @Override
                    public void OnItemClick(int position) {
                        mTextView2.setText(type[position]);
                        mytype = position;
                        if (position == 0) {
                            ll.setVisibility(View.VISIBLE);
                        } else {
                            ll.setVisibility(View.INVISIBLE);
                        }
                        listDialog.cancel();
                    }
                });
                listDialog.show();
            }
        });
        mImageView = findViewById(R.id.bitmap_imageview);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inTargetDensity = 100;
        options.inDensity = 100;
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bill2, options);

        }

        if (bitmap1 == null) {
            bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.bill2, options);
            bitmap1 = scaleImage(bitmap1);
        }
        if (BluetoothUtil.isBlueToothPrinter) {
            mImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap1));
        } else {
            mImageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
        }

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
            BluetoothUtil.sendData(ESCUtil.printBitmapMode(bitmap.getWidth()));

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

    /**
     * Scaled image width is an integer multiple of 8 and can be ignored
     */
    private Bitmap scaleImage(Bitmap bitmap1) {
        int width = bitmap1.getWidth();
        int height = bitmap1.getHeight();
        int newWidth = (width / 8 + 1) * 8;
        float scaleWidth = ((float) newWidth) / width;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, 1);
        return Bitmap.createBitmap(bitmap1, 0, 0, width, height, matrix, true);
    }

    public void onClick(View view) {
        if (!BluetoothUtil.isBlueToothPrinter) {
            starPOSPrintHelper.getInstance().printBitmap(bitmap, myorientation);
            starPOSPrintHelper.getInstance().feedPaper();
        } else {
//            if(mytype == 0){
//                if(mCheckBox1.isChecked() && mCheckBox2.isChecked()){
//                    BluetoothUtil.sendData(ESCUtil.printBitmap(bitmap1, 3));
//                }else if(mCheckBox1.isChecked()){
//                    BluetoothUtil.sendData(ESCUtil.printBitmap(bitmap1, 1));
//                }else if(mCheckBox2.isChecked()){
//                    BluetoothUtil.sendData(ESCUtil.printBitmap(bitmap1, 2));
//                }else{
//                    BluetoothUtil.sendData(ESCUtil.printBitmap(bitmap1, 0));
//                }
//            }else if(mytype == 1){
//                BluetoothUtil.sendData(ESCUtil.selectBitmap(bitmap1, 0));
//            }else if(mytype == 2){
//                BluetoothUtil.sendData(ESCUtil.selectBitmap(bitmap1, 1));
//            }else if(mytype == 3){
//                BluetoothUtil.sendData(ESCUtil.selectBitmap(bitmap1, 32));
//            }else if(mytype == 4){
//                BluetoothUtil.sendData(ESCUtil.selectBitmap(bitmap1, 33));
//            }
//            BluetoothUtil.sendData(ESCUtil.nextLine(3));

            printImage(bitmap1);
            BluetoothUtil.sendData(ESCUtil.nextLine(3));

        }
    }
}
