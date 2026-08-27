package hr.ogcs.eclipsestoreclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class ChatConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ChatConfiguration.class);

    @Bean
    public ChatClient chatClient(ChatModel chatModel, SyncMcpToolCallbackProvider provider) {
        return ChatClient
                .builder(chatModel).defaultTools(provider)
                .build();
    }

    @Bean
    public ApplicationRunner logDiscoveredMcpTools(SyncMcpToolCallbackProvider provider) {
        return args -> {
            ToolCallback[] toolCallbacks = provider.getToolCallbacks();
            log.info("Discovered {} MCP tool(s) from connected servers: {}",
                    toolCallbacks.length,
                    Arrays.stream(toolCallbacks)
                            .map(toolCallback -> toolCallback.getToolDefinition().name())
                            .toList());
        };
    }

}
