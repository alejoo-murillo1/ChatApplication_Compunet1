package daos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.User;

public class UserDao implements IDao<String, User> {

    private Map<String, User> users;

    public UserDao(){
        users = new HashMap<>();
    }

    @Override
    public List<String> findAllKeys() {
        List<String> lista = new ArrayList<>();

        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            if (user.isOnline()) {
                lista.add(entry.getKey());
            }
        }

        return lista;
    }

    @Override
    public List<User> findAllValues() {
        List<User> lista = new ArrayList<>();

        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            if (user.isOnline()) {
                lista.add(user);
            }
        }

        return lista;
    }

    @Override
    public User finById(String name) {
        return users.get(name);
    }

    @Override
    public User update(User newEntity) {
        String name = newEntity.getName();
        users.get(name).setOnline(newEntity.isOnline());

        return users.get(name);
    }

    @Override
    public boolean delete(User entity) {
        String name = entity.getName();
        users.remove(name);
        return users.containsKey(name);
    }

    @Override
    public User save(User entity) {
        String name = entity.getName();
        users.put(name, entity);
        return users.get(name);
    }
}
