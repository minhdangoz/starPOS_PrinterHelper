package com.starpos.printerhelper.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.starpos.printerhelper.R;
import com.starpos.printerhelper.utils.BitmapUtil;
import com.starpos.printerhelper.utils.BluetoothUtil;
import com.starpos.printerhelper.utils.ESCUtil;
import com.starpos.printerhelper.utils.starPOSPrintHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.BitSet;
import java.util.Locale;

import sunmi.sunmiui.dialog.DialogCreater;
import sunmi.sunmiui.dialog.ListDialog;

/**
 * Example of printing pic
 *
 * @author Jimmy
 * <p>
 * ---------------------------------------------------
 * |                   [Your Company Logo]             |
 * |                                                   |
 * |                   [Company Name]                  |
 * |               [Address Line 1]                     |
 * |               [Address Line 2]                     |
 * |               [City, State, Zip Code]              |
 * |               [Phone Number]                       |
 * |               [Email Address]                      |
 * |                                                   |
 * ---------------------------------------------------
 * <p>
 * ---------------------------------------------------
 * |                      Receipt                      |
 * |                                                   |
 * | Order Number: XYZ12345                            |
 * | Date: [Date and Time]                             |
 * | Cashier: [Cashier Name]                            |
 * |                                                   |
 * ---------------------------------------------------
 * <p>
 * ---------------------------------------------------
 * |                    List of Items                   |
 * | ------------------------------------------------- |
 * | Item                 | Quantity |   Price   | Total |
 * | -------------------- | -------- | --------- | ----- |
 * | Product 1            |     2    | $10.00    | $20.00|
 * | Product 2            |     1    | $25.00    | $25.00|
 * | Product 3            |     3    | $8.50     | $25.50|
 * |                                                    |
 * |                                  Subtotal: $70.50   |
 * |                                  Tax (8%): $5.64   |
 * |                                  Total: $76.14     |
 * |                                                    |
 * ---------------------------------------------------
 * <p>
 * ---------------------------------------------------
 * |                        Thank You                    |
 * |              We appreciate your business!           |
 * |            Please come back again soon!             |
 * |                      [Your Company]                 |
 * |                      [Website URL]                  |
 * ---------------------------------------------------
 */
public class HtmlActivity extends BaseActivity {
    private Bitmap mBitmapContent;
    private WebView mWebview;
    public static final int PRINT_WIDTH_365B = 560;
    public static final int PRINT_WIDTH_65 = 380;
    public static final int PRINT_WIDTH_80 = 580;
    public static final int PRINT_WIDTH_80_SEWOO = 480;
    public static final int PRINT_WIDTH_SGT_88IV = 575;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_html);
        setMyTitle("HTML Bitmap Bill");
        setBack();
        initView();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mWebview = null;
    }

    private void initView() {
        mWebview = findViewById(R.id.webView);

        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.bill_print_width);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mWebview.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;

        String replaceAll = "";

        try{
            InputStream stream = getAssets().open("base_print_bill.html");
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }

            r.close();

            replaceAll = total.toString();

            Log.i("HTML", "--> content: " + replaceAll);
        } catch (IOException e){
            e.printStackTrace();
        }



        if (dimensionPixelSize < PRINT_WIDTH_65) {
            layoutParams.width = PRINT_WIDTH_65;
            replaceAll = replaceAll.replace("initial-scale=1", "initial-scale=" + formatPercentInvoice(PRINT_WIDTH_65 / dimensionPixelSize));
        } else {
            layoutParams.width = dimensionPixelSize;
        }
        mWebview.setLayoutParams(layoutParams);

        mWebview.clearFormData();
        mWebview.getSettings().setAllowFileAccess(true);
//        mWebview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mWebview.getSettings().setJavaScriptEnabled(true);
//        mWebview.getSettings().setUseWideViewPort(true);
//        mWebview.getSettings().setLoadWithOverviewMode(true);
//
//        mWebview.getSettings().setSupportZoom(true);
//        mWebview.getSettings().setBuiltInZoomControls(true);
//        mWebview.getSettings().setDisplayZoomControls(false);

