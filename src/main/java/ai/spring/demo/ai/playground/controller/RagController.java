package ai.spring.demo.ai.playground.controller;

import ai.spring.demo.ai.playground.PdfLoader;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class RagController {
    private final PdfLoader pdfLoader;
    private final OpenAiChatModel chatClient;
    private final OpenAiEmbeddingModel embeddingClient;

    public RagController(
            PdfLoader pdfLoader,
            OpenAiChatModel chatClient,
            OpenAiEmbeddingModel embeddingClient) {
        this.pdfLoader = pdfLoader;
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

    private static final String systemTemplate = """
            You are helpful assistant, what i want you to do is to use the following information to answer \s
            the question. Do not use any other information, if you dont know, simply answer: I dont know the answer.
            
            {information}
            """;

    public static final String systemTemplate2 = """
            Return the result in html such that it will be nice to render in the web.
            When the answer includes a list of items, display the answers in bullet points using <ul>, <ol>, or <li>.
            """;

    @GetMapping("/rag")
    public ResponseEntity<String> rag(@RequestParam(required = false) String input) {
        if (input == null) return ResponseEntity.ok("Please ask a question about Cristiano Ronaldo.");

        //prepare messages
        List<Document> information = pdfLoader.vectorStoreSimilaritySearch(input);

        Map<String, Object> model = Map.of("information", information);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemTemplate);

        //create prompt
        Prompt prompt = new Prompt(Arrays.asList(
                new UserMessage(input),
                new SystemMessage(systemPromptTemplate.render(model)),
                new SystemMessage(systemTemplate2)
        ));

        //client call
        return ResponseEntity.ok(chatClient.call(prompt).getResult().getOutput().getContent());
    }
    //localhost:8080/rag?input=What has Ronaldo achieved in his career?
    //localhost:8080/rag?input=What is Ronaldo's phone number?
}