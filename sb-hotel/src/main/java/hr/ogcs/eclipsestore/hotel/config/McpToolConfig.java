package hr.ogcs.eclipsestore.hotel.config;

import hr.ogcs.eclipsestore.hotel.domain.HotelMcpTools;
import hr.ogcs.eclipsestore.hotel.domain.booking.api.mcp.BookingMcpTools;
import hr.ogcs.eclipsestore.hotel.domain.guest.api.mcp.GuestMcpTools;
import hr.ogcs.eclipsestore.hotel.domain.payment.api.mcp.PaymentMcpTools;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Registering the MCP Tools. Mandatory for every new MCP interface.
 */
@Configuration
public class McpToolConfig {

    /**
     * Key of the {@code _meta} entry on every returned content block holding the number of
     * milliseconds the server needed to answer the MCP tool call.
     */
    public static final String DURATION_MS_META_KEY = "durationMs";

    @Bean
    public ToolCallbackProvider bookingToolCallbackProvider(HotelMcpTools hotelMcpTools,
                                                            BookingMcpTools bookingMcpTools,
                                                            PaymentMcpTools paymentMcpTools,
                                                            GuestMcpTools guestMcpTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(hotelMcpTools, bookingMcpTools, paymentMcpTools, guestMcpTools)
                .build();
    }

    /**
     * Replaces Spring AI's default tool conversion (disabled via
     * {@code spring.ai.mcp.server.tool-callback-converter: false}) to add the server side
     * processing time to every tool result. It's attached to the content blocks rather than
     * the result itself, as Spring AI MCP clients only forward the content blocks.
     */
    @Bean
    public List<McpStatelessServerFeatures.SyncToolSpecification> timedToolSpecifications(ToolCallbackProvider toolCallbackProvider) {
        return Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(toolCallback -> McpToolUtils.toStatelessSyncToolSpecification(toolCallback, null))
                .map(McpToolConfig::timed)
                .toList();
    }

    private static McpStatelessServerFeatures.SyncToolSpecification timed(McpStatelessServerFeatures.SyncToolSpecification specification) {
        return McpStatelessServerFeatures.SyncToolSpecification.builder()
                .tool(specification.tool())
                .callHandler((context, request) -> {
                    long start = System.nanoTime();
                    McpSchema.CallToolResult result = specification.callHandler().apply(context, request);
                    long durationMs = (System.nanoTime() - start) / 1_000_000;
                    return withDuration(result, durationMs);
                })
                .build();
    }

    private static McpSchema.CallToolResult withDuration(McpSchema.CallToolResult result, long durationMs) {
        List<McpSchema.Content> content = result.content().stream()
                .map(block -> block instanceof McpSchema.TextContent text
                        ? (McpSchema.Content) McpSchema.TextContent.builder(text.text())
                                .annotations(text.annotations())
                                .meta(Map.of(DURATION_MS_META_KEY, durationMs))
                                .build()
                        : block)
                .toList();
        return new McpSchema.CallToolResult(content, result.isError(), result.structuredContent(), result.meta());
    }
}
