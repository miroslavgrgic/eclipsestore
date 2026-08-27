package hr.ogcs.eclipsestore.hotel.config;

import hr.ogcs.eclipsestore.hotel.domain.booking.api.mcp.BookingMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider bookingToolCallbackProvider(BookingMcpTools bookingMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(bookingMcpTools)
                .build();
    }
}
