package hr.ogcs.hotel.dataassistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final ChatClient chatClient;

    public ChatService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public ChatAnswer ask(String message) {
        ToolUsageRecorder.reset();
        try {
            String reply = chatClient.prompt()
                    // TODO move System message to applicaton.yml or env var
                    .system("You are a helpful data explorer that supports in extracting the relevant data from MCP services.")
                    .user(message)
                    .call()
                    .content();
            return new ChatAnswer(reply, ToolUsageRecorder.drain());
        } finally {
            ToolUsageRecorder.reset();
        }
    }

    public record ChatAnswer(String reply, List<String> usedTools) {
    }
}
