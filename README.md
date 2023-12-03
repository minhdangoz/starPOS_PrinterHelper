starPOS Printer Helper
==========
### 
Please be sure to read the documentation before using
###
We strongly recommend developers to use Android Studio to develop.The demo for Android Studio has full functionality, such as printing barcodes, printing qr code, printing text, printing pictures and printing tables.

## Instruction

* FunctionActivity
  Home page of each function module
* AllActivity
  Print all ESC command supported by Sunmi Printer
* QrActivity api to print Qr Code
* BarCodeActivity 
Call api to print bar code
* TextActivity 
Call api to print text
* TableActivity 
Call api to print table， 
  Bluetooth only plays table graphics
* HtmlActivity 
Call api to print bitmap html，
* BitmapActivity 
Call api to print image,picture
* BufferActivity 
Call api to print a simple small ticket content, and You can get the print result via transaction mode
* BlackLabelActivity 
Call api to print on black label paper
* LabelActivity 
Call api to print on "label paper"

### Connect to the printer
``` Java
private void init(){
    starPOSPrintHelper.getInstance().initStarPOSPrinterService(this);
    BluetoothUtil.connectBlueTooth(this);
}
```

### Print text
``` Java
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
```
### Print html content
*Recommended approach for all type and all kind of printer*
```Java
mBitmapContent = Bitmap.createBitmap(mWebview.getMeasuredWidth(), measuredHeight, Bitmap.Config.ARGB_8888);
Canvas canvas = new Canvas(this.mBitmapContent);
canvas.drawBitmap(this.mBitmapContent, 0.0f, mBitmapContent.getHeight(), new Paint());
mWebview.draw(canvas);

// 380px is ~6.5 cm print paper width
mBitmapContent = BitmapUtil.resizeImage(mBitmapContent, 380, false);

BluetoothUtil.sendData(ESCUtil.printRasterBitmap(mBitmapContent, 0));
BluetoothUtil.sendData(ESCUtil.nextLine(2));
```