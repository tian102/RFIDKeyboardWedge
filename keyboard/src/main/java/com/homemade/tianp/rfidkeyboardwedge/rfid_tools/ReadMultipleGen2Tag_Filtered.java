/**
 * Sample program that reads tags in the background and track tags
 * that have been seen; only print the tags that have not been seen
 * before.
 */

// Import the API
package com.homemade.tianp.rfidkeyboardwedge.rfid_tools;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.homemade.tianp.rfidkeyboardwedge.CustomExpandableListAdapter;
import com.homemade.tianp.rfidkeyboardwedge.JSONtoExpandableList;
import com.homemade.tianp.rfidkeyboardwedge.LastScannedTags;
import com.homemade.tianp.rfidkeyboardwedge.R;
import com.homemade.tianp.rfidkeyboardwedge.RFIDKeyboardWedge;
import com.thingmagic.*;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ReadMultipleGen2Tag_Filtered
{
    static SerialPrinter serialPrinter;
    static StringPrinter stringPrinter;
    static TransportListener currentListener;

    TagReadData[] tagReads;
    HashMap<String,String> individualTagInformation = null;
    HashMap<String, List<String>> tagInformation = null;

    // Context
    private RFIDKeyboardWedge rfidKeyboardWedge;
    private View inputView;
    private Reader connectedReader;

    public ReadMultipleGen2Tag_Filtered(final RFIDKeyboardWedge mRfidKeyboardWedge, final View mInputView, Reader mConnectedReader, SharedPreferences prefs) {

        this.rfidKeyboardWedge = mRfidKeyboardWedge;
        this.inputView = mInputView;
        this.connectedReader = mConnectedReader;

        // Program setup
        Reader r = null;
        int nextarg = 0;
        boolean trace = false;
        int[] antennaList = null;

        this.individualTagInformation = new HashMap<String,String>();
        this.tagInformation = new HashMap<String, List<String>>();



        // Create Reader object, connecting to physical device
        try
        {
            antennaList = parseAntennaList("1,2");

            r = this.connectedReader;

            if (trace)
            {
                setTrace(r, new String[] {"on"});
            }
            r.connect();
            if (Reader.Region.UNSPEC == (Reader.Region)r.paramGet("/reader/region/id"))
            {
                Reader.Region[] supportedRegions = (Reader.Region[])r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
                if (supportedRegions.length < 1)
                {
                    throw new Exception("Reader doesn't support any regions");
                }
                else
                {
                    r.paramSet("/reader/region/id", supportedRegions[0]);
                }
            }

            /**
             * Checking the software version of Sargas.
             * Antenna detection is supported on Sargas from the software versions higher than 5.1.x.x.
             * User has to pass antenna as an argument, if the antenna detection is not supported on
             * the respective reader firmware.
             */
            String model = r.paramGet("/reader/version/model").toString();
            Boolean checkPort = (Boolean)r.paramGet(TMConstants.TMR_PARAM_ANTENNA_CHECKPORT);
            String swVersion = (String) r.paramGet(TMConstants.TMR_PARAM_VERSION_SOFTWARE);
            if ((model.equalsIgnoreCase("M6e Micro") || model.equalsIgnoreCase("M6e Nano") ||
                    (model.equalsIgnoreCase("Sargas") && (swVersion.startsWith("5.1")))) && (false == checkPort) && antennaList == null)
            {
                System.out.println("Module doesn't has antenna detection support, please provide antenna list");
                r.destroy();
            }

            SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.GEN2, null, null, 1000);
            r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);

            // Create and add tag listener
            ReadListener rl = new PrintNewListener();
            r.addReadListener(rl);

            // Search for tags in the background
            r.startReading();
            Thread.sleep(3000); // Run for a while so we see some tags repeatedly
            r.stopReading();
            HashSet<TagData> tagReadData = ((PrintNewListener) rl).seenTags;


            for (TagData t:tagReadData) {
                List<String> childInfo = new ArrayList<String>();
                childInfo.add("EPC = " + t.epcString());
                childInfo.add("Protocol = " + t.getProtocol());

                tagInformation.put(t.epcString(),childInfo);

            }
            //Display Dialog for Scanned Tags
            displayScannedTagDialog(this.rfidKeyboardWedge,this.inputView,this.tagInformation);
            r.removeReadListener(rl);
            // Shut down reader
            //r.destroy();

//            //Display Dialog for Scanned Tags
//            displayScannedTagDialog(this.rfidKeyboardWedge,this.inputView,this.tagInformation);


        }
        catch (ReaderException re)
        {
            System.out.println("ReaderException: " + re.getMessage());
        }
        catch (Exception re)
        {
            System.out.println("Exception: " + re.getMessage());
        }
    }
    public void displayScannedTagDialog(final RFIDKeyboardWedge rfidKeyboardWedge1, final View inputView1, HashMap<String, List<String>> tagInformation1) throws JSONException {
        ExpandableListView expandableListView;
        ExpandableListAdapter expandableListAdapter;
        final List<String> expandableListTitle;
        final HashMap<String, List<String>> expandableListDetail;

        if (!tagInformation1.isEmpty()){
            LastScannedTags.setmGen2Tags(tagInformation1);

            // create a Dialog component
            final Dialog dialog = new Dialog(rfidKeyboardWedge1);

            //tell the Dialog to use the dialog.xml as it's layout description
            dialog.setContentView(R.layout.rfid_scan_dialog);
            dialog.setTitle("Scan Results");

            Button dialogButton1 = (Button) dialog.findViewById(R.id.dialogButton1);
            Button dialogButton2 = (Button) dialog.findViewById(R.id.dialogButton2);

            expandableListView = (ExpandableListView) dialog.findViewById(R.id.expandableListView1);
            //TODO: Needs to be fixed
            //expandableListDetail = JSONtoExpandableList.getData(LastScannedTags.getmGen2Tags_JSON());
            expandableListDetail = tagInformation1;
            expandableListTitle = new ArrayList<String>(expandableListDetail.keySet());
            expandableListAdapter = new CustomExpandableListAdapter(rfidKeyboardWedge1, expandableListTitle, expandableListDetail);
            expandableListView.setAdapter(expandableListAdapter);
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

                @Override
                public void onGroupExpand(int groupPosition) {
//                    Toast.makeText(rfidKeyboardWedge1,
//                            expandableListTitle.get(groupPosition) + " List Expanded.",
//                            Toast.LENGTH_SHORT).show();
                }
            });

            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

                @Override
                public void onGroupCollapse(int groupPosition) {
//                    Toast.makeText(rfidKeyboardWedge1,
//                            expandableListTitle.get(groupPosition) + " List Collapsed.",
//                            Toast.LENGTH_SHORT).show();

                }
            });

            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v,
                                            int groupPosition, int childPosition, long id) {
//                    Toast.makeText(
//                            rfidKeyboardWedge1,
//                            expandableListTitle.get(groupPosition)
//                                    + " -> "
//                                    + expandableListDetail.get(
//                                    expandableListTitle.get(groupPosition)).get(
//                                    childPosition), Toast.LENGTH_SHORT
//                    ).show();
                    String string = expandableListDetail.get(
                            expandableListTitle.get(groupPosition)).get(
                            childPosition);
                    string = string.substring(string.indexOf("=") + 2);
                    rfidKeyboardWedge.StringToWedge(string);
                    Toast.makeText(rfidKeyboardWedge1, "[" +string + "]\nadded", Toast.LENGTH_SHORT).show();

                    return false;
                }
            });

            dialogButton1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try{
                        //Todo: Action for Accept button
                        try {
                            dialog.dismiss();
                            this.finalize();
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }


                    }catch (Exception ex){
                        System.out.println(ex.getMessage());

                    }


                }
            });

            dialogButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Todo: Action for Accept button


                    dialog.dismiss();
                    try {
                        this.finalize();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            });

            Window window = dialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.token = inputView1.getWindowToken();
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
            window.setAttributes(lp);
            window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            dialog.show();
        }
    }

    public static void setTrace(Reader r, String args[])
    {
        if (args[0].toLowerCase().equals("on"))
        {
            r.addTransportListener(Reader.simpleTransportListener);
            currentListener = Reader.simpleTransportListener;
        }
        else if (currentListener != null)
        {
            r.removeTransportListener(Reader.simpleTransportListener);
        }
    }

    static class SerialPrinter implements TransportListener {

        public void message(boolean tx, byte[] data, int timeout) {

          System.out.print(tx ? "Sending: " : "Received:");
          for (int i = 0; i < data.length; i++)
          {
            if (i > 0 && (i & 15) == 0)
              System.out.printf("\n         ");
            System.out.printf(" %02x", data[i]);
          }
          System.out.printf("\n");
        }
    }

    static class StringPrinter implements TransportListener {

        public void message(boolean tx, byte[] data, int timeout)
        {
          System.out.println((tx ? "Sending:\n" : "Receiving:\n") +
                             new String(data));
        }
    }


    static class PrintNewListener implements ReadListener {
        HashSet<TagData> seenTags = new HashSet<TagData>();


        public void tagRead(Reader r, TagReadData tr)
        {
          TagData t = tr.getTag();
          if (!seenTags.contains(t))
          {
              System.out.println("New tag: " + t.toString());

              seenTags.add(t);
          }

        }

    }

    static  int[] parseAntennaList(String arguments) {
        int[] antennaList = null;
        try
        {
            String[] antennas = arguments.split(",");
            int i = 0;
            antennaList = new int[antennas.length];
            for (String ant : antennas)
            {
                antennaList[i] = Integer.parseInt(ant);
                i++;
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        return antennaList;
    }
}
