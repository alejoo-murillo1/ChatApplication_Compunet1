package daos;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.reflect.TypeToken;

import model.Message;
import model.Pair;
import persistence.JsonFileUtils;

public class MessageDao implements IDao<Pair<String, String>, List<Message>> {

    private Map<Pair<String, String>, List<Message>> userMessages;
    private Map<String, List<Message>> groupMessages;

    private final String userFilePath = "data/messages_users.json";
    private final String groupFilePath = "data/messages_groups.json";

    public MessageDao() {
        ensureFilesExist();
        loadFromFiles();
    }

    private void ensureFilesExist() {
        File dataFolder = new File("data");
        if (!dataFolder.exists()) dataFolder.mkdirs();

        try {
            File usersFile = new File(userFilePath);
            if (!usersFile.exists()) usersFile.createNewFile();

            File groupsFile = new File(groupFilePath);
            if (!groupsFile.exists()) groupsFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFiles() {
        try {
            // Cargar mensajes entre usuarios
            Type userListType = new TypeToken<List<Message>>() {}.getType();
            List<Message> userList = JsonFileUtils.readListFromFile(userFilePath, userListType);
            userMessages = new HashMap<>();

            if (userList != null) {
                for (Message m : userList) {
                    Pair<String, String> key = normalizeKey(m.getSender(), m.getReceiver());
                    userMessages.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
                }
            }

            // Cargar mensajes de grupos
            Type groupListType = new TypeToken<List<Message>>() {}.getType();
            List<Message> groupList = JsonFileUtils.readListFromFile(groupFilePath, groupListType);
            groupMessages = new HashMap<>();

            if (groupList != null) {
                for (Message m : groupList) {
                    groupMessages.computeIfAbsent(m.getReceiver(), k -> new ArrayList<>()).add(m);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            userMessages = new HashMap<>();
            groupMessages = new HashMap<>();
        }
    }

    private void saveUserMessages() {
        try {
            List<Message> all = userMessages.values().stream().flatMap(List::stream).toList();
            JsonFileUtils.writeListToFile(userFilePath, all);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveGroupMessages() {
        try {
            List<Message> all = groupMessages.values().stream().flatMap(List::stream).toList();
            JsonFileUtils.writeListToFile(groupFilePath, all);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===============================================================
    // ============= MÉTODOS PARA MENSAJES ENTRE USUARIOS =============
    // ===============================================================

    @Override
    public List<Message> save(List<Message> entity) {
        if (entity == null || entity.isEmpty()) return null;

        Message first = entity.get(0);
        Pair<String, String> key = normalizeKey(first.getSender(), first.getReceiver());

        List<Message> existing = userMessages.getOrDefault(key, new ArrayList<>());
        existing.addAll(entity);
        userMessages.put(key, existing);

        saveUserMessages();
        return existing;
    }

    public List<Message> saveUserMessage(Message message) {
        Pair<String, String> key = normalizeKey(message.getSender(), message.getReceiver());
        List<Message> existing = userMessages.getOrDefault(key, new ArrayList<>());
        existing.add(message);
        userMessages.put(key, existing);
        saveUserMessages();
        return existing;
    }

    @Override
    public List<Message> finById(Pair<String, String> id) {
        Pair<String, String> key = normalizeKey(id.getFirst(), id.getSecond());
        return userMessages.getOrDefault(key, new ArrayList<>());
    }

    // ===============================================================
    // =================== MÉTODOS PARA GRUPOS =======================
    // ===============================================================

    public List<Message> saveGroupMessage(Message message) {
        String groupName = message.getReceiver();
        List<Message> existing = groupMessages.getOrDefault(groupName, new ArrayList<>());
        existing.add(message);
        groupMessages.put(groupName, existing);
        saveGroupMessages();
        return existing;
    }

    public List<Message> findByGroup(String groupName) {
        return groupMessages.getOrDefault(groupName, new ArrayList<>());
    }

    // ===============================================================
    // ====================== MÉTODOS AUXILIARES =====================
    // ===============================================================

    private Pair<String, String> normalizeKey(String a, String b) {
        return (a.compareTo(b) < 0) ? new Pair<>(a, b) : new Pair<>(b, a);
    }

    @Override
    public List<Pair<String, String>> findAllKeys() {
        return new ArrayList<>(userMessages.keySet());
    }

    @Override
    public List<List<Message>> findAllValues() {
        return new ArrayList<>(userMessages.values());
    }

    @Override
    public List<Message> update(List<Message> newEntity) {
        if (newEntity == null || newEntity.isEmpty()) return null;
        Message first = newEntity.get(0);
        Pair<String, String> key = normalizeKey(first.getSender(), first.getReceiver());
        userMessages.put(key, newEntity);
        saveUserMessages();
        return newEntity;
    }

    @Override
    public boolean delete(List<Message> entity) {
        if (entity == null || entity.isEmpty()) return false;
        Message first = entity.get(0);
        Pair<String, String> key = normalizeKey(first.getSender(), first.getReceiver());
        boolean removed = userMessages.remove(key) != null;
        if (removed) saveUserMessages();
        return removed;
    }
}
