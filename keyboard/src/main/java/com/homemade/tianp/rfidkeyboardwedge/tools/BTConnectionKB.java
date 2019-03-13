package com.homemade.tianp.rfidkeyboardwedge.tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.homemade.tianp.rfidkeyboardwedge.MainActivity;
import com.homemade.tianp.rfidkeyboardwedge.RFIDFragment;
import com.homemade.tianp.rfidkeyboardwedge.RFIDKeyboardWedge;
import com.thingmagic.Reader;
import com.thingmagic.ReaderConnect;
import com.thingmagic.util.LoggerUtil;

/**
 * Created by tianp on 13 Feb 2018.
 */

public class BTConnectionKB {
    String readerChecked;
    private static RFIDKeyboardWedge mRfidKeyboardWedge;
    private  View mView;
    private static Reader reader = null;
    private static String deviceMAC= null;
    private static String TAG = "ConnectionListener";
    private static ProgressDialog pDialog = null;
    private static String readerName = null;
    public static BTConnectionKB.ReaderConnectionThread readerConnectionThread;

    public BTConnectionKB(RFIDKeyboardWedge rfidKeyboardWedge, View view,Reader mReader) {
        this.mRfidKeyboardWedge = rfidKeyboardWedge;
        this.mView = view;
        this.reader = mReader;
        setupProgressDialog();
    }

    private void setupProgressDialog(){
        pDialog = new ProgressDialog(mRfidKeyboardWedge);
        pDialog.setCancelable(false);
        Window window = pDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    public Reader getConnectedReader() {
        return reader;
    }


    public void connectToBTDevice(String query){


        boolean validPort = true;

        readerName = query.split(" - ")[0];
        query = "tmr:///" + query.split(" - ")[1];

        readerConnectionThread = new BTConnectionKB.ReaderConnectionThread(query, "Connect");
        readerConnectionThread.execute(new Void[0]);
    }
    public void disconnectFromBTDevice(String deviceMAC){
        if (deviceMAC.isEmpty() || deviceMAC.equals(null)){

        }else {
            String query = "tmr:///" + deviceMAC;

            readerConnectionThread = new BTConnectionKB.ReaderConnectionThread(query, "Disconnect");
            readerConnectionThread.execute(new Void[0]);
        }
    }

    private static class ReaderConnectionThread extends AsyncTask<Void, Void, String> {
        private String uriString = "";
        private String operation;
        public static boolean operationStatus = true;

        public ReaderConnectionThread(String requestedQuery, String operation) {
            this.uriString = requestedQuery;
            this.operation = operation;
        }

        protected void onPreExecute() {
            BTConnectionKB.pDialog.setProgressStyle(0);
            if(this.operation.equalsIgnoreCase("Connect")) {
                BTConnectionKB.pDialog.setMessage("Connecting. Please wait...");
            } else {
                BTConnectionKB.pDialog.setMessage("Disconnecting. Please wait...");
            }

            BTConnectionKB.pDialog.show();
        }

        protected String doInBackground(Void... params) {
            Long startTime = Long.valueOf(System.currentTimeMillis());
            String exception = "Exception :";

            try {
                if(this.operation.equalsIgnoreCase("Connect")) {
                    reader = ReaderConnect.connect(this.uriString);
                    LoggerUtil.debug(BTConnectionKB.TAG, "Reader Connected");
                    LoggerUtil.debug("Region", BTConnectionKB.reader.paramGet("/reader/Region/id").toString());
                    BTConnectionKB.reader.paramSet("/reader/Region/id", Reader.Region.NZ);
//                    LoggerUtil.debug("Region", BTConnectionKB.reader.paramGet("/reader/Region/id").toString());
                } else {
                    reader.destroy();
                    LoggerUtil.debug(BTConnectionKB.TAG, "Reader Disconnected");
                }
            } catch (Exception var5) {
                this.operationStatus = false;
                if(!var5.getMessage().contains("Connection is not created") && !var5.getMessage().contains("failed to connect")) {
                    exception = exception + var5.getMessage();
                    //reader.destroy();


                } else {
                    exception = exception + "Failed to connect to " + BTConnectionKB.readerName;
                }

                LoggerUtil.error(BTConnectionKB.TAG, "Exception while Connecting :", var5);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

            return exception;
        }

        protected void onPostExecute(String exception) {
            BTConnectionKB.pDialog.dismiss();
            LoggerUtil.debug(BTConnectionKB.TAG, "** onPostExecute **");
            try {

            }catch (Exception ex){

            }
            if(!this.operationStatus) {
                Toast.makeText(mRfidKeyboardWedge, "Exception occurred: " + exception, Toast.LENGTH_SHORT).show();
                if(this.operation.equalsIgnoreCase("Disconnect")) {

                    BTConnectionKB.mRfidKeyboardWedge.setConnectedReader(null,null);
                }

            } else {
                if(this.operation.equalsIgnoreCase("Connect")) {

                    BTConnectionKB.mRfidKeyboardWedge.setConnectedReader(BTConnectionKB.reader, this.uriString);
                } else {
                    //BTConnectionKB.connectButton.setText("Connect");
                    BTConnectionKB.mRfidKeyboardWedge.setConnectedReader(null, null);
                }
            }

        }
    }
}
