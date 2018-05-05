package com.dalong.mission.config;

import com.dalong.mission.service.BlacklistManager;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

@Component
public class BlacklistConfig {

    @Autowired
    private MissionProperties missionProperties;
    @Autowired
    private BlacklistManager blacklistManager;

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new ChannelTopic(missionProperties.getChannel()));
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter() {
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(blacklistManager);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        listenerAdapter.setSerializer(jackson2JsonRedisSerializer);
        return listenerAdapter;
    }


}
