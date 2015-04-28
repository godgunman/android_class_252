package com.example.simpleui;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ggm on 4/28/15.
 */
public class Utils {

    public static void writeFile(Context context, String fileName, String fileContent) {
        try {
            FileOutputStream fos
                    = context.openFileOutput(fileName, Context.MODE_APPEND);
            fos.write(fileContent.getBytes());
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
