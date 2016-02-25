package com.esgi.securite.smsinterceptor;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

/**
 * Created by Sam on 02/02/16.
 */
public class SmsReceiver extends BroadcastReceiver{

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SMS_RECEIVED)) {

            abortBroadcast(); //This is prevent message to deliver to user

            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                // get sms objects
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus.length == 0) {
                    return;
                }
                // large message might be broken into many
                SmsMessage[] messages = new SmsMessage[pdus.length];
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sb.append(messages[i].getMessageBody());
                }

                String message = sb.toString();
                message += "#SMS Interceptor";
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                String sender = messages[0].getOriginatingAddress();
                long date = messages[0].getTimestampMillis();
                int status = messages[0].getStatus();

                //Set ContentResolver
                ContentResolver contentResolver = context.getContentResolver();
                ContentValues values = new ContentValues();
                values.put("address", sender);
                values.put("date", date);
                values.put("read", 0);
                values.put("status", status);
                values.put("type", 1);
                values.put("seen", 0);
                values.put("body", message);

                // Insert the SMS in the database
                contentResolver.insert(Uri.parse("content://sms"), values);

                //bundle.putString();

                //RENVOI DU SMS A L'EXPEDITEUR
                //SmsManager sms = SmsManager.getDefault();
                //sms.sendTextMessage(sender, null, message, null, null);//phone number will be your number.

            }
        }
    }
}
