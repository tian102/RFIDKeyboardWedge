/**
 * Class for managing application fragments
 */
package com.homemade.tianp.rfidkeyboardwedge.tools;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.homemade.tianp.rfidkeyboardwedge.R;
import com.homemade.tianp.rfidkeyboardwedge.RFIDKeyboardWedge;
//import com.homemade.tianp.rfidkeyboardwedge.SettingsActivity;
import com.homemade.tianp.rfidkeyboardwedge.rfid_tools.AEITagDecoding;
import com.homemade.tianp.rfidkeyboardwedge.rfid_tools.AEITagDecodingFast;
import com.homemade.tianp.rfidkeyboardwedge.rfid_tools.ReadSingleGen2Tag;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.rfidreader.BluetoothService;
import com.thingmagic.rfidreader.ReaderActivity;
import com.thingmagic.rfidreader.services.UsbService;
import com.thingmagic.util.LoggerUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import aeitagdecoder.Railcar;

/**
 * Created by tianp on 13 Feb 2018.
 */

public class FragmentMethods {

    public static BTConnectionKB bluetoothConn;

    public static Spinner listBTDevices(Spinner spinner){
        try {
            InputMethodManager imm = (InputMethodManager)spinner.getContext().getSystemService(spinner.getContext().INPUT_METHOD_SERVICE);
            BluetoothService bluetoothService = new BluetoothService();
            boolean btEnabled = bluetoothService.checkBTState(spinner.getContext(), null);

            ArrayAdapter adapter = new ArrayAdapter(spinner.getContext(), android.R.layout.simple_spinner_dropdown_item);
            if(btEnabled) {
                Set<BluetoothDevice> pairedDevices = bluetoothService.getPairedDevices();
                Iterator var8 = pairedDevices.iterator();

                while(var8.hasNext()) {
                    BluetoothDevice bluetoothDevice = (BluetoothDevice)var8.next();
                    adapter.add(bluetoothDevice.getName() + " - " + bluetoothDevice.getAddress());
                }
            } else {
                Toast.makeText(spinner.getContext(), "BT failed", Toast.LENGTH_SHORT).show();
            }

            LoggerUtil.debug("Modified BT Connection", "Getting USB device list");
            UsbService usbService = new UsbService();
            ReaderActivity readerActivity = new ReaderActivity();
            ArrayList<String> connectedUSBDeviceNames = usbService.getConnectedUSBdevices(readerActivity);
            Iterator var14 = connectedUSBDeviceNames.iterator();

            while(var14.hasNext()) {
                String deviceName = (String)var14.next();
                adapter.add(deviceName);
            }

            spinner.setAdapter(adapter);

        } catch (Exception var11) {
            LoggerUtil.error("ReaderActivity", "Error loading paired bluetooth devices :", var11);
        }
        return spinner;
    }

