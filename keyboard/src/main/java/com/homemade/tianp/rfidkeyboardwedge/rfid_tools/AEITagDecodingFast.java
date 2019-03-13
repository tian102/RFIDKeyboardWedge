/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Sample program that reads AEI ATA tags for a fixed period of time (500ms)
 * and prints the tag details.
 */

// Import the API
package com.homemade.tianp.rfidkeyboardwedge.rfid_tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.homemade.tianp.rfidkeyboardwedge.CustomExpandableListAdapter;
import com.homemade.tianp.rfidkeyboardwedge.JSONtoExpandableList;
import com.homemade.tianp.rfidkeyboardwedge.LastScannedTags;
import com.homemade.tianp.rfidkeyboardwedge.R;
import com.homemade.tianp.rfidkeyboardwedge.RFIDKeyboardWedge;
import com.thingmagic.Reader;
import com.thingmagic.ReaderException;
import com.thingmagic.SimpleReadPlan;
import com.thingmagic.TMConstants;
import com.thingmagic.TagProtocol;
import com.thingmagic.TagReadData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class AEITagDecodingFast
{

    // Program setup
    Reader r = null;
    int nextarg = 0;
    boolean trace = false;
    int[] antennaList = null;
    TagReadData[] tagReads;
    HashMap<String, String> tagInformation = null;

    // Context
    private RFIDKeyboardWedge rfidKeyboardWedge;
    private View inputView;
    private Reader connectedReader;


    public static void setTrace(Reader r, String args[])
    {
        if (args[0].toLowerCase().equals("on"))
        {
          r.addTransportListener(r.simpleTransportListener);
        }
    }



    public AEITagDecodingFast(final RFIDKeyboardWedge mRfidKeyboardWedge, final View mInputView, Reader mConnectedReader, SharedPreferences prefs)
    {
        this.rfidKeyboardWedge = mRfidKeyboardWedge;
        this.inputView = mInputView;
        this.connectedReader = mConnectedReader;


        // Create Reader object, connecting to physical device
        try
        {


            this.antennaList = parseAntennaList("1,2");


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
                (model.equalsIgnoreCase("Sargas") && (swVersion.startsWith("5.1")))) && (false == checkPort) && this.antennaList == null)
            {
                System.out.println("Module doesn't has antenna detection support, please provide antenna list");
                r.destroy();
            }

            SimpleReadPlan plan = new SimpleReadPlan(this.antennaList, TagProtocol.ATA, null, null, 1000);
            r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);

            // Read tags
            this.tagReads = r.read(500);
            this.tagInformation = new HashMap<String, String>();

            // Considers the first tag read to be decoded
            String TagEPC = this.tagReads[0].getTag().epcString();

            // AEI ATA Tag details
            System.out.println("********************* AEI ATA Tag Details ****************************");
            System.out.println("EPC Tag data : " +    TagEPC);
            this.tagInformation.put("EPC",                 TagEPC);

            // Decode the tag
            AeiTag tag = AeiTag.decodeTagData(TagEPC);
            String binstrvalue = AEITagDecodingFast.hexToBin(TagEPC);

            if((AeiTag.DataFormat.getDescription(tag.getDataFormat()).matches("6-bit ASCII"))&&(AeiTag.IsHalfFrameTag.getValue() == false))
            {
                System.out.println("Binary Format : " +   binstrvalue);
                this.tagInformation.put("Binary Format",       binstrvalue);

                List<Integer> list  = AeiTag.fromString(binstrvalue);
                System.out.print("ASCII Format: ");
                for(int i: list)
                {
                    System.out.print(AeiTag.convertDecToSixBitAscii(i));
                }
                System.out.println("\n");
            }
            else
            {
                if(tag.isFieldValid().get(AeiTag.TagField.EQUIPMENT_GROUP))
                {
                    System.out.println("Equipment Group  : " + AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()));
                    this.tagInformation.put("Equipment Group",     AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()));

                    if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Railcar")))
                    {
                        Railcar rc = (Railcar)tag;
                        System.out.println("Equipment Group  : " + "Railcar");
                        System.out.println("Car Number       : " + rc.getCarNumber());
                        System.out.println("Side Indicator   : " + AeiTag.SideIndicator.getDescription(rc.getSide()));
                        System.out.println("Length(dm)       : " + rc.getLength());

                        this.tagInformation.put("Equipment Group",     "Railcar");
                        this.tagInformation.put("Car Number",          String.valueOf(rc.getCarNumber()));
                        this.tagInformation.put("Side Indicator",      AeiTag.SideIndicator.getDescription(rc.getSide()));
                        this.tagInformation.put("Length(dm)",          String.valueOf(rc.getLength()));

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Number of axles  : " + rc.getNumberOfAxles());
                            System.out.println("Bearing Type     : " + AeiTag.BearingType.getDescription(rc.getBearingType()));
                            System.out.println("Platform ID      : " + AeiTag.PlatformId.getDescription(rc.getPlatformId()));
                            System.out.println("Spare            : " + rc.getSpare());

                            this.tagInformation.put("Number of axles",     String.valueOf(rc.getNumberOfAxles()));
                            this.tagInformation.put("Bearing Type",        AeiTag.BearingType.getDescription(rc.getBearingType()));
                            this.tagInformation.put("Platform ID",         AeiTag.PlatformId.getDescription(rc.getPlatformId()));
                            this.tagInformation.put("Spare",               String.valueOf(rc.getSpare()));

                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("End-of-train device")))
                    {
                        EndOfTrain eot = (EndOfTrain)tag;
                        System.out.println("EOT Number       : " + eot.getEotNumber());
                        System.out.println("EOT Type         : " + EndOfTrain.EotType.getDescription(eot.getEotType()));
                        System.out.println("Side Indicator   : " + EndOfTrain.SideIndicator.getDescription(eot.getSide()));

                        this.tagInformation.put("Equipment Group",     "End-of-train device");
                        this.tagInformation.put("EOT Number",          String.valueOf(eot.getEotNumber()));
                        this.tagInformation.put("EOT Type",            EndOfTrain.EotType.getDescription(eot.getEotType()));
                        this.tagInformation.put("Side Indicator",      EndOfTrain.SideIndicator.getDescription(eot.getSide()));

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Spare            : " + eot.getSpare());

                            this.tagInformation.put("Spare",           String.valueOf(eot.getSpare()));
                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Locomotive")))
                    {
                        Locomotive loc = (Locomotive)tag;
                        System.out.println("Car Number       : " + loc.getLocomotiveNumber());
                        System.out.println("Side Indicator   : " + AeiTag.SideIndicator.getDescription(loc.getSide()));
                        System.out.println("Length(dm)       : " + loc.getLength());

                        this.tagInformation.put("Equipment Group",     "Locomotive");
                        this.tagInformation.put("Car Number",          String.valueOf(loc.getLocomotiveNumber()));
                        this.tagInformation.put("Side Indicator",      AeiTag.SideIndicator.getDescription(loc.getSide()));
                        this.tagInformation.put("Length(dm)",          String.valueOf(loc.getLength()));

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Number of axles  : " + loc.getNumberOfAxles());
                            System.out.println("Bearing Type     : " + AeiTag.BearingType.getDescription(loc.getBearingType()));
                            System.out.println("Spare            : " + loc.getSpare());

                            this.tagInformation.put("Number of axles", String.valueOf(loc.getNumberOfAxles()));
                            this.tagInformation.put("Bearing Type",    AeiTag.BearingType.getDescription(loc.getBearingType()));
                            this.tagInformation.put("Spare",           String.valueOf(loc.getSpare()));
                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Intermodal container")))
                    {
                        Intermodal imod =  (Intermodal)tag;
                        System.out.println("Car Number                : " + imod.getIdNumber());
                        System.out.println("Check Digit               : " + imod.getCheckDigit());
                        System.out.println("Length(cm)                : " + imod.getLength());

                        this.tagInformation.put("Equipment Group",         "Intermodal container");
                        this.tagInformation.put("Car Number",              String.valueOf(imod.getIdNumber()));
                        this.tagInformation.put("Check Digit",             String.valueOf(imod.getCheckDigit()));
                        this.tagInformation.put("Length(cm)",              String.valueOf(imod.getLength()));

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Height(cm)                : " + imod.getHeight());
                            System.out.println("Width(cm)                 : " + imod.getWidth());
                            System.out.println("Container Type            : " + imod.getContainerType());
                            System.out.println("Container Max Gross Weight: " + imod.getMaxGrossWeight());
                            System.out.println("Container Tare Weight     : " + imod.getTareWeight());
                            System.out.println("Spare                     : " + imod.getSpare());

                            this.tagInformation.put("Height(cm)",                  String.valueOf(imod.getHeight()));
                            this.tagInformation.put("Width(cm)",                   String.valueOf(imod.getWidth()));
                            this.tagInformation.put("Container Type",              String.valueOf(imod.getContainerType()));
                            this.tagInformation.put("Container Max Gross Weight",  String.valueOf(imod.getMaxGrossWeight()));
                            this.tagInformation.put("Container Tare Weight",       String.valueOf(imod.getTareWeight()));
                            this.tagInformation.put("Spare",                       String.valueOf(imod.getSpare()));
                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Trailer")))
                    {
                        Trailer trail = (Trailer)tag;
                        System.out.println("Trailer Number            : " + trail.getTrailerNumber());

                        this.tagInformation.put("Equipment Group",         "Trailer");
                        this.tagInformation.put("Trailer Number",          trail.getTrailerNumber());

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Length(cm)                : " + trail.getLength());
                            System.out.println("Width                     : " + AeiTag.Width.getDescription(trail.getWidth()));
                            System.out.println("Height                    : " + trail.getHeight());
                            System.out.println("Tandem Width              : " + AeiTag.Width.getDescription(trail.getTandemWidth()));
                            System.out.println("Type Detail Code          : " + AeiTag.TrailerTypeDetail.getDescription(trail.getTypeDetail()));
                            System.out.println("Forward Extension         : " + trail.getForwardExtension());
                            System.out.println("Tare Weight               : " + trail.getTareWeight());

                            this.tagInformation.put("Length(cm)",              String.valueOf(trail.getLength()));
                            this.tagInformation.put("Width",                   AeiTag.Width.getDescription(trail.getWidth()));
                            this.tagInformation.put("Height",                  String.valueOf(trail.getHeight()));
                            this.tagInformation.put("Tandem Width",            AeiTag.Width.getDescription(trail.getTandemWidth()));
                            this.tagInformation.put("Type Detail Code",        AeiTag.TrailerTypeDetail.getDescription(trail.getTypeDetail()));
                            this.tagInformation.put("Forward Extension",       String.valueOf(trail.getForwardExtension()));
                            this.tagInformation.put("Tare Weight",             String.valueOf(trail.getTareWeight()));
                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Chassis")))
                    {
                        Chassis chas = (Chassis)tag;
                        System.out.println("Chassis Number            : " + chas.getChassisNumber());
                        System.out.println("Type Detail Code          : " + AeiTag.ChassisTypeDetail.getDescription(chas.getTypeDetail()));
                        System.out.println("Tare Weight               : " + chas.getTareWeight());
                        System.out.println("Height                    : " + chas.getHeight());

                        this.tagInformation.put("Equipment Group",         "Chassis");
                        this.tagInformation.put("Chassis Number",          AeiTag.ChassisTypeDetail.getDescription(chas.getTypeDetail()));
                        this.tagInformation.put("Type Detail Code",        String.valueOf(chas.getTareWeight()));
                        this.tagInformation.put("Height",                  String.valueOf(chas.getHeight()));

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Tandem Width Code         : " + AeiTag.Width.getDescription(chas.getTandemWidth()));
                            System.out.println("Forward Extension         : " + chas.getForwardExtension());
                            System.out.println("Kingpin Setting           : " + chas.getKingpinSetting());
                            System.out.println("Axle Spacing              : " + chas.getAxleSpacing());
                            System.out.println("Running Gear Location     : " + chas.getRunningGearLoc());
                            System.out.println("Number of Lengths         : " + chas.getNumLengths());
                            System.out.println("Minimum Length            : " + chas.getMinLength());
                            System.out.println("Maximum Length            : " + chas.getMaxLength());
                            System.out.println("Spare                     : " + chas.getSpare());

                            this.tagInformation.put("Tandem Width Code",       AeiTag.Width.getDescription(chas.getTandemWidth()));
                            this.tagInformation.put("Forward Extension",       String.valueOf(chas.getForwardExtension()));
                            this.tagInformation.put("Kingpin Setting",         String.valueOf(chas.getKingpinSetting()));
                            this.tagInformation.put("Axle Spacing",            String.valueOf(chas.getAxleSpacing()));
                            this.tagInformation.put("Running Gear Location",   String.valueOf(chas.getRunningGearLoc()));
                            this.tagInformation.put("Number of Lengths",       String.valueOf(chas.getNumLengths()));
                            this.tagInformation.put("Minimum Length",          String.valueOf(chas.getMinLength()));
                            this.tagInformation.put("Spare",                   String.valueOf(chas.getMinLength()));
                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Railcar cover")))
                    {
                        RailcarCover rcov = (RailcarCover)tag;
                        System.out.println("Car Number                   : " + rcov.getEquipmentNumber());
                        System.out.println("Side Indicator               : " + AeiTag.SideIndicator.getDescription(rcov.getSide()));
                        System.out.println("Length                       : " + rcov.getLength());
                        System.out.println("Cover Type                   : " + AeiTag.CoverType.getDescription(rcov.getCoverType()));
                        System.out.println("Date built or rebuilt        : " + AeiTag.DateBuilt.getDescription(rcov.getDateBuilt()));

                        this.tagInformation.put("Equipment Group",             "Railcar cover");
                        this.tagInformation.put("Car Number",                  String.valueOf(rcov.getEquipmentNumber()));
                        this.tagInformation.put("Side Indicator",              AeiTag.SideIndicator.getDescription(rcov.getSide()));
                        this.tagInformation.put("Length",                      String.valueOf(rcov.getLength()));
                        this.tagInformation.put("Cover Type",                  String.valueOf(rcov.getLength()));
                        this.tagInformation.put("Date built or rebuilt",       AeiTag.CoverType.getDescription(rcov.getCoverType()));


                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Insulation                   : " + AeiTag.Insulation.getDescription(rcov.getInsulation()));
                            System.out.println("Fitting code/Lifting Bracket : " + AeiTag.Fitting.getDescription(rcov.getFitting()));
                            System.out.println("Associated railcar initial   : " + rcov.getAssocRailcarInitial());
                            System.out.println("Associated railcar number    : " + rcov.getAssocRailcarNum());

                            this.tagInformation.put("Insulation", AeiTag.Insulation.getDescription(rcov.getInsulation()));
                            this.tagInformation.put("Fitting code/Lifting Bracket", AeiTag.Fitting.getDescription(rcov.getFitting()));
                            this.tagInformation.put("Associated railcar initial", rcov.getAssocRailcarInitial());
                            this.tagInformation.put("Associated railcar number", String.valueOf(rcov.getAssocRailcarNum()));
                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Passive alarm tag")))
                    {
                        PassiveAlarmTag pa = (PassiveAlarmTag)tag;
                        System.out.println("Car Number       : " + pa.getEquipmentNumber());
                        System.out.println("Side Indicator   : " + AeiTag.SideIndicator.getDescription(pa.getSide()));
                        System.out.println("Length(dm)       : " + pa.getLength());

                        this.tagInformation.put("Equipment Group",     "Passive alarm tag");
                        this.tagInformation.put("Car Number    ",      String.valueOf(pa.getEquipmentNumber()));
                        this.tagInformation.put("Side Indicator",      AeiTag.SideIndicator.getDescription(pa.getSide()));
                        this.tagInformation.put("Length(dm)    ",      String.valueOf(pa.getLength()));

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Number of axles  : " + pa.getNumberOfAxles());
                            System.out.println("Bearing Type     : " + AeiTag.BearingType.getDescription(pa.getBearingType()));
                            System.out.println("Platform ID      : " + AeiTag.PlatformId.getDescription(pa.getPlatformId()));
                            System.out.println("AlarmCode        : " + AeiTag.Alarm.getDescription(pa.getSpare()));
                            System.out.println("Spare            : " + pa.getSpare());

                            this.tagInformation.put("Number of axles",     String.valueOf(pa.getNumberOfAxles()));
                            this.tagInformation.put("Bearing Type",        AeiTag.BearingType.getDescription(pa.getBearingType()));
                            this.tagInformation.put("Platform ID",         AeiTag.PlatformId.getDescription(pa.getPlatformId()));
                            this.tagInformation.put("AlarmCode",           AeiTag.Alarm.getDescription(pa.getSpare()));
                            this.tagInformation.put("Spare",               String.valueOf(pa.getSpare()));
                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Generator set")))
                    {
                        GeneratorSet gs = (GeneratorSet)tag;
                        System.out.println("Generator set number : " + gs.getGensetNumber());
                        System.out.println("Mounting code        : " + AeiTag.Mounting.getDescription(gs.getMounting()));

                        this.tagInformation.put("Equipment Group",         "Generator set");
                        this.tagInformation.put("Generator set number",    gs.getGensetNumber());
                        this.tagInformation.put("Mounting code",           AeiTag.Mounting.getDescription(gs.getMounting()));

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Tare weight          : " + gs.getTareWeight());
                            System.out.println("Fuel Capacity        : " + AeiTag.FuelCapacity.getDescription(gs.getFuelCapacity()));
                            System.out.println("Voltage              : " + AeiTag.Voltage.getDescription(gs.getVoltage()));
                            System.out.println("Spare                : " + gs.getSpare());

                            this.tagInformation.put("Tare weight",         String.valueOf(gs.getTareWeight()));
                            this.tagInformation.put("Fuel Capacity",       AeiTag.FuelCapacity.getDescription(gs.getFuelCapacity()));
                            this.tagInformation.put("Voltage",             AeiTag.Voltage.getDescription(gs.getVoltage()));
                            this.tagInformation.put("Spare",               String.valueOf(gs.getSpare()));
                        }
                    }
                    else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Multimodal equipment")))
                    {
                        MultiModalEquipment me = (MultiModalEquipment)tag;
                        System.out.println("Equipment Number : " + me.getEquipmentNumber());
                        System.out.println("Side Indicator   : " + AeiTag.SideIndicator.getDescription(me.getSide()));
                        System.out.println("Length(dm)       : " + me.getLength());

                        this.tagInformation.put("Equipment Group",         "Multimodal equipment");
                        this.tagInformation.put("Equipment Number",        String.valueOf(me.getEquipmentNumber()));
                        this.tagInformation.put("Side Indicator  ",        AeiTag.SideIndicator.getDescription(me.getSide()));
                        this.tagInformation.put("Length(dm)      ",        String.valueOf(me.getLength()));

                        if(AeiTag.IsHalfFrameTag.getValue() == false)
                        {
                            System.out.println("Number of axles  : " + me.getNumberOfAxles());
                            System.out.println("Bearing Type     : " + AeiTag.BearingType.getDescription(me.getBearingType()));
                            System.out.println("Platform ID      : " + AeiTag.PlatformId.getDescription(me.getPlatformId()));
                            System.out.println("Type Detail Code : " + AeiTag.MultiModalTypeDetail.getDescription(me.getTypeDetail()));
                            System.out.println("Spare            : " + me.getSpare());

                            this.tagInformation.put("Number of axles",     String.valueOf(me.getNumberOfAxles()));
                            this.tagInformation.put("Bearing Type",        AeiTag.BearingType.getDescription(me.getBearingType()));
                            this.tagInformation.put("Platform ID",         AeiTag.PlatformId.getDescription(me.getPlatformId()));
                            this.tagInformation.put("Type Detail Code",    AeiTag.MultiModalTypeDetail.getDescription(me.getTypeDetail()));
                            this.tagInformation.put("Spare",               String.valueOf(me.getSpare()));

                        }
                    }
                }
                else
                {
                    System.out.println("Error : Unknown/unidentified tag type, can not proceed with tag parsing and displaying the raw ASCII data.");
                    System.out.println("Binary Format : " + binstrvalue);

                }

                System.out.println("EquipmentInitial : " + tag.getEquipmentInitial());
                this.tagInformation.put("EquipmentInitial",     tag.getEquipmentInitial());

                if(tag.isFieldValid().get(AeiTag.TagField.TAG_TYPE))
                {
                    System.out.println("Tag Type         : " +    AeiTag.TagType.getDescription(tag.getTagType()));
                    this.tagInformation.put("Tag Type",                AeiTag.TagType.getDescription(tag.getTagType()));
                }
                else
                {
                    System.out.println("Tag Type         : " +    leftpad(Integer.toBinaryString(tag.getTagType()), 2, '0')+ " (Not a valid TagType.)");
                    this.tagInformation.put("Tag Type",                leftpad(Integer.toBinaryString(tag.getTagType()), 2, '0')+ " (Not a valid TagType.)");
                }

                if(AeiTag.IsHalfFrameTag.getValue() == false)
                {
                    if(tag.isFieldValid().get(AeiTag.TagField.DATA_FORMAT))
                    {
                        System.out.println("Data Format code : " +    AeiTag.DataFormat.getDescription(tag.getDataFormat()));
                        this.tagInformation.put("Data Format code",        AeiTag.DataFormat.getDescription(tag.getDataFormat()));
                    }
                    else
                    {
                        System.out.println("Data Format code : " +    leftpad(Integer.toBinaryString(tag.getDataFormat()), 6, '0')+ " (Not a valid Data Format.)");
                        this.tagInformation.put("Data Format code",        leftpad(Integer.toBinaryString(tag.getDataFormat()), 6, '0')+ " (Not a valid Data Format.)");
                    }
                }
            }

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


    public void displayScannedTagDialog(final RFIDKeyboardWedge rfidKeyboardWedge1, final View inputView1, HashMap<String, String> tagInformation1) throws JSONException {
        ExpandableListView expandableListView;
        ExpandableListAdapter expandableListAdapter;
        final List<String> expandableListTitle;
        final HashMap<String, List<String>> expandableListDetail;

        if (!tagInformation1.isEmpty()){
            LastScannedTags.setmAtaTag(tagInformation1);

            // create a Dialog component
            final Dialog dialog = new Dialog(rfidKeyboardWedge1);

            //tell the Dialog to use the dialog.xml as it's layout description
            dialog.setContentView(R.layout.rfid_scan_dialog);
            dialog.setTitle("Scan Results");

            Button dialogButton1 = (Button) dialog.findViewById(R.id.dialogButton1);
            Button dialogButton2 = (Button) dialog.findViewById(R.id.dialogButton2);

            expandableListView = (ExpandableListView) dialog.findViewById(R.id.expandableListView1);
            expandableListDetail = JSONtoExpandableList.getData(LastScannedTags.getmAtaTag_JSON());
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
  
    static  int[] parseAntennaList(String argument)
    {
        int[] antennaList = null;
        try
        {
            String[] antennas = argument.split(",");
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
  
    private static String leftpad(String originalString, int length,char padCharacter)
        {
            String paddedString = originalString;
            while (paddedString.length() < length) {
             paddedString = padCharacter + paddedString;
            }
            return paddedString;
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

    static JSONObject hashMapToJSON(HashMap<String, String> hashMap){
        JSONObject jsonChild = null;
        JSONObject mainObject = null;

        try {

            /*Create Main Object's child entries*/

            // Create Child object
            jsonChild = new JSONObject(hashMap);
            mainObject = new JSONObject();

            // Create JSON array of child (TagInfo) Object
            JSONArray childObject_Arr = new JSONArray();
            childObject_Arr.put(jsonChild);

            /*Create Main JSON Object with Child object*/

            if (mainObject != null) {
                mainObject.put(String.valueOf(childObject_Arr.getJSONObject(0).get("EPC")), childObject_Arr);
            }


        }catch (Exception ex){
            System.out.println(ex.getMessage());

        }
        return mainObject;


        /*JSONObject mainObj = new JSONObject();
        try {
            *//*Create Main Object's child entries*//*

            // Create Child object
            JSONObject jo = new JSONObject();
            JSONObject ja = new JSONObject();
            jo.put("firstName", "John");
            jo.put("lastName", "Doe");
            ja.put("firstName", "Jane");
            ja.put("lastName", "Doe");

            // Create JSON array of child Object
            JSONArray jA = new JSONArray();
            jA.put(jo);
            jA.put(ja);

            *//*Create Main JSON Object with Child object*//*

            mainObj.put("employees", jA);

        } catch (JSONException e) {

            e.printStackTrace();
        }*/

    }
}