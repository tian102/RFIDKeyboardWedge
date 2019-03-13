package com.homemade.tianp.rfidkeyboardwedge;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.homemade.tianp.rfidkeyboardwedge.rfid_tools.*;
import com.homemade.tianp.rfidkeyboardwedge.tools.BTConnectionKB;
import com.homemade.tianp.rfidkeyboardwedge.tools.FragmentMethods;
import com.homemade.tianp.rfidkeyboardwedge.tools.ServiceListenerRFID;
import com.thingmagic.Reader;
import com.thingmagic.util.LoggerUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.view.KeyEvent.ACTION_DOWN;

/**
 * Created by tianp on 10 Feb 2018.
 */

public class RFIDKeyboardWedge extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private ProgressDialog mProgressDialog;




    public static Reader reader = null;
    private static ServiceListenerRFID.ReadThread readThread;
    public static String deviceMAC = null;
    private KeyboardView kv;
    private Keyboard keyboard;
    private boolean caps = false;
    private boolean syms = false;
    private boolean scan = false;
    public static Keyboard.Key readRFIDkey = null;
    public static InputConnection inputConnection;
    public static View mInputView;

    /** shiftCaps
     *
     * @param shiftCaps     State 0: Caps = false (Shift unpressed)
     *                      State 1: Caps = true (Shift pressed once)   (Revert to State 0 if any
     *                                                                   other key instead of shift
     *                                                                   is pressed)
     *                      State 2: Caps = true (Caps lock is on)
     *                      State 3: Caps = false (Caps lock is of) (Reverts to State 0)
     *
     * */
    private int shiftCaps = 0;
    public static Keyboard.Key rfidTextButton;
    public static Keyboard.Key rfidIconButton;

    @Override
    public View onCreateInputView() {

        syms = false;
        shiftCaps = 0;
        InputMethodManager keyboard = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(mInputView, 0);
        //CheckAndDisp(1);

        return createKeyboard1();
    }

    @Override
    public void onPress(int primaryCode) {

    }


    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == 224)){

            clearID();
            scanRFID();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        inputConnection = getCurrentInputConnection();
        playClick(primaryCode);

        switch(primaryCode){
            case Keyboard.KEYCODE_DELETE:
                inputConnection.deleteSurroundingText(1,0);
                break;

            case Keyboard.KEYCODE_SHIFT:
                shiftCaps++;
                if(shiftCaps == 2){

                }else if (shiftCaps == 3) {
                    shiftCaps = 0;
                    caps = false;
                    keyboard.setShifted(caps);
                    kv.invalidateAllKeys();
                }else {
                    caps = !caps;
                    keyboard.setShifted(caps);
                    kv.invalidateAllKeys();
                }
                break;case 10:
                inputConnection.sendKeyEvent(new KeyEvent(ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;

            case Keyboard.KEYCODE_DONE:
                inputConnection.sendKeyEvent(new KeyEvent(ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;

            case -113:
                // Switch to symbols layout
                syms = !syms;
                caps = false;
                scan = false;
                createKeyboard();
                break;

            case 13:
                // Scan barcode
                clearID();
                barcodeScanButton();
                break;

            case 14:
                // Scan tag
                clearID();
                tagScanButton();
                break;

            case 15:
                //Print to screen
                CheckAndDisp(2);
                break;

            case 16:
                // Switch to scan layout
                scan = !scan;
                syms = false;
                caps = false;
                createKeyboard();
                break;

            case 17:
                // Scan barcodes sequentially
                clearID();
                continuousCaptureButton();
                break;

            case 18:
                // Scan tag
                clearID();
                tagScanButton();
                break;

            case 19:
                // Print array to screen
                CheckAndDisp(4);
                break;

            case 213:
                // Scan with connected reader
                clearID();
                scanRFID();

                break;

            case 215:
                // Connect to reader key
                connectToBTReader();
                break;

            case 214:
                // Settings 'gear' key
                openMainApp();
                break;

            case 216:
                // Dialog for previous tag scanned
                openPreviousTagDialog();
                break;

            default:

                char code = (char) primaryCode;
                if(Character.isLetter(code) && caps){
                    code = Character.toUpperCase(code);
                }
                if((primaryCode != 13) || (primaryCode != 14) || (primaryCode != 15) || (primaryCode != 16) || (primaryCode != 16)){
                    ToScreen(code);
                }
                if(shiftCaps==1){
                    shiftCaps = 0;
                    caps = false;
                    keyboard.setShifted(caps);
                    kv.invalidateAllKeys();
                }
                break;
        }
    }

    private void openPreviousTagDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        int mode = Integer.parseInt(prefs.getString("list_readmode","0"));
        switch(mode){
            case 0:
                if (LastScannedTags.getmGen2Tag() != null) {
                    try {
                        LastScannedTags.displayScannedTagDialog(this,mInputView,LastScannedTags.getmGen2Tag());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(this, "No previous data to display", Toast.LENGTH_SHORT).show();
                }
                break;

            case 1:
                if (LastScannedTags.getmGen2Tags() != null) {
                    try {
                        LastScannedTags.displayScannedTagsDialog(this,mInputView,LastScannedTags.getmGen2Tags());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(this, "No previous data to display", Toast.LENGTH_SHORT).show();
                }
                break;

            case 2:
                if (LastScannedTags.getmAtaTag() != null) {
                    try {
                        LastScannedTags.displayScannedTagDialog(this,mInputView,LastScannedTags.getmAtaTag());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(this, "No previous data to display", Toast.LENGTH_SHORT).show();
                }
                break;

            default:

                break;
        }

    }


    @Override
    public void sendKeyChar(char charCode) {
        super.sendKeyChar(charCode);
    }

    @Override
    public void updateInputViewShown() {
        CheckAndDisp(1);
        super.updateInputViewShown();
    }

    public static boolean isReaderActive() {
        return reader != null;
    }

    /** Used to choose between layouts e.g. Alphabet and Symbols*/
    private void createKeyboard(){
        createKeyboardLayout();
        this.setInputView(mInputView);
    }

    /** Used only to set keyboard at first launch
     * */
    private View createKeyboard1(){
        createKeyboardLayout();
        return kv;
    }

    private void createKeyboardLayout(){
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard,null);
        if(syms){
            keyboard = new Keyboard(this, R.xml.symbols);
        }else {
            keyboard = new Keyboard(this, R.xml.qwerty);
        }
        if(scan){
            keyboard = new Keyboard(this, R.xml.scanner);
            rfidTextButton = findKey(keyboard,213);
            rfidIconButton = findKey(keyboard,215);
            alterKeyboard(keyboard);
        }
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        int imeOptions = getCurrentInputEditorInfo().imeOptions;
        switch (imeOptions& EditorInfo.IME_MASK_ACTION) {
            case EditorInfo.IME_ACTION_NONE:

            case EditorInfo.IME_ACTION_GO:
                // Go
            case EditorInfo.IME_ACTION_SEARCH:
                // Search
            case EditorInfo.IME_ACTION_SEND:
                // Send
            case EditorInfo.IME_ACTION_NEXT:
                // Next
            case EditorInfo.IME_ACTION_DONE:
                // Done
        }
        mInputView = kv;
    }

    public String getDeviceMAC() {
        return deviceMAC;
    }

    private void openMainApp(){
        Intent intent =  new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void scanRFID(){

        if(getConnectedReader() != null){
            /*Service ListenerMethod*//*
            try {
                //Todo: add that reader is connected

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
               // ServiceListenerRFID serviceListenerRFID = new ServiceListenerRFID();

                ServiceListenerRFID serviceListenerRFID = new ServiceListenerRFID(this, prefs);
            } catch (Exception var5) {
                readRFIDkey.icon = getResources().getDrawable(R.drawable.ic_devices_white_24dp);
                readRFIDkey.label = "RFID";
                LoggerUtil.error(TAG, "Exception", var5);
            }*/



            /*ReadSingleGen2Tag.java method*/
            try {
                //Todo: add that reader is connected

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);



                /*
                 * Scanning done based on settings:
                 *
                 * @param mode switch between scanning modes
                 *             0:   Read single GEN2 tag
                 *             1:   Read multiple GEN2 tags
                 *             2:   Read single ATA tag
                 */


                int mode = Integer.parseInt(prefs.getString("list_readmode","0"));
                //reader.paramSet();

                switch (mode){
                    case 0:
                        /*
                         * TODO: If protocol set to 'GEN2' and is in 'sync' read mode
                         * @param   readGen2Tag   Read single GEN2 Tag
                         */
                        ReadSingleGen2Tag readGen2Tag = new ReadSingleGen2Tag(this,mInputView,getConnectedReader(),prefs);
                        break;
                    case 1:
                        /*
                         * TODO: If protocol set to 'GEN2' and is in 'async' read mode
                         * @param   readGen2Tag   Read multiple GEN2 Tags
                         */
                        ReadMultipleGen2Tag_Filtered readMultipleGen2Tag_Filtered = new ReadMultipleGen2Tag_Filtered(this,mInputView,getConnectedReader(),prefs);
                        break;
                    case 2:
                        /*
                         * TODO: If protocol set to decode 'multiprotocol'
                         * @param   multiProtocolRead   Decode ATA RFID Tag
                         */
                        AEITagDecodingFast ataTag = new AEITagDecodingFast(this,mInputView,getConnectedReader(),prefs);

                        break;
                    case 3:
                        /*
                         * TODO: If protocol set to 'GEN2' and is in 'async' read mode
                         * @param   readGen2Tag   Read multiple GEN2 Tags
                         */
                        ReadMultipleGen2Tag readMultipleGen2Tag = new ReadMultipleGen2Tag(getConnectedReader(),prefs);
                        break;
                    case 4:
                        /*
                         * TODO: If protocol set to 'multiprotocol'
                         * @param   multiProtocolRead   Read ATA RFID Tag
                         */
                        MultiProtocolRead multiProtocolRead = new MultiProtocolRead(getConnectedReader(),prefs);
                        break;
                }
            } catch (Exception var5) {
                rfidIconButton.icon = getResources().getDrawable(R.drawable.ic_devices_white_24dp);
                rfidTextButton.label = "RFID";
                LoggerUtil.error(TAG, "Exception", var5);
            }
        }else{
            Toast.makeText(this, "Reader not connected", Toast.LENGTH_SHORT).show();
            //Todo: add that reader is disconnected
            try {
                rfidIconButton.icon = getResources().getDrawable(R.drawable.ic_devices_white_24dp);
                rfidTextButton.label = "RFID";
            }catch (Exception ex){
                Log.d("EXCEPTION ","RfidKeyboardWedge");
            }
        }
    }



    public void connectToBTReader(){
        if(reader != null){
            //ServiceListenerRFID.stopReadTagThread();
            FragmentMethods.disconnectBTDevice(this,mInputView);
        }else{
            if(!FragmentMethods.getBluetoothStatus(this)){
                Toast.makeText(this, "Bluetooth not enabled.", Toast.LENGTH_LONG).show();
            }else{
                FragmentMethods.connectToBTDialog(this,mInputView,reader);
            }
        }
    }

    public static Reader getConnectedReader() {
        return reader;
    }

    public void setConnectedReader(Reader mReader, String mDeviceMAC) {
        reader = mReader;
        deviceMAC = mDeviceMAC;
        alterKeyboard(keyboard);
        createKeyboard();
    }

    /** Alter keycode-specific keyboard.key
    * TODO: #1 - Fix Start Reading Tag on single mode ReadSingleGen2Tag*/
    public void alterKeyboard(Keyboard keyboard){
        checkKeyboard(keyboard);
//        List<Keyboard.Key> keys = null;
//        if (keyboard != null) {
//            keys = keyboard.getKeys();
//        }
//
//        if (keys != null) {
//            for (Keyboard.Key key : keys) {
//                if (key.codes[0] == 213){
//                    readRFIDkey = key;
//                }
//                else if (key.codes[0] == 215) {
//                    if (reader != null || FragmentMethods.getBluetoothStatus(this,mInputView) != null || getConnectedReader() != null){
//                        key.icon = getResources().getDrawable(R.drawable.ic_devices_green_24dp);
//                        readRFIDkey.label = "Start Reading";
//                    }else{
//                        readRFIDkey.label = "RFID";
//                        key.icon = getResources().getDrawable(R.drawable.ic_devices_white_24dp);
//                    }
//                }
//            }
//        }
    }
    private static Keyboard.Key findKey(Keyboard keyboard, int primaryCode) {
        for (Keyboard.Key key : keyboard.getKeys()) {
            if (key.codes[0] == primaryCode) {
                return key;
            }
        }
        return null;
    }

    public static void checkKeyboard(Keyboard kb){

        if (reader != null || getConnectedReader() != null){
            rfidIconButton.icon = mInputView.getContext().getResources().getDrawable(R.drawable.ic_devices_green_24dp);
            rfidTextButton.label = "Start Reading";
        }else{
            rfidIconButton.icon = mInputView.getContext().getResources().getDrawable(R.drawable.ic_devices_white_24dp);
            rfidTextButton.label = "RFID";
        }
    }

    public static void alterKeyboard(RFIDKeyboardWedge rfidKeyboardWedge){
        List<Keyboard.Key> keys = null;
        Keyboard mKeyboard= rfidKeyboardWedge.keyboard;
        if (mKeyboard != null) {
            keys = mKeyboard.getKeys();
        }

        if (keys != null) {
            for (Keyboard.Key key : keys) {
                if (key.codes[0] == 213){
                    readRFIDkey = key;
                }
                else if (key.codes[0] == 215) {
                    if (reader != null || FragmentMethods.getBluetoothStatus(rfidKeyboardWedge,rfidKeyboardWedge.mInputView, reader) != null){
                        key.icon = rfidKeyboardWedge.getResources().getDrawable(R.drawable.ic_devices_green_24dp);
                        readRFIDkey.label = "Start Reading";
                    }else{
                        key.icon = rfidKeyboardWedge.getResources().getDrawable(R.drawable.ic_devices_white_24dp);
                    }
                }
            }
        }
    }

    /** Check if scan successful and display if true
     * */
    private void CheckAndDisp(int option){
        if (option == 1) {
            if (!ScanResult.GetProductID().equals("Default ID")) {
                String result = ScanResult.GetProductID();
                StringToWedge(result);
                ScanResult.SetProductID("Default ID");
            }
        }else if(option == 2){
            if(!ScanResult.GetHistoryProductID().equals("Default ID")){
                String result = ScanResult.GetHistoryProductID();
                StringToWedge(result);
            }
        }
    }


    /** Prints string to screen
     * */
    public static void StringToWedge(String string){
        for(int i = 0; i < string.length(); i++){
            ToScreen(string.charAt(i));

        }
    }

    /** Prints character to screen
     * */
    public static void ToScreen(char code){
        //sendKeyChar(code);
        inputConnection.commitText(String.valueOf(code),1);
    }

    /**Keyboard sound effects
     * */
    private void playClick(int keyCode){
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        switch (keyCode){
            case 32:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case 10:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    /** Start the barcode scanner
     * */
    private void barcodeScanButton(){

        Intent intent = new Intent(RFIDKeyboardWedge.this, BarcodeScanner.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }




    /** Start the 'multiple barcodes' scanner
     * */
    private void continuousCaptureButton(){
        Intent intent = new Intent(this, ContinuousCaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);

    }

    /** Start the tag scanner
     * */
    private void tagScanButton(){
        Intent intent = new Intent(this, NFCTagScanner.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void scanResultToJSON(ScanResult result){
        Toast.makeText(this, ScanResult.GetProductID(), Toast.LENGTH_SHORT).show();
    }

    /** Clear scan result
     * */
    private void clearID(){
        ScanResult.resetAll();
    }


    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {


    }

    @Override
    public void swipeUp() {

    }

}
