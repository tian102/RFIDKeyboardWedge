package com.homemade.tianp.rfidkeyboardwedge;

import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LastScannedTags {

    /*Class Variables*/
    private static HashMap<String, String> mAtaTag;
    private static HashMap<String, String> mGen2Tag;
    private static HashMap<String, List<String>> mGen2Tags;

    /*Getters & Setters*/

    public static HashMap<String, String> getmAtaTag() {
        return mAtaTag;
    }
    public static JSONObject getmAtaTag_JSON() {
        return getTagJSON(mAtaTag);
    }

    public static void setmAtaTag(HashMap<String, String> mAtaTag) {
        LastScannedTags.mAtaTag = mAtaTag;
    }

    public static HashMap<String, String> getmGen2Tag() {
        return mGen2Tag;
    }

    public static JSONObject getmGen2Tag_JSON() {
        return getTagJSON(mGen2Tag);
    }

    public static void setmGen2Tag(HashMap<String, String> mGen2Tag) {
        LastScannedTags.mGen2Tag = mGen2Tag;
    }

    public static HashMap<String, List<String>> getmGen2Tags() {
        return mGen2Tags;
    }

    public static JSONObject getmGen2Tags_JSON() {
        return getMultiTagJSON(mGen2Tags);
    }

    public static void setmGen2Tags(HashMap<String, List<String>> mGen2Tags) {
        LastScannedTags.mGen2Tags = mGen2Tags;
    }

    /*Additional functions*/

    public static JSONObject getTagJSON(HashMap<String, String> hashMap){
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

    }

    public static JSONObject getMultiTagJSON(HashMap<String, List<String>> hashMap){
        JSONObject jsonChild = null;
        JSONObject mainObject = null;

        try {
            /*Create Main Object's child entries*/

            // Create Child object
            mainObject = new JSONObject(hashMap);


        }catch (Exception ex){
            System.out.println(ex.getMessage());

        }
        return mainObject;

    }

    public static HashMap<String, String> jsonToHashmap(JSONObject jObject){

        HashMap<String, String> mainObject = null;


        return mainObject;

    }

    public static void displayScannedTagDialog(final RFIDKeyboardWedge rfidKeyboardWedge1, final View inputView1, HashMap<String, String> tagInformation1) throws JSONException {
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
                    rfidKeyboardWedge1.StringToWedge(string);
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

    public static void displayScannedTagsDialog(final RFIDKeyboardWedge rfidKeyboardWedge1, final View inputView1, HashMap<String, List<String>> tagInformation1) throws JSONException {
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
                    rfidKeyboardWedge1.StringToWedge(string);
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


}