    // Creates alert dialog which is used to connect to a BT device
    public static void connectToBTDialog(final RFIDKeyboardWedge rfidKeyboardWedge, final View mInputView, final Reader reader){
        AlertDialog.Builder builder = new AlertDialog.Builder(rfidKeyboardWedge);
        builder.setTitle("Select Bluetooth Scanner");

        final String[] listOfBT = listBTDevices(rfidKeyboardWedge.getApplication()).toArray(new String[0]);

        builder.setItems(listOfBT, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                bluetoothConn = new BTConnectionKB(rfidKeyboardWedge, mInputView,reader);
                bluetoothConn.connectToBTDevice(listOfBT[item]);
            }
        });
        AlertDialog alert = builder.create();
        Window window = alert.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mInputView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alert.show();
    }

    // Creates alert dialog which is used to connect to a BT device
    public static Reader getBluetoothStatus(final RFIDKeyboardWedge rfidKeyboardWedge, final View mInputView, Reader mReader){
        BTConnectionKB btConnectionKB = new BTConnectionKB(rfidKeyboardWedge,mInputView,mReader);
        return btConnectionKB.getConnectedReader();
    }


    /*TODO: Version 0.1 of scanning dialog
     */
    // Creates alert dialog which is used to display scanned tags
    public static void displayScannedTags(final RFIDKeyboardWedge rfidKeyboardWedge, final View mInputView, final Reader connectedReader){
        List<String> m_Text = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(rfidKeyboardWedge);
        builder.setTitle("Heading");

        // Set up the input
        final EditText input = new EditText(rfidKeyboardWedge);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                input.setText(ReadSingleGen2Tag.readTag(connectedReader));
            }
        });

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(rfidKeyboardWedge, input.getText(), Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });




        AlertDialog alert = builder.create();
        Window window = alert.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mInputView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        alert.show();
    }


    /*TODO: Version 0.2 of scanning dialog
     */
    // Creates alert dialog which is used to display scanned tags
    public static void scanRFIDTagDialog(final RFIDKeyboardWedge rfidKeyboardWedge, final View mInputView, final Reader connectedReader){
        // create a Dialog component
        final Dialog dialog = new Dialog(rfidKeyboardWedge);

        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.rfid_scan_dialog);
        dialog.setTitle("Android Custom Dialog Box");


        Button dialogButton1 = (Button) dialog.findViewById(R.id.dialogButton1);
        Button dialogButton2 = (Button) dialog.findViewById(R.id.dialogButton2);

        dialogButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AEITagDecoding ATA_Tag = new AEITagDecoding(connectedReader);
                try{

                }catch (Exception ex){
                    System.out.println(ex.getMessage());
                }


            }
        });

        dialogButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = mInputView.getWindowToken();
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.show();
    }



    public static void disconnectBTDevice(final RFIDKeyboardWedge rfidKeyboardWedge, final View mInputView){
        bluetoothConn.disconnectFromBTDevice(rfidKeyboardWedge.getDeviceMAC());

    }


    public static boolean getBluetoothStatus(Context context){
        boolean enabled = false;
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            enabled = false;
        } else {
            enabled = mBluetoothAdapter.isEnabled();
        }
        return enabled;
    }

    private static List<String> listBTDevices(Context context){
        List<String> stringList = new ArrayList<>();
        try {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            BluetoothService bluetoothService = new BluetoothService();
            boolean btEnabled = bluetoothService.checkBTState(context, null);

            if(btEnabled) {
                Set<BluetoothDevice> pairedDevices = bluetoothService.getPairedDevices();
                Iterator var8 = pairedDevices.iterator();

                while(var8.hasNext()) {
                    BluetoothDevice bluetoothDevice = (BluetoothDevice)var8.next();
                    stringList.add(bluetoothDevice.getName() + " - " + bluetoothDevice.getAddress());
                }
            } else {
                Toast.makeText(context, "Bluetooth not Enabled", Toast.LENGTH_SHORT).show();
            }

            LoggerUtil.debug("Modified BT Connection", "Getting USB device list");
            UsbService usbService = new UsbService();
            ReaderActivity readerActivity = new ReaderActivity();
            ArrayList<String> connectedUSBDeviceNames = usbService.getConnectedUSBdevices(readerActivity);
            Iterator var14 = connectedUSBDeviceNames.iterator();

            while(var14.hasNext()) {
                String deviceName = (String)var14.next();
                stringList.add(deviceName);
            }


        } catch (Exception var11) {
            LoggerUtil.error("ReaderActivity", "Error loading paired bluetooth devices :", var11);
        }
        return stringList;
    }

    public static String[] getProtocolKeys(String protocolKeys){
        String[] keys = null;
        try {
            keys = protocolKeys.split(";");

            if (keys.length == 1){
                if (keys[0] == ""){
                    keys = null;
                }
            }else {
                for (int i = 0; i<keys.length; i++) {
                    keys[i] = keys[i].replace(" ", "");
                    keys[i] = keys[i].replace("\n", "");
                }
            }

        }catch (Exception ex){
            Log.d("Exception", ex.toString());
            keys = null;
        }
        return keys;
    }


    /**********************************************************************************************
     Section ahead manages the Preference Pages opened via the SettingsFragment
     **********************************************************************************************/


    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"));
            bindPreferenceSummaryToValue(findPreference("example_list"));
            bindPreferenceSummaryToValue(findPreference("ReadingMode"));

        }


    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ReadWritePreferenceFragment extends PreferenceFragment  {
        public static String[] preferenceKeys_readwriteprefs = {
                "list_readmode",
                "list_region",
                "edittext_protocol",
                "edittext_timeout",
                "edittext_dutycycle_time_on",
                "edittext_dutycycle_time_off",
                "edittext_tag_delimiter",
                "list_read_data"
        };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_1_read_write);
            setHasOptionsMenu(true);


            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.




            for (String s: preferenceKeys_readwriteprefs) {
                bindPreferenceSummaryToValue(findPreference(s));
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // create ContextThemeWrapper from the original Activity Context with the custom theme

            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (view != null) {
                view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
            return view;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PerformanceTuningPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_2_performance_tuning);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // create ContextThemeWrapper from the original Activity Context with the custom theme

            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (view != null) {
                view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
            return view;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DisplayOptionsPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_3_display_options);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // create ContextThemeWrapper from the original Activity Context with the custom theme

            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (view != null) {
                view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
            return view;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ProfilePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_4_profile);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // create ContextThemeWrapper from the original Activity Context with the custom theme

            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (view != null) {
                view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
            return view;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FirmwarePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_5_firmware);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // create ContextThemeWrapper from the original Activity Context with the custom theme

            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (view != null) {
                view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
            return view;
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AboutPreferenceFragment extends PreferenceFragment {
        public static String[] preferenceKeys_aboutprefs = {
                "about_rfid_engine",
                "about_firmware_version",
                "about_hardware_version",
                "about_reader_serial",
                "about_supported_protocols",
                "about_app_version"
        };
        @Override
        public void onCreate(Bundle savedInstanceState) {


            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_6_about);
            setHasOptionsMenu(true);



            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            if (RFIDKeyboardWedge.isReaderActive()){
                try {
                    ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[0])).setText(RFIDKeyboardWedge.reader.paramGet("/reader/version/model").toString());
                    ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[1])).setText(RFIDKeyboardWedge.reader.paramGet("/reader/version/software").toString());
                    ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[2])).setText(RFIDKeyboardWedge.reader.paramGet("/reader/version/hardware").toString());
                    ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[3])).setText(RFIDKeyboardWedge.reader.paramGet("/reader/version/serial").toString());
                    ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[4])).setText(RFIDKeyboardWedge.reader.paramGet("/reader/version/supportedProtocols").toString());
                    ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[5])).setText(getResources().getString(R.string.app_version));
                } catch (ReaderException e) {
                    e.printStackTrace();
                }
            }else {

                ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[0])).setText("Reader not connected");
                ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[1])).setText("Reader not connected");
                ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[2])).setText("Reader not connected");
                ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[3])).setText("Reader not connected");
                ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[4])).setText("Reader not connected");
                ((EditTextPreference) findPreference(preferenceKeys_aboutprefs[5])).setText(getResources().getString(R.string.app_version));
            }
            for (String s:preferenceKeys_aboutprefs) {
                bindPreferenceSummaryToValue(findPreference(s));
            }

        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // create ContextThemeWrapper from the original Activity Context with the custom theme

            View view = super.onCreateView(inflater, container, savedInstanceState);
            if (view != null) {
                view.setBackgroundColor(getResources().getColor(android.R.color.background_light));
            }
            return view;
        }

    }

}
