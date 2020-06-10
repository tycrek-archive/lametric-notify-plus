package dev.jmoore.lametricnotify;

import android.graphics.drawable.Drawable;

public class AppItem {
    private final Drawable appIcon;
    private final String appName;
    private final String appPackage;

    public AppItem(Drawable i, String n, String p) {
        this.appIcon = i;
        this.appName = n;
        this.appPackage = p;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppPackage() {
        return appPackage;
    }
}
