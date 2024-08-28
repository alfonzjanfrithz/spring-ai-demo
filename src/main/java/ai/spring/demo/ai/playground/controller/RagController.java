package ai.spring.demo.ai.playground.controller;

import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class RagController {
    private final OpenAiChatModel chatClient;
    private final OpenAiEmbeddingModel embeddingClient;

    public RagController(
            OpenAiChatModel chatClient,
            OpenAiEmbeddingModel embeddingClient) {
        this.chatClient = chatClient;
        this.embeddingClient = embeddingClient;
    }

    @GetMapping("/embedding")
    public Map getEmbedding() {
        EmbeddingResponse embeddingResponse = embeddingClient.call(
                new EmbeddingRequest(List.of("Hello World"),
                        OpenAiEmbeddingOptions.builder()
                                .withModel("text-embedding-ada-002")
                                .build()));

        return Map.of("embedding", embeddingResponse.getResult().getOutput());
    }
    //localhost:8080/embedding
}