package com.highradius.nightswatch.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.highradius.nightswatch.constants.AppConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmsListener extends BroadcastReceiver {

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {

        preferences = context.getSharedPreferences("sender_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        StringBuilder logBuilder = new StringBuilder();

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){

//            Toast.makeText(context, "SMS Recieved!", Toast.LENGTH_SHORT).show();
            logBuilder.append("SMS Recieved!\n");

            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            String senderPhone;
            if (bundle != null){
                //---retrieve the SMS message received---
                try{
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for(int i=0; i<msgs.length; i++){
                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
                        senderPhone = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        //Toast.makeText(context, "Sender: "+senderPhone + " Message:"+msgBody, Toast.LENGTH_LONG).show();

                        Log.i("SMSL", "Sender: "+senderPhone + " Message:"+msgBody);
//                        Toast.makeText(context, "Received Message from " + senderPhone, Toast.LENGTH_SHORT).show();
                        logBuilder.append("Received Message from " + senderPhone+"\n");

                        String savedSenderPhone = preferences.getString("sender_phone", AppConstants.SMS.INSTANCE.getPHONE_EMPTY());

                        String regexPattern = preferences.getString(AppConstants.SP.INSTANCE.getTAG_REGEX_PATTERN(), ":");
                        regexPattern = regexPattern.trim();
                        if(regexPattern.equals("")){
                            regexPattern = " ";
                        }
                        Boolean canDetectAnyNum = preferences.getBoolean(AppConstants.SP.INSTANCE.getTAG_CAN_DETECT_ANY_NUM(), true);

                        Log.i("SMSL", "Saved Sender Phone: "+savedSenderPhone);
 //                       Toast.makeText(context, "Saved Sender Phone: "+savedSenderPhone, Toast.LENGTH_SHORT).show();
                        logBuilder.append("Saved Sender Phone: "+savedSenderPhone +"\n");

                        List<String> allSavedPhones = Arrays.asList(savedSenderPhone.split(","));

                        if(null != allSavedPhones && !allSavedPhones.isEmpty() && allSavedPhones.contains(senderPhone)) {
//                            Toast.makeText(context, "Numbers Matched!", Toast.LENGTH_SHORT).show();
                            logBuilder.append("Numbers Matched!" +"\n");
                            String[] msgArray = msgBody.split(regexPattern);

                            if(msgArray.length >= 2){
                                String otp = "";
                                for (char c : (msgArray[0]+" "+msgArray[1]).trim().toCharArray()){
                                    if(c == ' '){
                                        try{
                                            Integer.parseInt(otp);
                                            break;
                                        }
                                        catch (NumberFormatException | NullPointerException nfe){
                                           nfe.printStackTrace();
                                           otp = "";
                                        }
                                    }
                                    else {
                                        otp = otp + c;
                                    }

                                }

                                SmsUtils.INSTANCE.sendSMS(context, senderPhone, otp.trim());
                                Toast.makeText(context, "SMS Sent for OTP: "+otp.trim(), Toast.LENGTH_SHORT).show();
                                logBuilder.append("SMS Sent for OTP: "+otp.trim() +"\n");
                            }
                            else {
                                if(canDetectAnyNum) {

                                    String[] txtMessageWords = msgBody.split(" ");
                                    boolean otpFound = false;

                                    for(int j = 0; j < txtMessageWords.length ; j++){

                                        String currentWord = txtMessageWords[j].trim();

                                        try{
                                            Integer.parseInt(currentWord);
                                            SmsUtils.INSTANCE.sendSMS(context, senderPhone, currentWord);
                                            Toast.makeText(context, "SMS Sent for OTP: "+currentWord, Toast.LENGTH_SHORT).show();
                                            logBuilder.append("SMS Sent for OTP: "+currentWord +"\n");
                                            otpFound = true;
                                            break;
                                        }
                                        catch (Exception e){
                                            e.printStackTrace();
                                            continue;
                                        }

                                    }

//                                    Toast.makeText(context, "OTP Found: " + otpFound, Toast.LENGTH_SHORT).show();
                                    logBuilder.append("OTP Found: " + otpFound +"\n");
                                }
                                else {
                                    Log.i("SMSL", "No valid OTP found in text message: " + msgBody);
                                    Toast.makeText(context, "No valid OTP found in text message", Toast.LENGTH_LONG).show();
                                    logBuilder.append("No valid OTP found in text message"+"\n");
                                }
                            }
                        }
                        else{
                            Log.d("SMSL", "Number not found: " + senderPhone);
                            logBuilder.append("Number not found: " + senderPhone +"\n");
                        }

                    }
                }catch(Exception e){
                    Log.d("SMSL",e.getMessage());
//                    Toast.makeText(context, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    logBuilder.append("ERROR: " + e.getMessage()+"\n");
                }
            }
        }

        preferencesEditor.putString("logs", logBuilder.toString());
        preferencesEditor.commit();
    }
}