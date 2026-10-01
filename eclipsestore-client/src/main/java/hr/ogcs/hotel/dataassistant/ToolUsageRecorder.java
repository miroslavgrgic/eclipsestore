package hr.ogcs.hotel.dataassistant;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks which MCP tools were invoked while answering the current chat request.
 * Relies on the request being handled synchronously on a single thread, as done
 * by {@link ChatService#ask(String)}.
 */
final class ToolUsageRecorder {

    private static final ThreadLocal<List<String>> USED_TOOLS = ThreadLocal.withInitial(ArrayList::new);

    private ToolUsageRecorder() {
    }

    static void record(String toolName) {
        USED_TOOLS.get().add(toolName);
    }

    static void reset() {
        USED_TOOLS.get().clear();
    }

    static List<String> drain() {
        List<String> used = List.copyOf(USED_TOOLS.get());
        USED_TOOLS.get().clear();
        return used;
    }
}
