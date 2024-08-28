package io.github.oasisframework.hazelcast.three.helper.manager;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.github.oasisframework.hazelcast.common.manager.OasisHazelcastMapManager;
import io.github.oasisframework.hazelcast.three.helper.connection.HazelcastConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class OasisHazelcastMapManagerImpl implements OasisHazelcastMapManager {
	private final HazelcastInstance hazelcastInstance;

	public OasisHazelcastMapManagerImpl(@Qualifier(HazelcastConnection.HAZELCAST_INSTANCE)HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@Override
	public <K, V> V getValueFromMap(String mapName, K key) {
		IMap<K,V> map = hazelcastInstance.getMap(mapName);
		return map.get(key);
	}

	@Override
	public <K, V> void addValueToMap(String mapName, K key, V value) {
		IMap<K,V> map = hazelcastInstance.getMap(mapName);
		map.set(key, value);
	}

	public boolean contains(String mapName, String key){
		try{
			return hazelcastInstance.getMap(mapName).containsKey(key);
		}catch (RuntimeException ex){
			return false;
		}
	}
}
