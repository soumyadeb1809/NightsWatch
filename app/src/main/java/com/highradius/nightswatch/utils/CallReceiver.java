package com.highradius.nightswatch.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.telecom.TelecomManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

public class CallReceiver extends BaseCallReceiver {
    @Override
    protected void onIncomingCallReceived(final Context ctx, String number, Date start) {
        Log.d("SMSL", "Incoming call from: " + number);
        Toast.makeText(ctx, "Incoming call from: " + number, Toast.LENGTH_LONG).show();

        try {

            TelecomManager tm = (TelecomManager) ctx
                    .getSystemService(Context.TELECOM_SERVICE);

            if (tm == null) {
                // whether you want to handle this is up to you really
                throw new NullPointerException("tm == null");
            }

            if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                Log.d("SMSL", "Permission denied!!!");
                return;
            }
            tm.acceptRingingCall();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    dialNumber(ctx);
                    //dial(ctx, ",,#");
                }
            }, 5000);

        }
        catch(Exception e){
            Log.e("SMSL", e.getMessage());
        }
    }

    private void dialNumber(Context ctx) {
        Log.d("SMSL", "Dailing number...");
        Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel://" + "#,#"));
        call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.getApplicationContext().startActivity(call);
        Log.d("SMSL", "Complete!");
    }

    /**
     * Dials a number with DTMF chars
     * Note: When the number is dialed, only the initial number is displayed on the device dialer
     * For example: dial("*6900,,1") will display only *6900 on the device dialer (but the rest will also be processed)
     * @param number
     */
    public void dial(Context ctx, String number) {

        try {
            number = new String(number.trim().replace(" ", "%20").replace("&", "%26")
                    .replace(",", "%2c").replace("(", "%28").replace(")", "%29")
                    .replace("!", "%21").replace("=", "%3D").replace("<", "%3C")
                    .replace(">", "%3E").replace("#", "%23").replace("$", "%24")
                    .replace("'", "%27").replace("*", "%2A").replace("-", "%2D")
                    .replace(".", "%2E").replace("/", "%2F").replace(":", "%3A")
                    .replace(";", "%3B").replace("?", "%3F").replace("@", "%40")
                    .replace("[", "%5B").replace("\\", "%5C").replace("]", "%5D")
                    .replace("_", "%5F").replace("`", "%60").replace("{", "%7B")
                    .replace("|", "%7C").replace("}", "%7D"));

            Uri uri = Uri.parse("tel:"+ number);
            Intent intent = new Intent(Intent.ACTION_CALL, uri);
            ctx.startActivity(intent);

        } catch (Exception e) {
            //getAlertDialog().setMessage("Invalid number");
            e.printStackTrace();
        }
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {

    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {

    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Toast.makeText(ctx, "Outgoing call to: " + number, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {

    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {

    }
}
