/**
 * Sample program that reads tags for a fixed period of time (500ms)
 * and prints the tags found.
 */

// Import the API
package com.homemade.tianp.rfidkeyboardwedge.rfid_tools;

import android.app.Dialog;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import aeitagdecoder.AeiTag;

import static com.thingmagic.TagProtocol.GEN2;

public class ReadSingleGen2Tag
{
    private TagReadData[] tagReads;
    private HashMap<String, String> tagInformation = null;

    // Context
    private RFIDKeyboardWedge rfidKeyboardWedge;
    private View inputView;
    private Reader connectedReader;

    public ReadSingleGen2Tag(RFIDKeyboardWedge mRfidKeyboardWedge, View mInputView, Reader mConnectedReader, SharedPreferences prefs)
    {
        this.rfidKeyboardWedge = mRfidKeyboardWedge;
        this.inputView = mInputView;
        this.connectedReader = mConnectedReader;

        {
            // Program setup
            Reader r = null;
            int nextarg = 0;
            boolean trace = false;
            int[] antennaList = null;
            boolean printTagMetaData = true;


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
                if (Reader.Region.UNSPEC == (Reader.Region) r.paramGet("/reader/region/id"))
                {
                    Reader.Region[] supportedRegions = (Reader.Region[]) r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
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
//                    r.destroy();
                }

                SimpleReadPlan plan = new SimpleReadPlan(antennaList, GEN2, null, null, 1000);
                r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);

                if(r.getClass().getName()== ("com.thingmagic.SerialReader"))
                {
                //        Set<TagReadData.TagMetadataFlag> setMetaDataFlags = EnumSet.of(TagReadData.TagMetadataFlag.ANTENNAID , TagReadData.TagMetadataFlag.FREQUENCY);
                    Set<TagReadData.TagMetadataFlag> setMetaDataFlags = EnumSet.of( TagReadData.TagMetadataFlag.ALL);
                    r.paramSet(TMConstants.TMR_PARAM_READER_METADATA,setMetaDataFlags);
                }
                else
                {
                    // Configurable metadata param is not supported for llrp readers.
                    printTagMetaData = false;
                }
                // Read tags
                this.tagReads = r.read(500);
                this.tagInformation = new HashMap<String, String>();
                // Print tag reads
                for (TagReadData tr : this.tagReads)
                {
                    System.out.println("EPC: " + tr.epcString());
                    this.tagInformation.put("EPC", tr.epcString());

                    // Enable printTagMetaData to print meta data
                    if (printTagMetaData)
                    {
                        for (TagReadData.TagMetadataFlag metaData : TagReadData.TagMetadataFlag.values())
                        {
                            if (tr.metadataFlags.contains(metaData))
                            {
                                switch(metaData)
                                {
                                    case ANTENNAID:
                                        System.out.println("Antenna ID : " + tr.getAntenna());
                                        this.tagInformation.put("Antenna ID", String.valueOf(tr.getAntenna()));
                                        break;
                                    case DATA:
                                        // User should initialize Read Data

                                        StringBuilder stringBuilder = new StringBuilder();
                                        System.out.print("Data : ");

                                        for (byte b : tr.getData()) {
                                            System.out.printf("%02x ", b);
                                            stringBuilder.append(b);
                                        }
                                        this.tagInformation.put("Data", stringBuilder.toString());
                                        System.out.printf("\n");
                                        break;
                                    case FREQUENCY:
                                        System.out.println("Frequency : " + tr.getFrequency());
                                        this.tagInformation.put("Frequency", String.valueOf(tr.getFrequency()));
                                        break;
                                    case GPIO_STATUS:
                                        Reader.GpioPin[] state = tr.getGpio();
                                        for (Reader.GpioPin gp : state) {
                                            System.out.printf("GPIO Pin %d: %s\n", gp.id, gp.high ? "High" : "Low");
                                        }
                                        break;
                                    case PHASE:
                                        System.out.println("Phase : " + tr.getPhase());
                                        this.tagInformation.put("Phase", String.valueOf(tr.getPhase()));
                                        break;
                                    case PROTOCOL:
                                        System.out.println("Protocol : " + tr.getTag().getProtocol());
                                        this.tagInformation.put("Protocol", String.valueOf(tr.getTag().getProtocol()));
                                        break;
                                    case READCOUNT:
                                        System.out.println("ReadCount : " + tr.getReadCount());
                                        this.tagInformation.put("ReadCount", String.valueOf(tr.getReadCount()));
                                        break;
                                    case RSSI:
                                        System.out.println("RSSI : " + tr.getRssi());
                                        this.tagInformation.put("RSSI", String.valueOf(tr.getRssi()));
                                        break;
                                    case TIMESTAMP:
                                        System.out.println("Timestamp : " + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date(tr.getTime())));
                                        this.tagInformation.put("Timestamp",new SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ss.SSSZ").format(new Date(tr.getTime())));
                                        break;
                                    default:
                                        break;
                                }
                                if (GEN2 == tr.getTag().getProtocol())
                                {
                                    Gen2.TagReadData gen2 = (Gen2.TagReadData) (tr.prd);
                                    switch (metaData)
                                    {
                                        case GEN2_Q:
                                            System.out.println("Gen2Q : " + gen2.getGen2Q());
                                            this.tagInformation.put("Gen2Q", String.valueOf(gen2.getGen2Q()));
                                            break;
                                        case GEN2_LF:
                                            System.out.println("Gen2LinkFrequency : " + gen2.getGen2LF());
                                            this.tagInformation.put("Gen2LinkFrequency", String.valueOf(gen2.getGen2LF()));
                                            break;
                                        case GEN2_TARGET:
                                            System.out.println("Gen2Target : " + gen2.getGen2Target());
                                            this.tagInformation.put("Gen2Target", String.valueOf(gen2.getGen2Target()));
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }

//                // Shut down reader
//                r.destroy();

                //Display Dialog for Scanned Tags
                displayScannedTagDialog(this.rfidKeyboardWedge,this.inputView,this.tagInformation);

            }
            catch (ReaderException re)
            {
                System.out.println("Reader Exception : " + re.getMessage());
            }
            catch (Exception re)
            {
                System.out.println("Exception : " + re.getMessage());
            }
        }
    }

    public void displayScannedTagDialog(final RFIDKeyboardWedge rfidKeyboardWedge1, final View inputView1, HashMap<String, String> tagInformation1) throws JSONException {
        ExpandableListView expandableListView;
        ExpandableListAdapter expandableListAdapter;
        final List<String> expandableListTitle;
        final HashMap<String, List<String>> expandableListDetail;

        if (!tagInformation1.isEmpty()){
            LastScannedTags.setmGen2Tag(tagInformation1);

            // create a Dialog component
            final Dialog dialog = new Dialog(rfidKeyboardWedge1);

            //tell the Dialog to use the dialog.xml as it's layout description
            dialog.setContentView(R.layout.rfid_scan_dialog);
            dialog.setTitle("Scan Results");

            Button dialogButton1 = (Button) dialog.findViewById(R.id.dialogButton1);
            Button dialogButton2 = (Button) dialog.findViewById(R.id.dialogButton2);

            expandableListView = (ExpandableListView) dialog.findViewById(R.id.expandableListView1);
            expandableListDetail = JSONtoExpandableList.getData(LastScannedTags.getmGen2Tag_JSON());
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
      r.addTransportListener(r.simpleTransportListener);
    }    
  }

  public static String readTag(Reader connectedReader){
      // Program setup
      Reader r = null;
      int nextarg = 0;
      boolean trace = false;
      int[] antennaList = null;
      boolean printTagMetaData = false;


      // Create Reader object, connecting to physical device
      try
      {

          TagReadData[] tagReads;
          antennaList = parseAntennaList("1,2");


          r = connectedReader;
          if (trace)
          {
              setTrace(r, new String[] {"on"});
          }
          r.connect();
          if (Reader.Region.UNSPEC == (Reader.Region) r.paramGet("/reader/region/id"))
          {
              Reader.Region[] supportedRegions = (Reader.Region[]) r.paramGet(TMConstants.TMR_PARAM_REGION_SUPPORTEDREGIONS);
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

          SimpleReadPlan plan = new SimpleReadPlan(antennaList, GEN2, null, null, 1000);
          r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);

          if(r.getClass().getName()== ("com.thingmagic.SerialReader"))
          {
              //        Set<TagReadData.TagMetadataFlag> setMetaDataFlags = EnumSet.of(TagReadData.TagMetadataFlag.ANTENNAID , TagReadData.TagMetadataFlag.FREQUENCY);
              Set<TagReadData.TagMetadataFlag> setMetaDataFlags = EnumSet.of( TagReadData.TagMetadataFlag.ALL);
              r.paramSet(TMConstants.TMR_PARAM_READER_METADATA,setMetaDataFlags);
          }
          else
          {
              // Configurable metadata param is not supported for llrp readers.
              printTagMetaData = false;
          }
          // Read tags
          tagReads = r.read(500);
          // Print tag reads
          for (TagReadData tr : tagReads)
          {


              System.out.println("EPC: " + tr.epcString());

              // Enable printTagMetaData to print meta data
              if (printTagMetaData)
              {
                  for (TagReadData.TagMetadataFlag metaData : TagReadData.TagMetadataFlag.values())
                  {
                      if (tr.metadataFlags.contains(metaData))
                      {
                          switch(metaData)
                          {
                              case ANTENNAID:
                                  System.out.println("Antenna ID : " + tr.getAntenna());
                                  break;
                              case DATA:
                                  // User should initialize Read Data
                                  System.out.print("Data : ");
                                  for (byte b : tr.getData()) {
                                      System.out.printf("%02x ", b);
                                  }
                                  System.out.printf("\n");
                                  break;
                              case FREQUENCY:
                                  System.out.println("Frequency : " + tr.getFrequency());
                                  break;
                              case GPIO_STATUS:
                                  Reader.GpioPin[] state = tr.getGpio();
                                  for (Reader.GpioPin gp : state) {
                                      System.out.printf("GPIO Pin %d: %s\n", gp.id, gp.high ? "High" : "Low");
                                  }
                                  break;
                              case PHASE:
                                  System.out.println("Phase : " + tr.getPhase());
                                  break;
                              case PROTOCOL:
                                  System.out.println("Protocol : " + tr.getTag().getProtocol());
                                  break;
                              case READCOUNT:
                                  System.out.println("ReadCount : " + tr.getReadCount());
                                  break;
                              case RSSI:
                                  System.out.println("RSSI : " + tr.getRssi());
                                  break;
                              case TIMESTAMP:
                                  System.out.println("Timestamp : " + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date(tr.getTime())));
                                  break;
                              default:
                                  break;
                          }
                          if (GEN2 == tr.getTag().getProtocol())
                          {
                              Gen2.TagReadData gen2 = (Gen2.TagReadData) (tr.prd);
                              switch (metaData)
                              {
                                  case GEN2_Q:
                                      System.out.println("Gen2Q : " + gen2.getGen2Q());
                                      break;
                                  case GEN2_LF:
                                      System.out.println("Gen2LinkFrequency : " + gen2.getGen2LF());
                                      break;
                                  case GEN2_TARGET:
                                      System.out.println("Gen2Target : " + gen2.getGen2Target());
                                      break;
                              }
                          }
                      }
                  }
              }
          }

          // Shut down reader

          r.destroy();
          return "EPC: " + tagReads[0].epcString();
      }
      catch (ReaderException re)
      {
          System.out.println("Reader Exception : " + re.getMessage());
          return "error";
      }
      catch (Exception re)
      {
          System.out.println("Exception : " + re.getMessage());
          return "error";
      }
  }
  
  static  int[] parseAntennaList(String arguments)
    {
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
        }catch (Exception ex){
            System.out.println(ex);
        }

        return antennaList;
    }
}
