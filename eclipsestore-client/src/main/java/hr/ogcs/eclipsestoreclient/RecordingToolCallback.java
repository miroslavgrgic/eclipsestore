package hr.ogcs.eclipsestoreclient;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * Wraps a {@link ToolCallback} to report every invocation to the
 * {@link ToolUsageRecorder}, so the chat UI can show which MCP tools were
 * used to answer a given question.
 */
class RecordingToolCallback implements ToolCallback {

    private final ToolCallback delegate;

    RecordingToolCallback(ToolCallback delegate) {
        this.delegate = delegate;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        ToolUsageRecorder.record(delegate.getToolDefinition().name());
        return delegate.call(toolInput);
    }

    @Override
    public String call(String toolInput, @Nullable ToolContext toolContext) {
        ToolUsageRecorder.record(delegate.getToolDefinition().name());
        return delegate.call(toolInput, toolContext);
    }
}
