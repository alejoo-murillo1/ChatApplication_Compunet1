package daos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;

import model.Message;
import model.Pair;
import persistence.JsonFileUtils;

public class MessageDao implements IDao<Pair<String, String>, List<Message>> {

    private Map<Pair<String, String>, List<Message>> messages;

    private final String filePath = "data/messages.json";

    public MessageDao() {
        ensureFileExists();
        loadFromFile();
    }

    private void ensureFileExists() {
        File dataFolder = new File("data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File file = new File(filePath);
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        Type listType = new TypeToken<List<Message>>() {}.getType();
        List<Message> list = JsonFileUtils.readListFromFile(filePath, listType);
        messages = new HashMap<>();
        if (list != null) {
            for (Message m : list) {
                Pair<String,String> key = normalizeKey(m.getSender(), m.getReceiver());
                messages.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
            }
        }
    }

    private void saveToFile() {
        List<Message> allMessages = messages.values().stream()
                                            .flatMap(List::stream)
                                            .toList();
        try {
            JsonFileUtils.writeListToFile(filePath, allMessages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Pair<String, String>> findAllKeys() {
        return new ArrayList<>(messages.keySet());
    }

    @Override
    public List<List<Message>> findAllValues() {
        return new ArrayList<>(messages.values());
    }

    @Override
    public List<Message> finById(Pair<String, String> id) {
        Pair<String, String> key = normalizeKey(id.getFirst(), id.getSecond());
        return messages.getOrDefault(key, new ArrayList<>());
    }

    @Override
    public List<Message> update(List<Message> newEntity) {
        if (newEntity == null || newEntity.isEmpty()) return null;

        Message first = newEntity.get(0);
        Pair<String, String> key = normalizeKey(first.getSender(), first.getReceiver());

        messages.put(key, newEntity);
        return newEntity;
    }

    @Override
    public boolean delete(List<Message> entity) {
        if (entity == null || entity.isEmpty()) return false;

        Message first = entity.get(0);
        Pair<String, String> key = normalizeKey(first.getSender(), first.getReceiver());

        return messages.remove(key) != null;
    }

    // 🔹 Agrega mensajes a una conversación existente o crea una nueva
    @Override
    public List<Message> save(List<Message> entity) {
        if (entity == null || entity.isEmpty()) return null;

        Message first = entity.get(0);
        Pair<String, String> key = normalizeKey(first.getSender(), first.getReceiver());

        List<Message> existing = messages.getOrDefault(key, new ArrayList<>());
        existing.addAll(entity);

        messages.put(key, existing);
        return existing;
    }

    // 🔸 Método auxiliar: garantiza que (A, B) y (B, A) sean la misma clave
    private Pair<String, String> normalizeKey(String a, String b) {
        if (a.compareTo(b) < 0) {
            return new Pair<>(a, b);
        } else {
            return new Pair<>(b, a);
        }
    }

    public List<Message> saveSingle(Message message, boolean isGroup) {
        Pair<String, String> key;

        if(!isGroup) {
            key = normalizeKey(message.getSender(), message.getReceiver());
        } else {
            key = new Pair<>(message.getReceiver(), ""); //Solo nos importa el nombre del grupo
        }
        
        List<Message> existing = messages.getOrDefault(key, new ArrayList<>());
        existing.add(message);
        messages.put(key, existing);
        saveToFile();
        
        return existing;
    }


    public List<Message> findByGroup(String groupName) {
        Pair<String, String> key = new Pair<>(groupName, "");
    
        return messages.getOrDefault(key, new ArrayList<>());
    }
}
