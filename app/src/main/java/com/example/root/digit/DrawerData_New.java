package com.example.root.digit;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nglofton on 7/11/15.
 */
public class DrawerData_New {
    public List<Map<String,?>> items;
    public static final int TYPE_HEADER = 0;
    public static final int TYPE1 = 1;
    public static final int TYPE2 = 2;
    public static final int TYPE3 = 3;

    public DrawerData_New(HashMap user) {
        items = new ArrayList<Map<String,?>>();
        items.add(createDrawerHeader("Header", user, TYPE_HEADER));
        items.add(createDrawerItemWithIcon("Tags", R.drawable.tag_icon, TYPE1));
        items.add(createDrawerItemWithIcon("Line", R.drawable.drawer_divider, TYPE2));
        items.add(createDrawerItemWithIcon("About Develepor", R.drawable.about_me, TYPE3));
        items.add(createDrawerItemWithIcon("Log Out", R.drawable.log_out,TYPE3));
    }

    public List getDrawerList() {
        return items;
    }

    private HashMap createDrawerHeader(String title, HashMap userData, int viewType) {
        HashMap item = new HashMap();
        item.put("title",title);
        item.put("username", userData.get("username"));
        item.put("email", userData.get("email"));
        item.put("icon", userData.get("profile_pic"));
        item.put("type", viewType);
        return item;
    }
    private HashMap createDrawerItemWithIcon(String title, int icon, int viewType) {
        HashMap item = new HashMap();
        item.put("title",title);
        item.put("icon",icon);
        item.put("type", viewType);
        return item;
    }
    private HashMap createDrawerItem(String title, int viewType) {
        HashMap item = new HashMap();
        item.put("title",title);
        item.put("type", viewType);
        return item;
    }
}

