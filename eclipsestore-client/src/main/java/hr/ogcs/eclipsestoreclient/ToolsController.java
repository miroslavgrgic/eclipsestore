package hr.ogcs.eclipsestoreclient;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestController
public class ToolsController {

    private final SyncMcpToolCallbackProvider provider;

    public ToolsController(SyncMcpToolCallbackProvider provider) {
        this.provider = provider;
    }

    @GetMapping("/api/tools")
    public List<ToolInfo> listTools() {
        return Arrays.stream(provider.getToolCallbacks())
                .map(ToolCallback::getToolDefinition)
                .map(definition -> new ToolInfo(definition.name(), definition.description(), definition.inputSchema()))
                .sorted(Comparator.comparing(ToolInfo::name))
                .toList();
    }

    public record ToolInfo(String name, String description, String inputSchema) {
    }
}
