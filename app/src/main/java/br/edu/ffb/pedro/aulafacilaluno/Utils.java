package br.edu.ffb.pedro.aulafacilaluno;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Pedro on 09/04/2017.
 */

public class Utils {
    public static void showToastLong(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showToastShort(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
