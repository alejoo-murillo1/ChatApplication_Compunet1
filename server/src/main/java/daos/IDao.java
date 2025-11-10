package daos;

import java.util.List;

public interface IDao<K,V> {
    public List<K> findAllKeys();
    public List<V> findAllValues();
    public V finById(K id);
    public V update(V newEntity);
    public boolean delete(V entity);
    public V save(V entity);
}
