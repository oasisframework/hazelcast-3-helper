package io.github.oasisframework.hazelcast.three.helper.connection;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleEvent;
import io.github.oasisframework.hazelcast.common.properties.HazelcastConnectionAbstractProperties;
import io.github.oasisframework.hazelcast.three.helper.properties.HazelcastStatusProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class HazelcastConnection {

    public static final String HAZELCAST_INSTANCE = "hazelcastInstance";
    private static final String MONITORING_LEVEL_KEY = "hazelcast.health.monitoring.level";
    private static final String MONITORING_LEVEL_VALUE = "NOISY";
    private static final String INVOCATION_TIMEOUT_KEY = "hazelcast.client.invocation.timeout.seconds";
    private static final String INVOCATION_TIMEOUT_VALUE = "1";

    @Autowired
    private HazelcastStatusProperties hazelcastStatusProperties;
    @Autowired
    private HazelcastConnectionAbstractProperties hazelcastConnectionProperties;

    @Bean(HAZELCAST_INSTANCE)
    public HazelcastInstance hazelcastClient() {
        HazelcastInstance hz = HazelcastClient.newHazelcastClient(createClientConfig());
        hz.getLifecycleService().addLifecycleListener(this::getHealthCheckListener);
        return hz;
    }

    private ClientConfig createClientConfig() {
        ClientConfig config = new ClientConfig();

        if (StringUtils.isBlank(hazelcastConnectionProperties.getConnectionName()) || StringUtils.isBlank(
                hazelcastConnectionProperties.getConnectionPassword()) || StringUtils.isBlank(
                hazelcastConnectionProperties.getAddress())) {
            return config;
        }

        config.setNetworkConfig(createClientNetworkConfig());
      //  config.getSerializationConfig().addSerializerConfig(createProtocolBufferSerializer());

        config.setProperty(MONITORING_LEVEL_KEY, MONITORING_LEVEL_VALUE);
        config.setProperty(INVOCATION_TIMEOUT_KEY, INVOCATION_TIMEOUT_VALUE);
        config.getGroupConfig().setName(hazelcastConnectionProperties.getConnectionName());
        config.getGroupConfig().setPassword(hazelcastConnectionProperties.getConnectionPassword());

        return config;
    }

    private ClientNetworkConfig createClientNetworkConfig() {
        ClientNetworkConfig networkConfig = new ClientNetworkConfig();

        networkConfig.setAddresses(hazelcastConnectionProperties.getAddressList());
        networkConfig.setSmartRouting(true);
        networkConfig.setRedoOperation(true);
        networkConfig.setConnectionAttemptLimit(0);

        return networkConfig;
    }
/*
    private SerializerConfig createProtocolBufferSerializer() {
        return new SerializerConfig().setImplementation(new HzProtobufferSerializer()).setTypeClass(GeneratedMessageV3.class);
    }*/

    private void getHealthCheckListener(LifecycleEvent event) {
        if (hazelcastStatusProperties.containsErrorStatus(event)) {
            log.error("HAZELCAST CONNECTION REFUSED >> {}", event.getState());
        } else {
            log.info("HAZELCAST CONNECTION SUCCESSFUL >> {}", event.getState());
        }
    }
}
