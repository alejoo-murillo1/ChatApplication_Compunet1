package persistence;

import com.google.gson.Gson;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;

public class JsonFileUtils {

    private static final Gson gson = new Gson();

    public static <T> List<T> readListFromFile(String path, Type type) {
        try (Reader reader = new FileReader(path)) {
            List<T> list = gson.fromJson(reader, type);
            if (list == null) return List.of();
            return list;
        } catch (IOException e) {
            System.out.println("No se encontró el archivo " + path + ", creando uno nuevo.");
            return List.of();
        }
    }

    public static <T> void writeListToFile(String path, List<T> list) {
        try (Writer writer = new FileWriter(path)) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
