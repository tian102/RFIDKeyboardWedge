package com.homemade.tianp.rfidkeyboardwedge.tools;

import android.content.SharedPreferences;
import android.inputmethodservice.Keyboard;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.homemade.tianp.rfidkeyboardwedge.R;
import com.homemade.tianp.rfidkeyboardwedge.RFIDKeyboardWedge;
import com.thingmagic.Ata;
import com.thingmagic.MultiReadPlan;
import com.thingmagic.ReadExceptionListener;
import com.thingmagic.ReadListener;
import com.thingmagic.ReadPlan;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.ReaderUtil;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.StopOnTagCount;
import com.thingmagic.StopTriggerReadPlan;
import com.thingmagic.TMConstants;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;
import com.thingmagic.rfidreader.TagRecord;
import com.thingmagic.util.LoggerUtil;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import aeitagdecoder.AeiTag;
import aeitagdecoder.Chassis;
import aeitagdecoder.EndOfTrain;
import aeitagdecoder.GeneratorSet;
import aeitagdecoder.Intermodal;
import aeitagdecoder.Locomotive;
import aeitagdecoder.MultiModalEquipment;
import aeitagdecoder.PassiveAlarmTag;
import aeitagdecoder.Railcar;
import aeitagdecoder.RailcarCover;
import aeitagdecoder.Trailer;

import static com.homemade.tianp.rfidkeyboardwedge.RFIDKeyboardWedge.readRFIDkey;
import static com.homemade.tianp.rfidkeyboardwedge.RFIDKeyboardWedge.reader;

/**
 * Created by tianp on 18 Feb 2018.
 */


public class ServiceListenerRFID{

    /**
     * Introdcution
     *
     * <dl>
     * <dt><span class="strong">Heading 1</span></dt><dd>There is a line break.</dd>
     * <dt><span class="strong">Heading 2</span></dt><dd>There is a line break.</dd>
     * </dl>
     *
     * @param x foo
     * @return foo
     */

    private static String TAG = "ServiceListenerRFID";
    private static String readmode = "";
    private static Keyboard.Key readButton = null;
    private static ReadThread readThread;
    private static ReadTagThread readTagThread;
    private static LayoutInflater inflater;
    private static ArrayList<String> addedEPCRecords = new ArrayList();
    private static ConcurrentHashMap<String, TagRecord> epcToReadDataMap = new ConcurrentHashMap();
    private static String AtaTagInfo = "";
    private static int uniqueRecordCount = 0;
    private static int totalTagCount = 0;
    private static long readRatePerSec = 0L;
    private static long queryStartTime = 0L;
    private static long queryStopTime = 0L;
    static String strDateFormat = "M/d/yyyy h:m:s.SSS a";
    static SimpleDateFormat sdf;
    String format = "";
    static RFIDKeyboardWedge rfidKeyboardWedge;
    static SharedPreferences prefs = null;

    public View.OnClickListener clearListener = new View.OnClickListener() {
        public void onClick(View v) {
            clearTagRecords();
        }
    };

    public static void stopReadThread(){
        if (readThread != null) {
            readThread.setReading(false);
        }
    }
    public static void stopReadTagThread(){
        if (readTagThread != null) {
            readTagThread.setReading(false);
        }
    }

    public ServiceListenerRFID(RFIDKeyboardWedge mrfidKeyboardWedge, SharedPreferences preferences) {
        prefs = preferences;
        rfidKeyboardWedge = mrfidKeyboardWedge;
        this.findAllViewsById();

        startScan();
    }

    private void findAllViewsById() {
        readButton = (Keyboard.Key)rfidKeyboardWedge.readRFIDkey;
    }

    private void loadSettings() {
        try {
            //Set reader region from shared preferences
            manageRegion(reader);               //DONE

            //Register additional Protocols
            manageProtocols();                  //DONE


        }catch (Exception ex){
            System.out.println(ex.toString());
        }



        /*
        -----------------------------------------
        1.  Read and Write Settings
        -----------------------------------------
            Scanning Mode
                SyncAsync
            Region
                Select Region
            Protocols
                Select Protocols
            Enable Embedded Read Data
                TID
                Reserved
                EPC
                User
            DutyCycle
                Time_On
                Time_Off

        -----------------------------------------
        2.  Performance Tuning
        -----------------------------------------
            RF Power
                Read Power [-10dBm:30dBm]
            Gen2 Settings
                BLF
                Tari
                Tag Encoding
                Session
                Target
                Q

        -----------------------------------------
        3.  Regulatory Testing
        -----------------------------------------
            Hop Time
                Time in ms
            Hop Table
                 918250,  923250,  913250,  905250,  923750,  912750,
                 918750,  926250,  921250,  905750,  915250,  904750,  911250,
                 916750,  926750,  921750,  913750,  925250,  910750,  916250,
                 922750,  904250,  917250,  909750,  903750,  911750,  906250,
                 919750,  927250,  922250,  907250,  920750,  909250,  925750,
                 920250,  914750,  908750,  924750,  915750,  910250,  903250,
                 908250,  919250,  924250,  914250,  902750,  907750,  917750,
                 906750,  912250

        -----------------------------------------
        4.  Profile
        -----------------------------------------
            Select Profile

        -----------------------------------------
        5.  Firmware Update
        -----------------------------------------
            Upload Firmware file

        -----------------------------------------
        6.  About
        -----------------------------------------
            RFID Engine
            Firmware Version
            Hardware Version
            App Version
        */

    }


