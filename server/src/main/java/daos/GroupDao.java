package daos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Group;

public class GroupDao implements IDao<String,Group> {

    private Map<String, Group> groups;

    public GroupDao(){
        groups = new HashMap<>();
    }

    @Override
    public List<String> findAllKeys() {
        List<String> lista = new ArrayList<>();

        for (Map.Entry<String, Group> entry : groups.entrySet()) {
            lista.add(entry.getKey());
        }

        return lista;
    }

    @Override
    public List<Group> findAllValues() {
        List<Group> lista = new ArrayList<>();

        for (Map.Entry<String, Group> entry : groups.entrySet()) {
            lista.add(entry.getValue());
        }

        return lista;
    }

    @Override
    public Group finById(String name) {
        return groups.get(name);
    }

    @Override
    public Group update(Group newEntity) {
        String name = newEntity.getName();
        groups.get(name).setMembers(newEntity.getMembers());

        return groups.get(name);
    }

    @Override
    public boolean delete(Group entity) {
        String name = entity.getName();
        groups.remove(name);
        return groups.containsKey(name);
    }

    @Override
    public Group save(Group entity) {
        String name = entity.getName();
        groups.put(name, entity);
        return groups.get(name);
    }
    
}
