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
import aeitagdecoder.*;
import com.thingmagic.*;
import java.math.BigInteger;
import java.util.List;

public class AEITagDecoding
{

    private Object tag = null;
    private String tagType = "";

    public Object getTag() {
        return tag;
    }

    public String getTagType() {
        return tagType;
    }

    public static void setTrace(Reader r, String args[])
  {    
    if (args[0].toLowerCase().equals("on"))
    {
      r.addTransportListener(r.simpleTransportListener);
    }    
  }

    public AEITagDecoding(Reader connectedReader)
    {
    // Program setup
    Reader r = null;
    int nextarg = 0;
    boolean trace = false;
    int[] antennaList = null;


    
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
        
        SimpleReadPlan plan = new SimpleReadPlan(antennaList, TagProtocol.ATA, null, null, 1000);
        r.paramSet(TMConstants.TMR_PARAM_READ_PLAN, plan);
        
        // Read tags
        tagReads = r.read(500);
         
        // Considers the first tag read to be decoded
        String TagEPC = tagReads[0].getTag().epcString();
        
        // AEI ATA Tag details
        System.out.println("********************* AEI ATA Tag Details ****************************");
        System.out.println("EPC Tag data : " + TagEPC);
        
        // Decode the tag
        AeiTag tag = AeiTag.decodeTagData(TagEPC);
        String binstrvalue = AEITagDecoding.hexToBin(TagEPC);

        if((AeiTag.DataFormat.getDescription(tag.getDataFormat()).matches("6-bit ASCII"))&&(AeiTag.IsHalfFrameTag.getValue() == false))
        {
            System.out.println("Binary Format : " + binstrvalue);
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
                if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Railcar")))
                {
                    Railcar rc = (Railcar)tag;
                    System.out.println("Car Number       : " + rc.getCarNumber());
                    System.out.println("Side Indicator   : " + AeiTag.SideIndicator.getDescription(rc.getSide()));
                    System.out.println("Length(dm)       : " + rc.getLength());
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Number of axles  : " + rc.getNumberOfAxles());
                        System.out.println("Bearing Type     : " + AeiTag.BearingType.getDescription(rc.getBearingType()));
                        System.out.println("Platform ID      : " + AeiTag.PlatformId.getDescription(rc.getPlatformId()));
                        System.out.println("Spare            : " + rc.getSpare());
                    }
                    tagType = "Railcar";
                    tag = rc;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("End-of-train device")))
                {
                    EndOfTrain eot = (EndOfTrain)tag;
                    System.out.println("EOT Number       : " + eot.getEotNumber());
                    System.out.println("EOT Type         : " + EndOfTrain.EotType.getDescription(eot.getEotType()));
                    System.out.println("Side Indicator   : " + EndOfTrain.SideIndicator.getDescription(eot.getSide()));
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Spare            : " + eot.getSpare());
                    }
                    tagType = "End-of-train device";
                    tag = eot;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Locomotive")))
                {
                    Locomotive loc = (Locomotive)tag;
                    System.out.println("Car Number       : " + loc.getLocomotiveNumber());
                    System.out.println("Side Indicator   : " + AeiTag.SideIndicator.getDescription(loc.getSide()));
                    System.out.println("Length(dm)       : " + loc.getLength());
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Number of axles  : " + loc.getNumberOfAxles());
                        System.out.println("Bearing Type     : " + AeiTag.BearingType.getDescription(loc.getBearingType()));
                        System.out.println("Spare            : " + loc.getSpare());
                    }
                    tagType = "Locomotive";
                    tag = loc;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Intermodal container")))
                {
                    Intermodal imod =  (Intermodal)tag;
                    System.out.println("Car Number                : " + imod.getIdNumber());
                    System.out.println("Check Digit               : " + imod.getCheckDigit());
                    System.out.println("Length(cm)                : " + imod.getLength());
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Height(cm)                : " + imod.getHeight());
                        System.out.println("Width(cm)                 : " + imod.getWidth());
                        System.out.println("Container Type            : " + imod.getContainerType());
                        System.out.println("Container Max Gross Weight: " + imod.getMaxGrossWeight());
                        System.out.println("Container Tare Weight     : " + imod.getTareWeight());
                        System.out.println("Spare                     : " + imod.getSpare());
                    }
                    tagType = "Intermodal container";
                    tag = imod;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Trailer")))
                {
                    Trailer trail = (Trailer)tag;
                    System.out.println("Trailer Number            : " + trail.getTrailerNumber());
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Length(cm)                : " + trail.getLength());
                        System.out.println("Width                     : " + AeiTag.Width.getDescription(trail.getWidth()));
                        System.out.println("Height                    : " + trail.getHeight());
                        System.out.println("Tandem Width              : " + AeiTag.Width.getDescription(trail.getTandemWidth()));
                        System.out.println("Type Detail Code          : " + AeiTag.TrailerTypeDetail.getDescription(trail.getTypeDetail()));
                        System.out.println("Forward Extension         : " + trail.getForwardExtension());
                        System.out.println("Tare Weight               : " + trail.getTareWeight());
                    }
                    tagType = "Trailer";
                    tag = trail;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Chassis")))
                {
                    Chassis chas = (Chassis)tag;
                    System.out.println("Chassis Number            : " + chas.getChassisNumber());
                    System.out.println("Type Detail Code          : " + AeiTag.ChassisTypeDetail.getDescription(chas.getTypeDetail()));
                    System.out.println("Tare Weight               : " + chas.getTareWeight());
                    System.out.println("Height                    : " + chas.getHeight());
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
                    }
                    tagType = "Chassis";
                    tag = chas;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Railcar cover")))
                {
                    RailcarCover rcov = (RailcarCover)tag;
                    System.out.println("Car Number                   : " + rcov.getEquipmentNumber());
                    System.out.println("Side Indicator               : " + AeiTag.SideIndicator.getDescription(rcov.getSide()));
                    System.out.println("Length                       : " + rcov.getLength());
                    System.out.println("Cover Type                   : " + AeiTag.CoverType.getDescription(rcov.getCoverType()));
                    System.out.println("Date built or rebuilt        : " + AeiTag.DateBuilt.getDescription(rcov.getDateBuilt()));
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Insulation                   : " + AeiTag.Insulation.getDescription(rcov.getInsulation()));
                        System.out.println("Fitting code/Lifting Bracket : " + AeiTag.Fitting.getDescription(rcov.getFitting()));
                        System.out.println("Associated railcar initial   : " + rcov.getAssocRailcarInitial());
                        System.out.println("Associated railcar number    : " + rcov.getAssocRailcarNum());
                    }
                    tagType = "Railcar cover";
                    tag = rcov;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Passive alarm tag")))
                {
                    PassiveAlarmTag pa = (PassiveAlarmTag)tag;
                    System.out.println("Car Number       : " + pa.getEquipmentNumber());
                    System.out.println("Side Indicator   : " + AeiTag.SideIndicator.getDescription(pa.getSide()));
                    System.out.println("Length(dm)       : " + pa.getLength());
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Number of axles  : " + pa.getNumberOfAxles());
                        System.out.println("Bearing Type     : " + AeiTag.BearingType.getDescription(pa.getBearingType()));
                        System.out.println("Platform ID      : " + AeiTag.PlatformId.getDescription(pa.getPlatformId()));
                        System.out.println("AlarmCode        : " + AeiTag.Alarm.getDescription(pa.getSpare()));
                        System.out.println("Spare            : " + pa.getSpare());
                    }
                    tagType = "Passive alarm tag";
                    tag = pa;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Generator set")))
                {
                    GeneratorSet gs = (GeneratorSet)tag;
                    System.out.println("Generator set number : " + gs.getGensetNumber());
                    System.out.println("Mounting code        : " + AeiTag.Mounting.getDescription(gs.getMounting()));
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Tare weight          : " + gs.getTareWeight());
                        System.out.println("Fuel Capacity        : " + AeiTag.FuelCapacity.getDescription(gs.getFuelCapacity()));
                        System.out.println("Voltage              : " + AeiTag.Voltage.getDescription(gs.getVoltage()));
                        System.out.println("Spare                : " + gs.getSpare());
                    }
                    tagType = "Generator set";
                    tag = gs;
                }
                else if((AeiTag.EquipmentGroup.getDescription(tag.getEquipmentGroup()).matches("Multimodal equipment")))
                {
                    MultiModalEquipment me = (MultiModalEquipment)tag;
                    System.out.println("Equipment Number : " + me.getEquipmentNumber());
                    System.out.println("Side Indicator   : " + AeiTag.SideIndicator.getDescription(me.getSide()));
                    System.out.println("Length(dm)       : " + me.getLength());
                    if(AeiTag.IsHalfFrameTag.getValue() == false)
                    {
                        System.out.println("Number of axles  : " + me.getNumberOfAxles());
                        System.out.println("Bearing Type     : " + AeiTag.BearingType.getDescription(me.getBearingType()));
                        System.out.println("Platform ID      : " + AeiTag.PlatformId.getDescription(me.getPlatformId()));
                        System.out.println("Type Detail Code : " + AeiTag.MultiModalTypeDetail.getDescription(me.getTypeDetail()));
                        System.out.println("Spare            : " + me.getSpare());
                    }
                    tagType = "Multimodal equipment";
                    tag = me;
                }
            }
            else 
            {
                System.out.println("Error : Unknown/unidentified tag type, can not proceed with tag parsing and displaying the raw ASCII data.");
                System.out.println("Binary Format : " + binstrvalue);

            }

            System.out.println("EquipmentInitial : " + tag.getEquipmentInitial());

            if(tag.isFieldValid().get(AeiTag.TagField.TAG_TYPE))
            {
                System.out.println("Tag Type         : " + AeiTag.TagType.getDescription(tag.getTagType()));
            }
            else
            {
                System.out.println("Tag Type         : " + leftpad(Integer.toBinaryString(tag.getTagType()), 2, '0')+ " (Not a valid TagType.)");
            }

            if(AeiTag.IsHalfFrameTag.getValue() == false)
            {
                if(tag.isFieldValid().get(AeiTag.TagField.DATA_FORMAT))
                {
                    System.out.println("Data Format code : " +  AeiTag.DataFormat.getDescription(tag.getDataFormat()));
                }
                else
                {
                    System.out.println("Data Format code : " + leftpad(Integer.toBinaryString(tag.getDataFormat()), 6, '0')+ " (Not a valid Data Format.)");
                }
            }
        }  
        
        // Shut down reader
        r.destroy();
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
}

