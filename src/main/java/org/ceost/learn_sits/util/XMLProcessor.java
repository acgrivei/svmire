/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ceost.learn_sits.util;

import com.thoughtworks.xstream.XStream;
import java.io.FileWriter;
import java.io.IOException;
import org.ceost.learn_sits.model.SVMSettings;
import org.ceost.learn_sits.model.Settings;

/**
 *
 * @author Alex
 */
public class XMLProcessor {

    public static XStream xstream;

    public static void init() {
        xstream = new XStream();
        xstream.alias("settings", Settings.class);
        xstream.alias("SVMsettings", SVMSettings.class);
    }

    public static void dataToXML(Settings data, String destination) throws IOException {
        String xml = xstream.toXML(data);
        FileWriter fw = new FileWriter(destination);        
        fw.write(xml);
        fw.close();
    }

    public static Settings XMLToData(String xml) {
        return (Settings) xstream.fromXML(xml);
    }

}
