package hr.ogcs.hotel.dataassistant;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks which MCP tools were invoked while answering the current chat request, together
 * with their raw JSON input/output, so the chat UI can show which MCP tools were used -
 * and visualize the data they returned - to answer a given question.
 * Relies on the request being handled synchronously on a single thread, as done
 * by {@link ChatService#ask(String)}.
 */
final class ToolUsageRecorder {

    private static final ThreadLocal<List<ToolCall>> TOOL_CALLS = ThreadLocal.withInitial(ArrayList::new);

    private ToolUsageRecorder() {
    }

    static void record(String toolName, String input, String output) {
        TOOL_CALLS.get().add(new ToolCall(toolName, input, output));
    }

    static void reset() {
        TOOL_CALLS.get().clear();
    }

    static List<ToolCall> drain() {
        List<ToolCall> calls = List.copyOf(TOOL_CALLS.get());
        TOOL_CALLS.get().clear();
        return calls;
    }
}
