package io.github.oasisframework.hazelcast.three.helper.manager;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import io.github.oasisframework.hazelcast.common.manager.OasisHazelcastMapManager;
import io.github.oasisframework.hazelcast.three.helper.connection.HazelcastConnection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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
	@Override
	public <K, V> void addValueToMap(String mapName, K key, V value,
									 long ttl, TimeUnit ttlUnit) {
		hazelcastInstance.getMap(mapName).set(key, value, ttl, ttlUnit);
	}

	/**
	 * Adds a value to the specified map with a given TTL and max idle time.
	 * <p>
	 * This operation is not supported in Hazelcast 3 as max idle time configuration is unavailable.
	 * To use this functionality, consider upgrading to Hazelcast 5 or later.
	 * </p>
	 *
	 * @throws UnsupportedOperationException if max idle time is specified
	 */
	@Override
	public <K, V> void addValueToMap(String mapName, K key, V value,
									 long ttl, TimeUnit ttlUnit,
									 long maxIdle, TimeUnit maxIdleUnit) {
		throw new UnsupportedOperationException(
				"Hazelcast 3 does not support max idle time configuration for map entries. " +
						"Upgrade to Hazelcast 5 or later to enable this feature."
		);
	}

}
