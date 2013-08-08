package com.simpleEditor;

import java.io.File;
import javax.swing.filechooser.*;

/**
 *
 * @author @author Ulrich Nzuzi <ulrichnz@code.google.com>
 */
public class EditorSceneFilter extends FileFilter{
    //Accept j3o and ogre xml files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
 
//        String extension = getExtension(f);
//        if (extension != null) {
            if (f.getName().indexOf(".swe") > 0){
                    return true;
            } else {
                return false;
            }
//        }
 
//        return false;
    }
 
    //The description of this filter
    public String getDescription() {
        return "Scene (*.swe)";
    }
    
    /*
     * Get the extension of a file.
     */
//    private static String getExtension(File f) {
//        String ext = null;
//        String s = f.getName();
//        int i = s.indexOf('.');
//                //lastIndexOf('.');
// 
//        if (i > 0 &&  i < s.length() - 1) {
//            ext = s.substring(i+1).toLowerCase();
//        }
//        return ext;
//    }
}