    private void manageProtocols(){
        if (prefs.getBoolean("switch_protocol", false)){

            String[] keys = FragmentMethods.getProtocolKeys(prefs.getString("edittext_protocol", ""));

            //TODO: Remove previous keys

            if (keys != null){
                for (String key:keys) {
                    if (key.length() == 32){
                        String licenseKey = key.toUpperCase();
                        Reader.LicenseOperation licenseOperation = new Reader.LicenseOperation();
                        licenseOperation.option = Reader.LicenseOption.SET_LICENSE_KEY;
                        byte[] byteLic = ReaderUtil.hexStringToByteArray(licenseKey);
                        licenseOperation.key = byteLic;
                        try
                        {
                            // Manage License key param supports both setting and erasing the license.
                            System.out.println("License operation started.\n");
                            reader.paramSet(TMConstants.TMR_PARAM_MANAGE_LICENSE_KEY, licenseOperation);
                            System.out.println("License operation succeeded.\n");
                            // Report protocols enabled by current license key
                            TagProtocol[] protocolList = ( TagProtocol[])reader.paramGet("/reader/version/supportedProtocols");
                            System.out.println("Supported Protocols:" );
                            for(TagProtocol p : protocolList)
                            {
                                System.out.println(p);
                            }

                        }
                        catch (ReaderException re)
                        {
                            System.out.println("ReaderException: " + re.getMessage());
                            Toast.makeText(rfidKeyboardWedge, re.getMessage() + " : " + key, Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception re)
                        {
                            System.out.println("Exception: " + re.getMessage());
                        }
                    }else{
                        Log.d("rfidKeyboardWedge", "Invalid Key: " + key + "\nLicense Key needs to be 32 characters");
                    }
                }
            }
        }

    }

    /*private void scannerRead(){

        int selection

        TagReadData[] tagReadData = null;
        if()
                simpleReadplanScan(reader);             //SimpleReadPlan

                multiprotocolScan(reader);              //MultiReadPlan

                stopTriggerPlanScan(reader);            //StopTriggerReadPlan

                continuousReadPlanScan(reader);



//        for (TagReadData trd:tagReadData){
//            Toast.makeText(rfidKeyboardWedge, trd.getTag().getProtocol().toString() + ": " + trd.toString(), Toast.LENGTH_SHORT).show();
//        }

    }*/

/*    private void continuousReadPlanScan(Reader reader) {

    }

    private void stopTriggerPlanScan(Reader reader) {

    }*/

    private void manageRegion(Reader mReader) throws Exception {
        if (Reader.Region.UNSPEC == (Reader.Region) mReader.paramGet("/reader/region/id"))
        {
            Reader.Region[] supportedRegions = (Reader.Region[]) mReader.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
            if (supportedRegions.length < 1)
            {
                throw new Exception("Reader doesn't support any regions");
            }
            else
            {
                mReader.paramSet("/reader/region/id", supportedRegions[0]);
            }
        }else {
            int selection = Integer.parseInt(prefs.getString("list_region", "0"));
            String region = rfidKeyboardWedge.getResources().getStringArray(R.array.pref_region)[selection];
            mReader.paramSet("/reader/region/id", Reader.Region.valueOf(region));
        }
    }

    private TagReadData[] tagDataFilter(TagReadData[] tagReadData){


        return tagReadData;
    }


    /*private TagReadData[] multiprotocolScan(Reader reader){
        TagProtocol[] protocolList = new TagProtocol[0];
        try {
            protocolList = (TagProtocol[]) reader.paramGet("/reader/version/supportedProtocols");
            ReadPlan rp[] = new ReadPlan[protocolList.length];
            for (int i = 0; i < protocolList.length; i++)
            {
                rp[i] = new SimpleReadPlan(new int[]{1, 2}, protocolList[i], null, null, 10);
            }
            MultiReadPlan testMultiReadPlan = new MultiReadPlan(rp);
            reader.paramSet("/reader/read/plan", testMultiReadPlan);
            TagReadData[] t;
            try
            {
                t = reader.read(1000);
            }
            catch (ReaderException re)
            {
                System.out.printf("Error reading tags: %s\n", re.getMessage());
                return null;
            }
            for (TagReadData trd : t)
            {
                System.out.println(trd.getTag().getProtocol().toString() + ": " + trd.toString());
            }
            return t;
        } catch (ReaderException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void simpleReadplanScan(Reader reader){
        TagProtocol[] protocolList = new TagProtocol[0];
        TagReadData[] t = null;
        String readerModel = null;
        try {
            readerModel = (String) reader.paramGet("/reader/version/model");
            if(readerModel.equalsIgnoreCase("M6e Micro")) {
                SimpleReadPlan simplePlan = new SimpleReadPlan(new int[]{1, 2}, TagProtocol.GEN2);
                reader.paramSet("/reader/read/plan", simplePlan);

                try {
                    t = reader.read(1000);
                }catch (ReaderException re) {
                    System.out.printf("Error reading tags: %s\n", re.getMessage());
                    t=null;
                }

                for (TagReadData trd : t) {
                    System.out.println(trd.getTag().getProtocol().toString() + ": " + trd.toString());
                }

            }
        } catch (ReaderException e) {
            e.printStackTrace();
            t=null;
        }

        //TODO: Filter and print to screen
        //Filter();
        //PrintToScreen();
    }*/

    public void startScan() {

        String readPlan = "simple";
        try {
            String readerModel = (String) reader.paramGet("/reader/version/model");
            readPlan = "simple";

            // Check for Sync/Async
            String readermode = prefs.getString("list_readmode", "0");
            int selection = Integer.parseInt(readermode);
            String timeOut = prefs.getString("edit_text_title", "1000");
            String operation = "";
            //readerMode="0"   |    "Sync"
            //readerMode="1"   |    "Async"
            if(selection == 0) {
                operation = "syncRead";

            } else if(selection == 1) {
                operation = "asyncRead";
            }

            if(readButton.label.equals("Stop Reading")) {
                readTagThread.setReading(false);

                readButton.label = ("Start Reading");
            }else {
                if(readButton.label.equals("Start Reading")) {
                    if(operation.equals("syncRead")){
                        loadSettings();
                        readButton.label = ("Reading");
                    }else {
                        loadSettings();
                        readButton.label = ("Stop Reading");
                    }
                }

                /*if(readerModel.equalsIgnoreCase("M6e Micro")) {
                    if(prefs.getBoolean("switch_protocol", false)){
                        if (operation.equalsIgnoreCase("AsyncRead")){
                            Toast.makeText(rfidKeyboardWedge, "MultiProtocolRead reading not currently supported with Asynchronous Reading.", Toast.LENGTH_SHORT).show();
                            SimpleReadPlan simplePlan = new SimpleReadPlan(new int[]{1, 2}, TagProtocol.GEN2);
                            reader.paramSet("/reader/read/plan", simplePlan);

//                            StopOnTagCount sotc = new StopOnTagCount();
//                            sotc.N = 2; // number of tags to read
//                            StopTriggerReadPlan strp = new StopTriggerReadPlan(sotc, new int[]{1, 2}, TagProtocol.GEN2);
//                            reader.paramSet("/reader/read/plan", strp);

                        }else {
                            readPlan = "multi";
                            TagProtocol[] protocolList = (TagProtocol[]) reader.paramGet("/reader/version/supportedProtocols");
                            if(protocolList == null){
                                Toast.makeText(rfidKeyboardWedge, "No valid additional protocols found", Toast.LENGTH_SHORT).show();
                                Toast.makeText(rfidKeyboardWedge, "Single protocol scan mode selected", Toast.LENGTH_SHORT).show();
                                SimpleReadPlan simplePlan = new SimpleReadPlan(new int[]{1, 2}, TagProtocol.GEN2);
                                reader.paramSet("/reader/read/plan", simplePlan);
                            }else{
                                ReadPlan rp[] = new ReadPlan[protocolList.length];
                                for (int i = 0; i < protocolList.length; i++)
                                {
                                    rp[i] = new SimpleReadPlan(new int[]{1, 2}, protocolList[i], null, null, 10);
                                }
                                MultiReadPlan testMultiReadPlan = new MultiReadPlan(rp);
                                reader.paramSet("/reader/read/plan", testMultiReadPlan);
                            }
                        }


                    }else{
                        readPlan = "simple";
                        SimpleReadPlan simplePlan = new SimpleReadPlan(new int[]{1, 2}, TagProtocol.GEN2);
                        reader.paramSet("/reader/read/plan", simplePlan);

//                        StopOnTagCount sotc = new StopOnTagCount();
//                        sotc.N = 2; // number of tags to read
//                        StopTriggerReadPlan strp = new StopTriggerReadPlan(sotc, new int[]{1, 2}, TagProtocol.GEN2);
//                        reader.paramSet("/reader/read/plan", strp);
                    }

                }*/

                if(readerModel.equalsIgnoreCase("M6e Micro")) {
                    if(prefs.getBoolean("switch_protocol", false)){
                        if (operation.equalsIgnoreCase("AsyncRead")){
                            readPlan = "simple";
                            Toast.makeText(rfidKeyboardWedge, "MultiProtocolRead reading not currently supported with Asynchronous Reading.", Toast.LENGTH_SHORT).show();
                            SimpleReadPlan simplePlan = new SimpleReadPlan(new int[]{1, 2}, TagProtocol.GEN2);
                            reader.paramSet("/reader/read/plan", simplePlan);

                        }else {
//                            readPlan = "multi";
//                            TagProtocol[] protocolList = (TagProtocol[]) reader.paramGet("/reader/version/supportedProtocols");
//                            if(protocolList == null){
//                                Toast.makeText(rfidKeyboardWedge, "No valid additional protocols found", Toast.LENGTH_SHORT).show();
//                                Toast.makeText(rfidKeyboardWedge, "Single protocol scan mode selected", Toast.LENGTH_SHORT).show();
//                                SimpleReadPlan simplePlan = new SimpleReadPlan(new int[]{1, 2}, TagProtocol.ATA, null, null, 1000);
//                                reader.paramSet("/reader/read/plan", simplePlan);
//                            }else{
//                                ReadPlan rp[] = new ReadPlan[protocolList.length];
//                                for (int i = 0; i < protocolList.length; i++)
//                                {
//                                    rp[i] = new SimpleReadPlan(new int[]{1, 2}, protocolList[i], null, null, 10);
//                                }
//                                MultiReadPlan testMultiReadPlan = new MultiReadPlan(rp);
//                                reader.paramSet("/reader/read/plan", testMultiReadPlan);
//                            }

                            readPlan = "multi";

                            SimpleReadPlan simplePlan = new SimpleReadPlan(new int[]{1, 2}, TagProtocol.ATA, null, null, 1000);
                            reader.paramSet("/reader/read/plan", simplePlan);

                        }


                    }else{

                        if (operation.equalsIgnoreCase("AsyncRead")){
                            SimpleReadPlan simplePlan = new SimpleReadPlan(new int[]{1, 2}, TagProtocol.GEN2);
                            reader.paramSet("/reader/read/plan", simplePlan);

//                            StopOnTagCount sotc = new StopOnTagCount();
//                            sotc.N = 2; // number of tags to read
//                            StopTriggerReadPlan strp = new StopTriggerReadPlan(sotc, new int[]{1, 2}, TagProtocol.GEN2);
//                            reader.paramSet("/reader/read/plan", strp);

                        }else {
                            readPlan = "simple";
                            StopOnTagCount sotc = new StopOnTagCount();
                            sotc.N = 2; // number of tags to read
                            StopTriggerReadPlan strp = new StopTriggerReadPlan(sotc, new int[]{1, 2}, TagProtocol.GEN2);
                            reader.paramSet("/reader/read/plan", strp);
                        }


                    }

                }


                clearTagRecords();
                readTagThread = new ReadTagThread(reader, operation, readPlan);
                readTagThread.execute(new Void[0]);
            }
        } catch (Exception var4) {
            LoggerUtil.error(TAG, "Exception", var4);
        }

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static void clearTagRecords() {
        addedEPCRecords.clear();
        epcToReadDataMap.clear();
        uniqueRecordCount = 0;
        totalTagCount = 0;
        readRatePerSec = 0L;
        queryStartTime = System.currentTimeMillis();
    }

    static {
        sdf = new SimpleDateFormat(strDateFormat);
    }

    public static class ReadThread extends AsyncTask<Void, Integer, ConcurrentHashMap<String, TagRecord>> {
        private String operation;
        private Long duration;
        private static boolean exceptionOccur = false;
        private static String exception = "";
        private static boolean reading = true;
        private static Reader mReader;
        static ReadExceptionListener exceptionListener = new ReadThread.TagReadExceptionReceiver();
        static ReadListener readListener = new ReadThread.PrintListener();

        public ReadThread(Reader reader, String operation) {
            this.operation = operation;
            mReader = reader;
        }

        protected void onPreExecute() {
            clearTagRecords();
            addedEPCRecords = new ArrayList();
            epcToReadDataMap = new ConcurrentHashMap();
            exceptionOccur = false;
        }

        protected ConcurrentHashMap<String, TagRecord> doInBackground(Void... params) {
            Long startTime = Long.valueOf(System.currentTimeMillis());
            boolean var12 = false;

            Long endTime;
            label101: {
                try {
                    var12 = true;

                    if(this.operation.equalsIgnoreCase("syncRead")) {
                        queryStartTime = System.currentTimeMillis();
                        TagReadData[] tagReads = mReader.read(1000L);
                        queryStopTime = System.currentTimeMillis();
                        TagReadData[] var4 = tagReads;
                        int var5 = tagReads.length;

                        for(int var6 = 0; var6 < var5; ++var6) {
                            TagReadData tr = var4[var6];
                            this.parseTag(tr, false);
                            //Toast.makeText(rfidKeyboardWedge, "Tag read sync", Toast.LENGTH_SHORT).show();
                        }

                        this.publishProgress(new Integer[]{Integer.valueOf(0)});
                        var12 = false;

                    } else {
                        this.setReading(true);
                        mReader.addReadExceptionListener(exceptionListener);
                        mReader.addReadListener(readListener);
                        mReader.startReading();
                        queryStartTime = System.currentTimeMillis();
                        this.refreshReadRate();

                        while(isReading()) {
                            Thread.sleep(100L);
                        }

                        queryStopTime = System.currentTimeMillis();
                        System.out.println("Stop reading @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + isReading());
                        mReader.stopReading();
                        System.out.println("Stop reading------");
                        mReader.removeReadListener(readListener);
                        mReader.removeReadExceptionListener(exceptionListener);
                        System.out.println("removed listeners------");
                        var12 = false;
                    }
                    break label101;
                } catch (Exception var13) {
                    exception = var13.getMessage();
                    exceptionOccur = true;
                    LoggerUtil.error(TAG, "Exception while reading :", var13);
                    var12 = false;
                } finally {
                    if(var12) {
                        endTime = Long.valueOf(System.currentTimeMillis());
                        this.duration = Long.valueOf((endTime.longValue() - startTime.longValue()) / 1000L);
                    }
                }

                endTime = Long.valueOf(System.currentTimeMillis());
                this.duration = Long.valueOf((endTime.longValue() - startTime.longValue()) / 1000L);
                return epcToReadDataMap;
            }

            endTime = Long.valueOf(System.currentTimeMillis());
            this.duration = Long.valueOf((endTime.longValue() - startTime.longValue()) / 1000L);
            return epcToReadDataMap;
        }

        private void refreshReadRate() {
            (new Thread(new Runnable() {
                public void run() {
                    while(ReadThread.reading) {
                        try {
                            Thread.sleep(900L);
                            ReadThread.this.publishProgress(new Integer[]{Integer.valueOf(0)});
                        } catch (InterruptedException var2) {
                            LoggerUtil.error(TAG, "Exception ", var2);
                        }
                    }

                }
            })).start();
        }

        private static void calculateReadrate() {
            long elapsedTime = System.currentTimeMillis() - queryStartTime;
            if(!isReading()) {
                elapsedTime = queryStopTime - queryStartTime;
            }

            long tagReadTime = elapsedTime / 1000L;
            if(tagReadTime == 0L) {
                readRatePerSec = (long)((double) totalTagCount / ((double)elapsedTime / 1000.0D));
            } else {
                readRatePerSec = (long) totalTagCount / tagReadTime;
            }

        }

        private void parseTag(TagReadData tr, boolean publishResult) {
            totalTagCount = totalTagCount + tr.getReadCount();
            String epcString = tr.getTag().epcString();
            TagRecord tempTR;
            if(epcToReadDataMap.keySet().contains(epcString)) {
                tempTR = (TagRecord) epcToReadDataMap.get(epcString);
                tempTR.readCount += tr.getReadCount();
            } else {
                tempTR = new TagRecord();
                tempTR.setEpcString(epcString);
                tempTR.setReadCount(tr.getReadCount());
                epcToReadDataMap.put(epcString, tempTR);
            }

            if(publishResult) {
                this.publishProgress(new Integer[]{Integer.valueOf(0)});
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            int progressToken = progress[0].intValue();
            if(progressToken == -1) {
                Toast.makeText(rfidKeyboardWedge, "ERROR :" + exception, Toast.LENGTH_SHORT).show();
            } else {
                populateSearchResult(epcToReadDataMap);
                if(!exceptionOccur && totalTagCount > 0) {
                    Toast.makeText(rfidKeyboardWedge, "Unique Tags : " + epcToReadDataMap.keySet().size(), Toast.LENGTH_LONG).show();
                    Toast.makeText(rfidKeyboardWedge, "Total Tags  : " + totalTagCount, Toast.LENGTH_LONG).show();
                }
            }

        }

        protected void onPostExecute(ConcurrentHashMap<String, TagRecord> epcToReadDataMap) {
            if(exceptionOccur) {
                Toast.makeText(rfidKeyboardWedge, "ERROR :" + exception, Toast.LENGTH_SHORT).show();
            } else {
                calculateReadrate();
                //Toast.makeText(rfidKeyboardWedge, "Total Tags  : " + totalTagCount, Toast.LENGTH_LONG).show();
                populateSearchResult(epcToReadDataMap);
            }


           if(this.operation.equalsIgnoreCase("AsyncRead")) {
                readButton.label = ("Start Reading");
            } else if(this.operation.equalsIgnoreCase("SyncRead")) {
                readButton.label = ("Read");
            }

            if(exceptionOccur) {
                readButton.label = ("RFID");
                disconnectReader();
            }

        }

        private static void disconnectReader() {
            reader = null;
        }

        private static void populateSearchResult(ConcurrentHashMap<String, TagRecord> epcToReadDataMap) {
            try {
                Set<String> epcKeySet = epcToReadDataMap.keySet();
                Iterator var2 = epcKeySet.iterator();

                while(var2.hasNext()) {
                    String epcString = (String)var2.next();
                    TagRecord tagRecordData = (TagRecord)epcToReadDataMap.get(epcString);
                    if(!addedEPCRecords.contains(epcString.toString())) {
                        addedEPCRecords.add(epcString.toString());
                        uniqueRecordCount = addedEPCRecords.size();

                        Toast.makeText(rfidKeyboardWedge, "UniqueRecordCount: " + String.valueOf(uniqueRecordCount), Toast.LENGTH_SHORT).show();
                        Toast.makeText(rfidKeyboardWedge, "EPC String: " + tagRecordData.getEpcString(), Toast.LENGTH_SHORT).show();
                        Toast.makeText(rfidKeyboardWedge, "Read count: " + String.valueOf(tagRecordData.getReadCount()), Toast.LENGTH_SHORT).show();
                        rfidKeyboardWedge.StringToWedge(tagRecordData.getEpcString());

                    }
                }
            } catch (Exception var5) {
                LoggerUtil.error(TAG, "Exception while populating tags :", var5);
            }

        }

        public static boolean isReading() {
            return reading;
        }

        public void setReading(boolean mreading) {
            reading = mreading;
        }

        static class TagReadExceptionReceiver implements ReadExceptionListener {
            TagReadExceptionReceiver() {
            }

            public void tagReadException(Reader r, ReaderException re) {
                if(!re.getMessage().equalsIgnoreCase("No connected antennas found") && !re.getMessage().contains("The module has detected high return loss") && !re.getMessage().contains("Tag ID buffer full") && !re.getMessage().contains("No tags found")) {
                    ReadThread.exception = re.getMessage();
                    ReadThread.exceptionOccur = true;
                    readThread.setReading(false);
                    readThread.publishProgress(new Integer[]{Integer.valueOf(-1)});
                }

            }
        }

        static class PrintListener implements ReadListener {
            PrintListener() {
            }

            public void tagRead(Reader r, TagReadData tr) {
                readThread.parseTag(tr, true);
            }
        }
    }

    public static class ReadTagThread extends AsyncTask<Void, Integer, ConcurrentHashMap<String, TagRecord>> {


        private static String operation;
        private static String exception = "";
        private static String mReadPlan = "";
        private static boolean exceptionOccur = false;
        private static boolean reading = true;
        private static Reader mReader;
        private Long duration;
        static ReadExceptionListener exceptionListener = new ReadTagThread.TagReadExceptionReceiver();
        static ReadListener readListener = new ReadTagThread.PrintListener();

        public ReadTagThread(Reader reader, String operation, String readPlan) {
            this.operation = operation;
            this.mReadPlan = readPlan;
            mReader = reader;
        }
        public static String getOperation() {
            return operation;
        }
        protected void onPreExecute() {
            clearTagRecords();
            addedEPCRecords = new ArrayList();
            epcToReadDataMap = new ConcurrentHashMap();
            exceptionOccur = false;
        }

        static String hexToBin(String s) {
            String tag = new BigInteger(s, 16).toString(2);
            Integer length = tag.length();
            if (length < 128) {
                for (int i = 0; i < 128 - length; i++) {
                    tag = "0" + tag;
                }
            }
            return tag;
        }

        static String leftpad(String originalString, int length,char padCharacter)
        {
            String paddedString = originalString;
            while (paddedString.length() < length) {
                paddedString = padCharacter + paddedString;
            }
            return paddedString;
        }

        protected ConcurrentHashMap<String, TagRecord> doInBackground(Void... params) {
            Long startTime = Long.valueOf(System.currentTimeMillis());
            boolean var12 = false;

            Long endTime;
            label101: {
                try {
                    var12 = true;

                    if(this.operation.equalsIgnoreCase("syncRead")) {
//TODO Change if statement
                        if(this.mReadPlan.equalsIgnoreCase("multi")){
                            // Read tags
                            queryStartTime = System.currentTimeMillis();
                            TagReadData[] tagReads = mReader.read(1000L);
                            queryStopTime = System.currentTimeMillis();
                            // Considers the first tag read to be decoded
                            String TagEPC = tagReads[0].getTag().epcString();

                            this.parseTag(tagReads[0], false);

                            this.publishProgress(new Integer[]{Integer.valueOf(0)});
                            var12 = false;

                            /*// Decode the tag
                            AeiTag tag = AeiTag.decodeTagData(TagEPC);
                            String binstrvalue = hexToBin(TagEPC);
                            if((AeiTag.DataFormat.getDescription(tag.getDataFormat()).matches("6-bit ASCII"))&&(AeiTag.IsHalfFrameTag.getValue() == false))
                            {
                               tagInfo.add("Binary Format : " + binstrvalue);
                                List<Integer> list  = AeiTag.fromString(binstrvalue);
                                System.out.print("ASCII Format: ");
                                for(int i: list)
                                {
                                    System.out.print(AeiTag.convertDecToSixBitAscii(i));
                                }
                               tagInfo.add("\n");
                            }
                            else
                            {
                                if(tag.isFieldValid().get(AeiTag.TagField.EQUIPMENT_GROUP))
                                {
                                   tagInfo.add("Equipment Group  : " + AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()));
                                    if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Railcar")))
                                    {
                                        Railcar rc = (Railcar)tag;
                                       tagInfo.add("Car Number: " + rc.getCarNumber());
                                       tagInfo.add("Side Indicator: " + AeiTag.SideIndicator.getDescription(rc.getSide()));
                                       tagInfo.add("Length(dm): " + rc.getLength());
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Number of axles: " + rc.getNumberOfAxles());
                                           tagInfo.add("Bearing Type: " + AeiTag.BearingType.getDescription(rc.getBearingType()));
                                           tagInfo.add("Platform ID: " + AeiTag.PlatformId.getDescription(rc.getPlatformId()));
                                           tagInfo.add("Spare: " + rc.getSpare());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("End-of-train device")))
                                    {
                                        EndOfTrain eot = (EndOfTrain)tag;
                                       tagInfo.add("EOT Number: " + eot.getEotNumber());
                                       tagInfo.add("EOT Type: " + EndOfTrain.EotType.getDescription(eot.getEotType()));
                                       tagInfo.add("Side Indicator: " + EndOfTrain.SideIndicator.getDescription(eot.getSide()));
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Spare            : " + eot.getSpare());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Locomotive")))
                                    {
                                        Locomotive loc = (Locomotive)tag;
                                       tagInfo.add("Car Number: " + loc.getLocomotiveNumber());
                                       tagInfo.add("Side Indicator: " + AeiTag.SideIndicator.getDescription(loc.getSide()));
                                       tagInfo.add("Length(dm): " + loc.getLength());
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Number of axles: " + loc.getNumberOfAxles());
                                           tagInfo.add("Bearing Type: " + AeiTag.BearingType.getDescription(loc.getBearingType()));
                                           tagInfo.add("Spares: " + loc.getSpare());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Intermodal container")))
                                    {
                                        Intermodal imod =  (Intermodal)tag;
                                       tagInfo.add("Car Number: " + imod.getIdNumber());
                                       tagInfo.add("Check Digit: " + imod.getCheckDigit());
                                       tagInfo.add("Length(cm): " + imod.getLength());
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Height(cm): " + imod.getHeight());
                                           tagInfo.add("Width(cm): " + imod.getWidth());
                                           tagInfo.add("Container Type: " + imod.getContainerType());
                                           tagInfo.add("Container Max Gross Weight: " + imod.getMaxGrossWeight());
                                           tagInfo.add("Container Tare Weight: " + imod.getTareWeight());
                                           tagInfo.add("Spare: " + imod.getSpare());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Trailer")))
                                    {
                                        Trailer trail = (Trailer)tag;
                                       tagInfo.add("Trailer Number: " + trail.getTrailerNumber());
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Length(cm): " + trail.getLength());
                                           tagInfo.add("Width: " + AeiTag.Width.getDescription(trail.getWidth()));
                                           tagInfo.add("Height: " + trail.getHeight());
                                           tagInfo.add("Tandem Width: " + AeiTag.Width.getDescription(trail.getTandemWidth()));
                                           tagInfo.add("Type Detail Code: " + AeiTag.TrailerTypeDetail.getDescription(trail.getTypeDetail()));
                                           tagInfo.add("Forward Extension: " + trail.getForwardExtension());
                                           tagInfo.add("Tare Weight: " + trail.getTareWeight());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Chassis")))
                                    {
                                        Chassis chas = (Chassis)tag;
                                       tagInfo.add("Chassis Number: " + chas.getChassisNumber());
                                       tagInfo.add("Type Detail Code: " + AeiTag.ChassisTypeDetail.getDescription(chas.getTypeDetail()));
                                       tagInfo.add("Tare Weight: " + chas.getTareWeight());
                                       tagInfo.add("Height: " + chas.getHeight());
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Tandem Width Code: " + AeiTag.Width.getDescription(chas.getTandemWidth()));
                                           tagInfo.add("Forward Extension: " + chas.getForwardExtension());
                                           tagInfo.add("Kingpin Setting: " + chas.getKingpinSetting());
                                           tagInfo.add("Axle Spacing: " + chas.getAxleSpacing());
                                           tagInfo.add("Running Gear Location: " + chas.getRunningGearLoc());
                                           tagInfo.add("Number of Lengths: " + chas.getNumLengths());
                                           tagInfo.add("Minimum Length: " + chas.getMinLength());
                                           tagInfo.add("Maximum Length: " + chas.getMaxLength());
                                           tagInfo.add("Spare: " + chas.getSpare());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Railcar cover")))
                                    {
                                        RailcarCover rcov = (RailcarCover)tag;
                                       tagInfo.add("Car Number: " + rcov.getEquipmentNumber());
                                       tagInfo.add("Side Indicator: " + AeiTag.SideIndicator.getDescription(rcov.getSide()));
                                       tagInfo.add("Length: " + rcov.getLength());
                                       tagInfo.add("Cover Type: " + AeiTag.CoverType.getDescription(rcov.getCoverType()));
                                       tagInfo.add("Date built or rebuilt: " + AeiTag.DateBuilt.getDescription(rcov.getDateBuilt()));
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Insulation: " + AeiTag.Insulation.getDescription(rcov.getInsulation()));
                                           tagInfo.add("Fitting code/Lifting Bracket: " + AeiTag.Fitting.getDescription(rcov.getFitting()));
                                           tagInfo.add("Associated railcar initial: " + rcov.getAssocRailcarInitial());
                                           tagInfo.add("Associated railcar number: " + rcov.getAssocRailcarNum());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Passive alarm tag")))
                                    {
                                        PassiveAlarmTag pa = (PassiveAlarmTag)tag;
                                       tagInfo.add("Car Number: " + pa.getEquipmentNumber());
                                       tagInfo.add("Side Indicator: " + AeiTag.SideIndicator.getDescription(pa.getSide()));
                                       tagInfo.add("Length(dm): " + pa.getLength());
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Number of axles: " + pa.getNumberOfAxles());
                                           tagInfo.add("Bearing Type : " + AeiTag.BearingType.getDescription(pa.getBearingType()));
                                           tagInfo.add("Platform ID: " + AeiTag.PlatformId.getDescription(pa.getPlatformId()));
                                           tagInfo.add("AlarmCode: " + AeiTag.Alarm.getDescription(pa.getSpare()));
                                           tagInfo.add("Spare: " + pa.getSpare());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Generator set")))
                                    {
                                        GeneratorSet gs = (GeneratorSet)tag;
                                       tagInfo.add("Generator set number: " + gs.getGensetNumber());
                                       tagInfo.add("Mounting code: " + AeiTag.Mounting.getDescription(gs.getMounting()));
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Tare weight: " + gs.getTareWeight());
                                           tagInfo.add("Fuel Capacity: " + AeiTag.FuelCapacity.getDescription(gs.getFuelCapacity()));
                                           tagInfo.add("Voltage: " + AeiTag.Voltage.getDescription(gs.getVoltage()));
                                           tagInfo.add("Spare: " + gs.getSpare());
                                        }
                                    }
                                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Multimodal equipment")))
                                    {
                                        MultiModalEquipment me = (MultiModalEquipment)tag;
                                       tagInfo.add("Equipment Number : " + me.getEquipmentNumber());
                                       tagInfo.add("Side Indicator   : " + AeiTag.SideIndicator.getDescription(me.getSide()));
                                       tagInfo.add("Length(dm)       : " + me.getLength());
                                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                                        {
                                           tagInfo.add("Number of axles  : " + me.getNumberOfAxles());
                                           tagInfo.add("Bearing Type     : " + AeiTag.BearingType.getDescription(me.getBearingType()));
                                           tagInfo.add("Platform ID      : " + AeiTag.PlatformId.getDescription(me.getPlatformId()));
                                           tagInfo.add("Type Detail Code : " + AeiTag.MultiModalTypeDetail.getDescription(me.getTypeDetail()));
                                           tagInfo.add("Spare            : " + me.getSpare());
                                        }
                                    }
                                }
                                else
                                {
                                   tagInfo.add("Error : Unknown/unidentified tag type, can not proceed with tag parsing and displaying the raw ASCII data.");
                                   tagInfo.add("Binary Format : " + binstrvalue);

                                }

                               tagInfo.add("EquipmentInitial : " + tag.getEquipmentInitial());

                                if(tag.isFieldValid().get(AeiTag.TagField.TAG_TYPE))
                                {
                                   tagInfo.add("Tag Type: " + AeiTag.TagType.getDescription(tag.getTagType()));
                                }
                                else
                                {
                                   tagInfo.add("Tag Type: " + leftpad(Integer.toBinaryString(tag.getTagType()), 2, '0')+ " (Not a valid TagType.)");
                                }

                                if(AeiTag.IsHalfFrameTag.getValue() == false)
                                {
                                    if(tag.isFieldValid().get(AeiTag.TagField.DATA_FORMAT))
                                    {
                                       tagInfo.add("Data Format code : " +  AeiTag.DataFormat.getDescription(tag.getDataFormat()));
                                    }
                                    else
                                    {
                                       tagInfo.add("Data Format code : " + leftpad(Integer.toBinaryString(tag.getDataFormat()), 6, '0')+ " (Not a valid Data Format.)");
                                    }
                                }
                            }

                            this.parseAtaTag(list2String(tagInfo),true);*/

                        }else{
                            queryStartTime = System.currentTimeMillis();
                            TagReadData[] tagReads = mReader.read(1000L);
                            queryStopTime = System.currentTimeMillis();
                            TagReadData[] var4 = tagReads;
                            int var5 = tagReads.length;

                            for(int var6 = 0; var6 < var5; ++var6) {
                                TagReadData tr = var4[var6];
                                this.parseTag(tr, false);
                                //Toast.makeText(rfidKeyboardWedge, "Tag read sync", Toast.LENGTH_SHORT).show();
                            }

                            this.publishProgress(new Integer[]{Integer.valueOf(0)});
                            var12 = false;
                        }
                    } else {
                        this.setReading(true);
                        mReader.addReadExceptionListener(exceptionListener);
                        mReader.addReadListener(readListener);
                        mReader.startReading();
                        queryStartTime = System.currentTimeMillis();
                        this.refreshReadRate();

                        while(isReading()) {
                            Thread.sleep(100L);
                        }

                        queryStopTime = System.currentTimeMillis();
                        System.out.println("Stop reading @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@" + isReading());
                        mReader.stopReading();
                        System.out.println("Stop reading------");
                        mReader.removeReadListener(readListener);
                        mReader.removeReadExceptionListener(exceptionListener);
                        System.out.println("removed listeners------");
                        var12 = false;
                    }
                    break label101;
                } catch (Exception var13) {
                    exception = var13.getMessage();
                    exceptionOccur = true;
                    LoggerUtil.error(TAG, "Exception while reading :", var13);
                    var12 = false;
                } finally {
                    if(var12) {

                        endTime = Long.valueOf(System.currentTimeMillis());
                        this.duration = Long.valueOf((endTime.longValue() - startTime.longValue()) / 1000L);
                    }
                }

                endTime = Long.valueOf(System.currentTimeMillis());
                this.duration = Long.valueOf((endTime.longValue() - startTime.longValue()) / 1000L);
                return epcToReadDataMap;
            }
            endTime = Long.valueOf(System.currentTimeMillis());
            this.duration = Long.valueOf((endTime.longValue() - startTime.longValue()) / 1000L);
            return epcToReadDataMap;
        }

        private String list2String(List<String> stringList){
            String s = "";
            for (String listEntry:stringList
                 ) {
                s = s + listEntry + "\n";
            }

            return s;
        }

        private void refreshReadRate() {
            (new Thread(new Runnable() {
                public void run() {
                    while(ReadTagThread.reading) {
                        try {
                            Thread.sleep(900L);
                            ReadTagThread.this.publishProgress(new Integer[]{Integer.valueOf(0)});
                        } catch (InterruptedException var2) {
                            LoggerUtil.error(TAG, "Exception ", var2);
                        }
                    }

                }
            })).start();
        }

        private static void calculateReadrate() {
            long elapsedTime = System.currentTimeMillis() - queryStartTime;
            if(!isReading()) {
                elapsedTime = queryStopTime - queryStartTime;
            }

            long tagReadTime = elapsedTime / 1000L;
            if(tagReadTime == 0L) {
                readRatePerSec = (long)((double) totalTagCount / ((double)elapsedTime / 1000.0D));
            } else {
                readRatePerSec = (long) totalTagCount / tagReadTime;
            }

        }

        private void parseTag(TagReadData tr, boolean publishResult) {
            totalTagCount = totalTagCount + tr.getReadCount();

            String readData = getReadData(tr);

            //String epcString = tr.getTag().epcString();

            TagRecord tempTR;
            if(epcToReadDataMap.keySet().contains(readData)) {
                tempTR = (TagRecord) epcToReadDataMap.get(readData);
                tempTR.readCount += tr.getReadCount();
            } else {
                tempTR = new TagRecord();
                tempTR.setEpcString(readData);
                tempTR.setReadCount(tr.getReadCount());
                epcToReadDataMap.put(readData, tempTR);
            }

            if(publishResult) {
                this.publishProgress(new Integer[]{Integer.valueOf(0)});
            }

        }
        private void parseAtaTag(String ataTagData, boolean publishResult) {
            AtaTagInfo = ataTagData;
            if(publishResult) {
                this.publishProgress(new Integer[]{Integer.valueOf(0)});
            }

        }

        protected void onProgressUpdate(Integer... progress) {
            int progressToken = progress[0].intValue();
            if(progressToken == -1) {
                Toast.makeText(rfidKeyboardWedge, "ERROR :" + exception, Toast.LENGTH_SHORT).show();
            } else {
                //TODO Fix if statement
                if(this.mReadPlan.equalsIgnoreCase("multi")){
                    populateATAResult(AtaTagInfo);
                }else {
                    populateResult(epcToReadDataMap);
                }

                if(!exceptionOccur && totalTagCount > 0) {
                    //Toast.makeText(rfidKeyboardWedge, "Unique Tags : " + epcToReadDataMap.keySet().size(), Toast.LENGTH_LONG).show();
                    //Toast.makeText(rfidKeyboardWedge, "Total Tags  : " + totalTagCount, Toast.LENGTH_LONG).show();
                }
            }

        }

        protected void onPostExecute(ConcurrentHashMap<String, TagRecord> epcToReadDataMap) {
            if(exceptionOccur) {
                Toast.makeText(rfidKeyboardWedge, "ERROR :" + exception, Toast.LENGTH_SHORT).show();
            } else {
                calculateReadrate();
                //Toast.makeText(rfidKeyboardWedge, "Total Tags  : " + totalTagCount, Toast.LENGTH_LONG).show();
               // populateResult(epcToReadDataMap);
            }


            if(this.operation.equalsIgnoreCase("AsyncRead")) {
                readButton.label = ("Start Reading");
            } else if(this.operation.equalsIgnoreCase("SyncRead")) {
                readButton.label = ("Read");
            }

            if(exceptionOccur) {
                readButton.label = ("RFID");
                disconnectReader();
            }

        }

        private static void disconnectReader() {
            reader = null;
        }

        private static void populateResult(ConcurrentHashMap<String, TagRecord> epcToReadDataMap) {
            try {
                Set<String> epcKeySet = epcToReadDataMap.keySet();
                Iterator var2 = epcKeySet.iterator();

                while(var2.hasNext()) {
                    String epcString = (String)var2.next();
                    TagRecord tagRecordData = (TagRecord)epcToReadDataMap.get(epcString);
                    if(!addedEPCRecords.contains(epcString)) {
                        addedEPCRecords.add(epcString);
                        uniqueRecordCount = addedEPCRecords.size();


                        rfidKeyboardWedge.StringToWedge(tagRecordData.getEpcString());


                    }
                }
            } catch (Exception var5) {
                LoggerUtil.error(TAG, "Exception while populating tags :", var5);
            }

        }

        private static void populateATAResult(String ATA_String) {
            try {
                rfidKeyboardWedge.StringToWedge(ATA_String);

            } catch (Exception var5) {
                LoggerUtil.error(TAG, "Exception while populating tags :", var5);
            }

        }

        public static boolean isReading() {
            return reading;
        }

        public void setReading(boolean mreading) {
            reading = mreading;
        }


        public String getReadData(TagReadData tr) {
            String readData = "";
            String readDataSelection = prefs.getString("list_read_data", "0");
            int readDataSelectionVal = Integer.parseInt(readDataSelection);

            try {
                // Check if EPC
                if (readDataSelectionVal == 0){
                    readData = tr.getTag().epcString();
                }
                // Check if UserMem
                else if (readDataSelectionVal == 1){
                    readData = tr.getUserMemData().toString();
                }
                // Check if TID
                else if (readDataSelectionVal == 2){
                    //readData = new String(tr.getTIDMemData(), StandardCharsets.UTF_8);
                    readData = tr.getTIDMemData().toString();
                }
                // Check if ReservedMem
                else if (readDataSelectionVal == 3){
                    readData = tr.getReservedMemData().toString();
                }

            }catch (Exception ex){
                Toast.makeText(rfidKeyboardWedge, ex.toString(), Toast.LENGTH_SHORT).show();
                readData = "Error";
            }

            return readData;

        }

        static class TagReadExceptionReceiver implements ReadExceptionListener {
            TagReadExceptionReceiver() {
            }

            public void tagReadException(Reader r, ReaderException re) {
                if(!re.getMessage().equalsIgnoreCase("No connected antennas found") && !re.getMessage().contains("The module has detected high return loss") && !re.getMessage().contains("Tag ID buffer full") && !re.getMessage().contains("No tags found")) {
                    ReadTagThread.exception = re.getMessage();
                    ReadTagThread.exceptionOccur = true;
                    readTagThread.setReading(false);
                    readTagThread.publishProgress(new Integer[]{Integer.valueOf(-1)});
                }

            }
        }

        static class PrintListener implements ReadListener {
            PrintListener() {
            }

            public void tagRead(Reader r, TagReadData tr) {
                readTagThread.parseTag(tr, true);
            }
        }
    }

}
