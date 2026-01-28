/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package MasterCut.domain.utils;

/**
 *
 * @author js015
 */
import java.io.*;

public class DeepCopy {
    public static <T extends Serializable> T deepCopy(T original) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(original);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            return (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to copy object.", e);
        }
    }
}
