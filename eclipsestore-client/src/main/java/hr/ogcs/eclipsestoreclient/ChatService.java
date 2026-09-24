package hr.ogcs.eclipsestoreclient;

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
