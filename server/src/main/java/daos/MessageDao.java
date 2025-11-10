package daos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Message;
import model.Pair;

public class MessageDao implements IDao<Pair<String, String>, List<Message>> {

    private Map<Pair<String, String>, List<Message>> messages;

    public MessageDao() {
        this.messages = new HashMap<>();
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

    public List<Message> saveSingle(Message message) {
        Pair<String, String> key = normalizeKey(message.getSender(), message.getReceiver());

        List<Message> existing = messages.getOrDefault(key, new ArrayList<>());
        existing.add(message);
        messages.put(key, existing);
        return existing;
    }
}
