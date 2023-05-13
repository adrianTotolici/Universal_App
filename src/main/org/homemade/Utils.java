package org.homemade;

import java.io.*;
import java.util.HashMap;

public class Utils {

    public static void Log(Object obj){
        String callerClassName = new Exception().getStackTrace()[1].getClassName();
        System.out.println("("+callerClassName+") "+obj.toString());
    }

    public static void saveData(Object object, String filePath){
        try {
            File file = new File(filePath);
            FileOutputStream oFile = new FileOutputStream(file, false);

            ObjectOutputStream objectOut = new ObjectOutputStream(oFile);
            objectOut.writeObject(object);
            objectOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
