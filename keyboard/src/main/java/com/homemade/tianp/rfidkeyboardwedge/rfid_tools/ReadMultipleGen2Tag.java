/**
 * Sample program that reads tags in the background and prints the
 * tags found.
 */

// Import the API
package com.homemade.tianp.rfidkeyboardwedge.rfid_tools;
import android.content.SharedPreferences;

import com.thingmagic.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReadMultipleGen2Tag
{
    static SerialPrinter serialPrinter;
    static StringPrinter stringPrinter;
    static TransportListener currentListener;

    public ReadMultipleGen2Tag(Reader connectedReader, SharedPreferences prefs)
    {
        // Program setup
        Reader r = null;
        boolean trace = false;
        int[] antennaList = null;


        // Create Reader object, connecting to physical device
        try
        {

            antennaList = parseAntennaList("1,2");
            r = connectedReader;
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
            ReadExceptionListener exceptionListener = new TagReadExceptionReceiver();
            r.addReadExceptionListener(exceptionListener);
            // Create and add tag listener
            ReadListener rl = new PrintListener();
            r.addReadListener(rl);
            // search for tags in the background
            r.startReading();
            System.out.println("Do other work here");
            Thread.sleep(500);
            System.out.println("Do other work here");
            Thread.sleep(500);
            r.stopReading();

            r.removeReadListener(rl);
            r.removeReadExceptionListener(exceptionListener);

            // Shut down reader
            r.destroy();
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

   static class SerialPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
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

  static class StringPrinter implements TransportListener
  {
    public void message(boolean tx, byte[] data, int timeout)
    {
      System.out.println((tx ? "Sending:\n" : "Receiving:\n") +
                         new String(data));
    }
  }

  static class PrintListener implements ReadListener
  {
    public void tagRead(Reader r, TagReadData tr)
    {
      System.out.println("Background read: " + tr.toString());
    }

  }

  static class TagReadExceptionReceiver implements ReadExceptionListener
  {
        String strDateFormat = "M/d/yyyy h:m:s a";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
        public void tagReadException(Reader r, ReaderException re)
        {
            String format = sdf.format(Calendar.getInstance().getTime());
            System.out.println("Reader Exception: " + re.getMessage() + " Occured on :" + format);
            if(re.getMessage().equals("Connection Lost"))
            {
                System.exit(1);
            }
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
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        return antennaList;
    }
  
}