//        mWebview.setDrawingCacheEnabled(false);
        mWebview.buildDrawingCache();



        // mWebview.loadUrl("file:///android_asset/base_print_bill.html");
        mWebview.loadDataWithBaseURL("file://" + getFilesDir(), replaceAll, "text/html; charset=UTF-8", "utf-8", null);

//        mWebview.loadDataWithBaseURL("file://" + getFilesDir(), replaceAll, "text/html; charset=UTF-8", "utf-8", null);
        mWebview.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                Log.e("HTML", "--> load web view onReceivedSslError " + error.toString());
                handler.proceed();
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.e("HTML", "--> load web view onPageStarted");
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                Log.e("HTML", "--> load web view onReceivedHttpError: " + errorResponse);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Log.e("HTML", "--> load web view onReceivedError : " + description);
            }

            @Override // android.webkit.WebViewClient
            public void onPageFinished(WebView webView, String str) {
//                PrintBillActivity.this.findViewById(R.id.tv_print_bill).setVisibility(0);
                Log.e("HTML", "--> load web view onPageFinished :" + str);
            }

            @Override
            public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
//                PrintBillActivity.this.findViewById(R.id.tv_print_bill).setVisibility(8);
                super.onReceivedError(webView, webResourceRequest, webResourceError);
                Log.e("HTML", "--> load web view failed");
            }
        });



    }

    public void onClick(View view) {

//        if (this.mBitmapContent == null) {
        this.mWebview.setScrollbarFadingEnabled(true);
        int width = this.mWebview.getWidth();
        int height = this.mWebview.getHeight();
        this.mWebview.measure(View.MeasureSpec.makeMeasureSpec(0, 0), View.MeasureSpec.makeMeasureSpec(0, 0));
        WebView webView = this.mWebview;
        webView.layout(0, 0, webView.getMeasuredWidth(), this.mWebview.getMeasuredHeight());
        int measuredHeight = this.mWebview.getMeasuredHeight();
        this.mWebview.setDrawingCacheEnabled(true);
        this.mWebview.buildDrawingCache();

        Log.e("HTML", "--> measuredHeight = " + measuredHeight);

        if (measuredHeight == 0) {
            return;
        }
        int height2 = this.mWebview.getMeasuredWidth();

        Log.e("HTML", "--> height2 = " + height2);
        if (height2 == 0) {
            return;
        }
        this.mBitmapContent = Bitmap.createBitmap(this.mWebview.getMeasuredWidth(), measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.mBitmapContent);
        canvas.drawBitmap(this.mBitmapContent, 0.0f, this.mBitmapContent.getHeight(), new Paint());
        this.mWebview.draw(canvas);

        Log.e("HTML", "--> bitmap w = " + mBitmapContent.getWidth());
        Log.e("HTML", "--> bitmap h = " + mBitmapContent.getHeight());
        this.mBitmapContent = BitmapUtil.resizeImage(mBitmapContent, PRINT_WIDTH_65, false);
//        this.mWebview.layout(0, 0, width, height);

//        }

        if (!BluetoothUtil.isBlueToothPrinter) {
            starPOSPrintHelper.getInstance().printBitmap(mBitmapContent, 0);
            starPOSPrintHelper.getInstance().feedPaper();
        } else {
            BluetoothUtil.sendData(ESCUtil.printRasterBitmap(mBitmapContent, 0));

//            BluetoothUtil.sendData(ESCUtil.printBitmap(mBitmapContent));
//
            BluetoothUtil.sendData(ESCUtil.nextLine(2));


        }
    }

    private static final DecimalFormat dfPercen = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public static String formatPercentInvoice(double d) {
        DecimalFormat decimalFormat = dfPercen;
        decimalFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        return decimalFormat.format(d);
    }
}
