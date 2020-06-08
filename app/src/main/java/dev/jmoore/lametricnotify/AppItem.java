package dev.jmoore.lametricnotify;

public class AppItem {
    private String appName, appPackage;

    public AppItem() {}

    public AppItem(String n, String p) {
        this.appName = n;
        this.appPackage = p;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

}
