package ai.spring.demo.ai.playground.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@RestController
public class CityController {
    private final OpenAiChatModel chatModel;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public CityController(
            OpenAiChatModel chatModel,
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
        this.chatClient = chatClientBuilder.defaultAdvisors(new PromptChatMemoryAdvisor(chatMemory)).build();
    }

    @GetMapping("/simplePrompt")
    public ResponseEntity<String> getCity() {
        //client call (inline prompt)
        return ResponseEntity.ok(chatModel.call("Give me a name of a random city in Malaysia."));
    }
    //localhost:8080/simplePrompt

    private final OpenAiChatOptions options = new OpenAiChatOptions.Builder().
            withModel(OpenAiApi.ChatModel.GPT_3_5_TURBO.value).build();

    @GetMapping("/messageTypes")
    public ResponseEntity<String> messageTypes() {
        // prepare messages
        List<Message> messages = Arrays.asList(
                new UserMessage("tell me about one interesting place in London."),
                new SystemMessage("Put the result in html format so it will be nice to render in the web."),
                new SystemMessage("""
                        Reply in Malaysian main languages, Malay, Chinese, and English.
                        Put each language in its own text area that is as wide as the screen \s
                         and as tall as 3 lines, with varying background colour.
                        """)
        );
        //create prompt
        OpenAiChatOptions options = new OpenAiChatOptions.Builder().withModel(OpenAiApi.ChatModel.GPT_3_5_TURBO.value).build();
        Prompt prompt = new Prompt(messages, options);
        //client call
        ChatResponse response = chatModel.call(prompt);
        return ResponseEntity.ok(response.getResult().getOutput().getContent());
    }//localhost:8080/MessageTypes

    public static final String userTemplate = """
            Give me information of city called {city}, and tell me the following details:
            - Name of the city
            - Name of the country
            - Religion (in percentage)
            - Number of population
            - GDP per capita in USD
            - Calling Code
            - Driving Side
            - Their official language.
            - How to write "Hello!", "Thank You!", "How Much?" and "Its too expensive!". \\s
             Include phonetic transcriptions in open and closing brackets except for english.
            
             {format}
            """;
    public static final String systemTemplate = """
            Return the result in html table such that it will be nice to render in the web.
            Design the table to have 2 columns spanning 80% of the screen width, with font size of 20.
            Use as much height for each row as necessary to fit in the table contents.
            Add padding around the text and align the text to the middle of each table cell.
            Add borders to each cell of the table, and use suitable colours for its background.
            Be sure to only display the table in html, do not include any explanations.
            """;

    @GetMapping("/promptTemplate")
    public ResponseEntity<String> withPromptTemplate(@RequestParam(required = false) String input) {
        //prepare messages
        PromptTemplate userPromptTemplate = new PromptTemplate(userTemplate, getTemplateModel(input, ""));
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemTemplate);

        UserMessage userMessage = (UserMessage) userPromptTemplate.createMessage();
        SystemMessage systemMessage = new SystemMessage(systemPromptTemplate.render());

        //create prompt
        Prompt prompt = new Prompt(Arrays.asList(userMessage, systemMessage));

        //client call
        return ResponseEntity.ok(chatModel.call(prompt).getResult().getOutput().getContent());
    }
    //localhost:8080/promptTemplate?input=Kuala Lumpur

    @GetMapping("/outputConverter")
    public Map<String, Object> returnJson(@RequestParam(required = false) String input) {
        //prepare messages
        MapOutputConverter parser = new MapOutputConverter();
        PromptTemplate template = new PromptTemplate(userTemplate, getTemplateModel(input, parser.getFormat()));

        //create prompt
        Prompt prompt = template.create();

        //client call
        return parser.parse(chatModel.call(prompt).getResult().getOutput().getContent());
    }
    //localhost:8080/outputConverter?input=London

    private Map<String, Object> getTemplateModel(String name, String format) {
        name = name == null ? "Tokyo" : name;
        return Map.of(
                "city", name,
                "format", format
        );
    }

    @GetMapping(value = "/memoryAdvisor", produces = "text/plain")
    public ResponseEntity<String> chatMemory(@RequestParam String input) {
        String chatId = "default_id";

        chatClient.prompt().user(input).advisors(advisor ->
                advisor.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId).param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
        ).call().content();

        List<Message> messages = chatMemory.get(chatId,1000);
        String appResponse = messages.stream().map( message ->
                message.getMessageType().getValue() + ": " + message.getContent()
        ).collect(Collectors.joining("\n"));
        return ResponseEntity.ok(appResponse);
    }
    // http://localhost:8080/memoryAdvisor?input=Hello

    public static final String functionUserTemplate = """
            Give me information of city called {city}, and tell me the following details:
            - Name of the city
            - Name of the country
            - Religion (in percentage)
            - Number of population
            - GDP per capita in USD
            - The temperature in that city
            - Their most spoken languages.
            """;

    public static final String functionSystemTemplate = """
            Return the result in html table such that it will be nice to render in the web.
            Design the table to have 2 columns spanning 80% of the screen width, with font size of 20.
            Add padding around the text and align the text to the middle of each table cell.
            Add borders to each cell of the table, and use suitable colours for its background.
            The temperature should be displayed in Celsius and Fahrenheit.
            Be sure to provide answers for all details requested by the user.
            Do not include any explanations.
            """;


    @GetMapping("/functionCall")
    public ResponseEntity<String> functionCall(@RequestParam(required = false) String input) {
        //prepare messages
        Map<String, Object> userModel = Map.of(
                "city", input == null ? "Tokyo" : input);

        PromptTemplate user = new PromptTemplate(functionUserTemplate, userModel);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(functionSystemTemplate);

        UserMessage userMessage = ((UserMessage) user.createMessage());
        SystemMessage systemMessage = (SystemMessage) systemPromptTemplate.createMessage();

        //create prompt
        Prompt prompt = new Prompt(Arrays.asList(userMessage, systemMessage),
                OpenAiChatOptions.builder().withFunction("getCityWeather").build());

        //client call
        return ResponseEntity.ok(chatModel.call(prompt).getResult().getOutput().getContent());
    }
    //localhost:8080/functionCall?input=Tokyo
}
