package hr.ogcs.eclipsestore.hotel.config;

import hr.ogcs.eclipsestore.hotel.domain.HotelMcpTools;
import hr.ogcs.eclipsestore.hotel.domain.booking.api.mcp.BookingMcpTools;
import hr.ogcs.eclipsestore.hotel.domain.guest.api.mcp.GuestMcpTools;
import hr.ogcs.eclipsestore.hotel.domain.payment.api.mcp.PaymentMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registering the MCP Tools. Mandatory for every new MCP interface.
 */
@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider bookingToolCallbackProvider(HotelMcpTools hotelMcpTools,
                                                            BookingMcpTools bookingMcpTools,
                                                            PaymentMcpTools paymentMcpTools,
                                                            GuestMcpTools guestMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(hotelMcpTools, bookingMcpTools, paymentMcpTools, guestMcpTools)
                .build();
    }
}
