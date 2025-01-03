package io.github.oasisframework.hazelcast.three.helper.connection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import io.github.oasisframework.hazelcast.common.properties.HazelcastConnectionProperties;
import io.github.oasisframework.hazelcast.common.properties.HazelcastMapProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

@Configuration
public class HazelcastConnection {

    public static final String HAZELCAST_INSTANCE = "hazelcastInstance";
    private static final String MONITORING_LEVEL_KEY = "hazelcast.health.monitoring.level";
    private static final String MONITORING_LEVEL_VALUE = "NOISY";
    private static final String INVOCATION_TIMEOUT_KEY = "hazelcast.client.invocation.timeout.seconds";
    private static final String INVOCATION_TIMEOUT_VALUE = "1";

    private final HazelcastConnectionProperties hazelcastConnectionProperties;
    private final SerializerConfig customConfig;

    public HazelcastConnection(HazelcastConnectionProperties hazelcastConnectionProperties, SerializerConfig customConfig) {
        this.hazelcastConnectionProperties = hazelcastConnectionProperties;
        this.customConfig = customConfig;
    }

    @Bean
    @ConditionalOnMissingBean (SerializerConfig.class)
    public SerializerConfig dummySerializerConfig() {
        return new SerializerConfig();
    }

    @Bean(HAZELCAST_INSTANCE)
    public HazelcastInstance hazelCastClient() {
        HazelcastInstance instance = HazelcastClient.newHazelcastClient(createClientConfig());

        addMapsConfigs(instance.getConfig());

        return instance;
    }

    private ClientConfig createClientConfig() {
        ClientConfig config = new ClientConfig();

        if (StringUtils.isBlank(hazelcastConnectionProperties.getConnectionName()) || StringUtils.isBlank(
                hazelcastConnectionProperties.getConnectionPassword()) || StringUtils.isBlank(
                hazelcastConnectionProperties.getAddress())) {
            return config;
        }

        config.setNetworkConfig(createClientNetworkConfig());
        config.getSerializationConfig().addSerializerConfig(customConfig);

        config.setProperty(MONITORING_LEVEL_KEY, MONITORING_LEVEL_VALUE);
        config.setProperty(INVOCATION_TIMEOUT_KEY, INVOCATION_TIMEOUT_VALUE);
        config.getGroupConfig().setName(hazelcastConnectionProperties.getConnectionName());
        config.getGroupConfig().setPassword(hazelcastConnectionProperties.getConnectionPassword());
        config.setInstanceName(hazelcastConnectionProperties.getInstanceName());

        return config;
    }

    private void addMapsConfigs(Config config) {
        if (CollectionUtils.isEmpty(hazelcastConnectionProperties.getCreatableMaps())) {
            return;
        }

        hazelcastConnectionProperties.getCreatableMaps().stream().filter(Objects::nonNull).map(this::createMapConfig).filter(Objects::nonNull)
                .forEach(mapConfig -> addMapConfig(config, mapConfig));
    }

    private MapConfig createMapConfig(HazelcastMapProperty property) {
        try {
            MapConfig mapConfig = new MapConfig(property.getName());

            mapConfig.setAsyncBackupCount(property.getMaxAsyncBackupCounts());
            mapConfig.setMaxIdleSeconds(property.getMaxIdleSeconds());
            mapConfig.setTimeToLiveSeconds(property.getTimeToLiveSeconds());

            return mapConfig;
        } catch (Exception e) {
            return null;
        }
    }

    private void addMapConfig(Config config, MapConfig mapConfig) {
        try {
            config.addMapConfig(mapConfig);
        } catch (Exception ignored) {

        }
    }

    private ClientNetworkConfig createClientNetworkConfig() {
        ClientNetworkConfig networkConfig = new ClientNetworkConfig();

        networkConfig.setAddresses(hazelcastConnectionProperties.getAddressList());
        networkConfig.setSmartRouting(true);
        networkConfig.setRedoOperation(true);
        networkConfig.setConnectionAttemptLimit(0);

        return networkConfig;
    }

}
