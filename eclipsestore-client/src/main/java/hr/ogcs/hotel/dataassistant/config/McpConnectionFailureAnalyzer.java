package hr.ogcs.hotel.dataassistant.config;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.diagnostics.FailureAnalysis;
import org.springframework.boot.diagnostics.FailureAnalyzer;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

import java.util.Map;

/**
 * Turns a failed MCP client initialization (e.g. the MCP server is not running) into a short
 * startup failure message instead of a long nested stacktrace.
 */
public class McpConnectionFailureAnalyzer implements FailureAnalyzer {

    private static final String MCP_INIT_FAILURE = "Client failed to initialize";
    private static final String CONNECTIONS_PROPERTY = "spring.ai.mcp.client.streamable-http.connections";

    private static final Bindable<Map<String, Map<String, String>>> CONNECTIONS = Bindable.of(
            ResolvableType.forClassWithGenerics(Map.class,
                    ResolvableType.forClass(String.class),
                    ResolvableType.forClassWithGenerics(Map.class, String.class, String.class)));

    private final Environment environment;

    public McpConnectionFailureAnalyzer(Environment environment) {
        this.environment = environment;
    }

    @Override
    public FailureAnalysis analyze(Throwable failure) {
        Throwable initFailure = findMcpInitFailure(failure);
        if (initFailure == null) {
            return null;
        }
        return new FailureAnalysis(
                "Could not connect to MCP server(s) " + configuredServers() + " (" + rootCauseMessage(initFailure) + ").",
                "Start the MCP server or check '" + CONNECTIONS_PROPERTY + "' in application.yml.",
                failure);
    }

    private String configuredServers() {
        return Binder.get(environment)
                .bind(CONNECTIONS_PROPERTY, CONNECTIONS)
                .map(connections -> {
                    StringBuilder servers = new StringBuilder();
                    connections.forEach((name, connection) -> servers
                            .append(servers.isEmpty() ? "" : ", ")
                            .append(name).append(" -> ").append(connection.get("url")));
                    return servers.toString();
                })
                .orElse("<none configured>");
    }

    private static Throwable findMcpInitFailure(Throwable failure) {
        for (Throwable t = failure; t != null; t = t.getCause()) {
            if (t.getMessage() != null && t.getMessage().startsWith(MCP_INIT_FAILURE)) {
                return t;
            }
        }
        return null;
    }

    private static String rootCauseMessage(Throwable failure) {
        Throwable root = failure;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getMessage() != null ? root.getMessage() : root.getClass().getSimpleName();
    }
}
