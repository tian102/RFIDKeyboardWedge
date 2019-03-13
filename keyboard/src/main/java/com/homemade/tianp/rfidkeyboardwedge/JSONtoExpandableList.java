package com.homemade.tianp.rfidkeyboardwedge;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONtoExpandableList {
    public static HashMap<String, List<String>> getData(JSONObject jsonTaglist) throws JSONException {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();

        Map<String, Object> a = jsonToMap(jsonTaglist);
        
        List<String> scannedTag = new ArrayList<String>();
        Map.Entry<String,Object> entry = a.entrySet().iterator().next();
        String key = entry.getKey();

        ArrayList<HashMap<String,String>> valueArr= (ArrayList<HashMap<String,String>>) entry.getValue();


        Iterator it = valueArr.get(0).entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            scannedTag.add(pair.getKey() + " = " + pair.getValue());
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }


        expandableListDetail.put(key, scannedTag);
        
        return expandableListDetail;
    }



    /*TODO: Needs to be fixed*/
    public static HashMap<String, List<String>> getMultiTagData(JSONObject jsonTaglist) throws JSONException {
        HashMap<String, List<String>> expandableListDetail = new HashMap<String, List<String>>();
        String key = "";
        List<String> strValues = new ArrayList<>();
        Map<String, Object> a = jsonToMap(jsonTaglist);
        Iterator parentObj =  a.entrySet().iterator();
        ArrayList<HashMap<String,String>> valueArr = new ArrayList<HashMap<String,String>>();

        while (parentObj.hasNext()){
            Map.Entry pair = (Map.Entry)parentObj.next();
            key = (String) pair.getKey();
            //ArrayList<HashMap<String,String>> valueArr= (ArrayList<HashMap<String,String>>) key.get.getValue();
            Iterator childObj = valueArr.get(0).entrySet().iterator();
            while (childObj.hasNext()) {
                Map.Entry pair1 = (Map.Entry)childObj.next();
                strValues.add(pair1.getKey() + " = " + pair1.getValue());
                System.out.println(pair1.getKey() + " = " + pair1.getValue());
                childObj.remove(); // avoids a ConcurrentModificationException
            }
            parentObj.remove(); // avoids a ConcurrentModificationException
            expandableListDetail.put(key,strValues);
        }


//        valueArr.add((HashMap<String,String>)currentKey.getValue());
//        currentKey = a.entrySet().iterator().next();
//        valueArr.add((HashMap<String,String>)currentKey.getValue());

        return expandableListDetail;
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
