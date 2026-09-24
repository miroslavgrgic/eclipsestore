package hr.ogcs.eclipsestoreclient;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/api/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        ChatService.ChatAnswer answer = chatService.ask(request.message());
        return new ChatResponse(answer.reply(), answer.usedTools());
    }

    public record ChatRequest(String message) {
    }

    public record ChatResponse(String reply, List<String> usedTools) {
    }
}
