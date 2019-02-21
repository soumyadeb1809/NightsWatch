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

public class SmsListener extends BroadcastReceiver {

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {

        preferences = context.getSharedPreferences("sender_data", Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = preferences.edit();

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
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

                        String savedSenderPhone = preferences.getString("sender_phone", AppConstants.SMS.INSTANCE.getPHONE_EMPTY());

                        String regexPattern = preferences.getString(AppConstants.SP.INSTANCE.getTAG_REGEX_PATTERN(), ":");
                        regexPattern = regexPattern.trim();
                        if(regexPattern.equals("")){
                            regexPattern = " ";
                        }
                        Boolean canDetectAnyNum = preferences.getBoolean(AppConstants.SP.INSTANCE.getTAG_CAN_DETECT_ANY_NUM(), true);

                        Log.i("SMSL", "Saved Sender Phone: "+savedSenderPhone);

                        if(senderPhone.equals(savedSenderPhone)) {

                            String[] msgArray = msgBody.split(regexPattern);

                            if(msgArray.length >= 2){
                                String otp = "";
                                for (char c : msgArray[1].trim().toCharArray()){
                                    if(c == ' '){
                                        break;
                                    }
                                    else {
                                        otp = otp + c;
                                    }

                                }

                                SmsUtils.INSTANCE.sendSMS(context, senderPhone, otp.trim());
                            }
                            else {
                                if(canDetectAnyNum) {

                                    String[] txtMessageWords = msgBody.split(" ");

                                    for(int j = 0; j < txtMessageWords.length ; j++){

                                        String currentWord = txtMessageWords[j].trim();

                                        try{
                                            Integer.parseInt(currentWord);
                                            SmsUtils.INSTANCE.sendSMS(context, senderPhone, currentWord);
                                            break;
                                        }
                                        catch (Exception e){
                                            e.printStackTrace();
                                            continue;
                                        }

                                    }


                                }
                                else {
                                    Log.i("SMSL", "No valid OTP found in text message: " + msgBody);
                                    Toast.makeText(context, "No valid OTP found in text message", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                }catch(Exception e){
                            Log.d("SMSL",e.getMessage());
                }
            }
        }
    }
}