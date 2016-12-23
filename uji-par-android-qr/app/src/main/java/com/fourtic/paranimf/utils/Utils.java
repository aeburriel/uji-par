package com.fourtic.paranimf.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.fourtic.paranimf.entradas.activity.SettingsActivity;
import com.fourtic.paranimf.entradas.constants.Constants;

public class Utils
{
	private static SimpleDateFormat HOUR_FORMAT = new SimpleDateFormat("HH:mm");
    private static SimpleDateFormat WEEKDAY_FORMAT = new SimpleDateFormat("EEEE");
    private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd MMMM yyyy");
    private static SimpleDateFormat DATE_WITH_TIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static SimpleDateFormat DATE_FORMAT_SHORT = new SimpleDateFormat("dd/MM/yyyy");

    // private static SimpleDateFormat WEEKDAY_NAME = new SimpleDateFormat("cccc");
    // private static SimpleDateFormat MONTH_NAME = new SimpleDateFormat("LLLL");

    public static byte[] inputStreamToByteArray(InputStream is) throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1)
        {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public static String inputStreamToString(InputStream is) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        while ((line = reader.readLine()) != null)
            sb.append(line + System.getProperty("line.separator"));

        return sb.toString();
    }

    public static byte[] getMD5FromFile(File file) throws Exception
    {
        InputStream is = new FileInputStream(file);

        return getMD5FromInputStream(is);
    }

    public static byte[] getMD5FromInputStream(InputStream is) throws Exception
    {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1)
        {
            digest.update(data, 0, nRead);
        }

        return digest.digest();
    }

    public static void fromInputStreamToOutputStream(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }

    public static float convertDpToPixel(float dp, Context context)
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static int getResourceId(Context context, String resourceType, String resourceName)
    {
        String packageName = context.getPackageName();
        return context.getResources().getIdentifier(resourceName, resourceType, packageName);
    }

    public static String getStringResourceByName(Context context, String stringResourceName)
    {
        int resId = getResourceId(context, "string", stringResourceName);
        return context.getString(resId);
    }

    public static boolean lenguajeSoportado(String language)
    {
        return language.equals("es") || language.equals("ca");
    }

    synchronized public static String formatHour(Date date)
    {
        return HOUR_FORMAT.format(date);
    }

    synchronized public static String formatWeekday(Date date)
    {
        return WEEKDAY_FORMAT.format(date);
    }

    synchronized public static String formatDate(Date date)
    {
        return DATE_FORMAT.format(date);
    }

    synchronized public static String formatDateWithTime(Date date)
    {
        return DATE_WITH_TIME_FORMAT.format(date);
    }

    synchronized public static String formatDateShort(Date date)
    {
        return DATE_FORMAT_SHORT.format(date);
    }

    synchronized public static Date parseDateShort(String date) throws ParseException
    {
        return DATE_FORMAT_SHORT.parse(date);
    }

    synchronized public static Date parseDateWithTime(String string) throws ParseException
    {
        return DATE_WITH_TIME_FORMAT.parse(string);
    }

    @SuppressWarnings("deprecation")
    public static CountDownTimer playSoundAndvibrate(Context context, int soundResource, int millisDuration)
    {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, Constants.TAG);

        CountDownTimer timer = null;

        try
        {
            wl.acquire();

            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(1000);

            timer = playSound(context, soundResource, millisDuration);
        }
        catch (Exception e)
        {
            Log.e(Constants.TAG, "Error reproduciendo sonido de notificación en aviso de cita", e);
        }
        finally
        {
            wl.release();
        }

        return timer;
    }

    private static CountDownTimer playSound(Context context, int soundResource, int millisDuration)
    {
        final MediaPlayer mPlayer = MediaPlayer.create(context, soundResource);

        CountDownTimer timer = new CountDownTimer(millisDuration, 2000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {
                mPlayer.start();
            }

            @Override
            public void onFinish()
            {
            }
        };

        timer.start();

        return timer;
    }
}
