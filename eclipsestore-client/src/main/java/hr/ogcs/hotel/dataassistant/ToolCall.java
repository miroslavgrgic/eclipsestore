package hr.ogcs.hotel.dataassistant;

import org.jspecify.annotations.Nullable;

/**
 * A single MCP tool invocation made while answering a chat request, including the raw
 * JSON input sent to the tool and the raw JSON result it returned. Exposed to the UI so
 * it can render the entities and relationships contained in the payload (e.g. as a graph).
 * {@code durationMs} is the time the MCP server reported for answering the call, or
 * {@code null} if the server didn't report it.
 */
public record ToolCall(String name, String input, String output, @Nullable Long durationMs) {
}
