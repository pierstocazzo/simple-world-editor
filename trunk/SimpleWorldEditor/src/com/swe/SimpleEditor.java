package com.swe;


import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;



public class SimpleEditor {

    public static void main(String[] args) {
        EditorBaseManager app = EditorBaseManager.getInstance();
        AppSettings aps = new AppSettings(true);
        aps.setVSync(true);
//        aps.setFrameRate(80);
//        aps.setResolution(1600, 800);
        app.setSettings(aps);
        app.setShowSettings(false);
        app.start();
        
    }
}
