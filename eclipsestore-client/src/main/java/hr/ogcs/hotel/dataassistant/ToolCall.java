package hr.ogcs.hotel.dataassistant;

/**
 * A single MCP tool invocation made while answering a chat request, including the raw
 * JSON input sent to the tool and the raw JSON result it returned. Exposed to the UI so
 * it can render the entities and relationships contained in the payload (e.g. as a graph).
 */
public record ToolCall(String name, String input, String output) {
}
