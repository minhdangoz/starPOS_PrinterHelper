package com.starpos.printerhelper.logging;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.starpos.printerhelper.BuildConfig;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

public class FileLoggingTree extends Timber.DebugTree {

    private static final String LOG_TAG = FileLoggingTree.class.getSimpleName();

    private Context mContext;

    public FileLoggingTree(Context context){
     mContext = context;
    }

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        try {
            String path = "Log";
            String fileNameTimeStamp = new SimpleDateFormat("dd-MM-yyyy",
                    Locale.getDefault()).format(new Date());
            String logTimeStamp = new SimpleDateFormat("E MMM dd yyyy 'at' hh:mm:ss:SSS aaa",
                    Locale.getDefault()).format(new Date());
            String fileName = fileNameTimeStamp + ".html";

            // Create file
            File file  = generateFile(mContext, path, fileName);

            // If file created or exists save logs
            if (file != null) {
                FileWriter writer = new FileWriter(file, true);
                writer.append("<p style=\"background:lightgray;\"><strong "
                                + "style=\"background:lightblue;\">&nbsp&nbsp")
                        .append(logTimeStamp)
                        .append(" :&nbsp&nbsp</strong><strong>&nbsp&nbsp")
                        .append(tag)
                        .append("</strong> - ")
                        .append(message)
                        .append("</p>");
                writer.flush();
                writer.close();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG,"Error while logging into file : " + e);
        }
    }

    @Override
    protected String createStackElementTag(StackTraceElement element) {
        // Add log statements line number to the log
        return super.createStackElementTag(element) + " - " + element.getLineNumber();
    }

    /*  Helper method to create file*/
    @Nullable
    private static File generateFile(Context context, @NonNull String path, @NonNull String fileName) {
        File file = null;
        if (isExternalStorageAvailable()) {
            File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    BuildConfig.APPLICATION_ID + File.separator + path);

            boolean dirExists = true;

            if (!root.exists()) {
                dirExists = root.mkdirs();
            }

            if (dirExists) {
                file = new File(root, fileName);
            }
        } else{
            File root = new File(context.getFilesDir() + File.separator + path);

            boolean dirExists = true;

            if (!root.exists()) {
                dirExists = root.mkdirs();
            }

            if (dirExists) {
                file = new File(root, fileName);
            }
        }
        return file;
    }

    /* Helper method to determine if external storage is available*/
    private static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}