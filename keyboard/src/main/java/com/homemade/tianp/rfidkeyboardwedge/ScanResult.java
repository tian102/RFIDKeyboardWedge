package com.homemade.tianp.rfidkeyboardwedge;

import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Used to get and/or set the scanned UID.
 *
 * @author  Tian Pretorius
 * @version 1.0
 * @since   2017-03-15
 *
 * Created by tianp on 24 Mar 2017.
 */

public class ScanResult {
    public static String ProductID = "Default ID";
    public static String HistoryProductID = "Default ID";

    public static List<String> ProductID_Array = new ArrayList<>();
    public static List<String> HistoryProductID_Array = new ArrayList<>();


    public static void resetAll(){
        ProductID = "Default ID";
        HistoryProductID = "Default ID";

        ProductID_Array = new ArrayList<>();
        HistoryProductID_Array = new ArrayList<>();
    }

    public static String GetProductID(){
        return ProductID;
    }

    public static boolean SetProductID(String productID){
        ProductID = productID;
        return true;
    }

    public static String GetHistoryProductID(){
        return HistoryProductID;
    }

    public static boolean SetHistoryProductID(String historyProductID){
        HistoryProductID = historyProductID;
        return true;
    }

    public static void convertProductID_Array(){
        String temp = "";
        for (String item:ProductID_Array) {
            temp += item +"\n";
        }
        ProductID = temp;
    }

    public static boolean SetProductID_Array(String productID){
        if (!ProductID_Array.contains(productID)){
            ProductID_Array.add(productID);
        }
        return true;
    }
    public static void convertHistoryProductID_Array(){
        String temp = "";
        for (String item:HistoryProductID_Array) {
            temp += item +"\n";
        }
        HistoryProductID = temp;
    }

    public static boolean SetHistoryProductID_Array(String historyProductID){
        if (!HistoryProductID_Array.contains(historyProductID)){
            HistoryProductID_Array.add(historyProductID);
        }

        return true;
    }


}
