package ai.spring.demo.ai.playground.controller;



import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.parser.MapOutputParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class CityController {
    private final OpenAiChatModel client;

    public CityController(OpenAiChatModel client) {
        this.client = client;
    }

    @GetMapping("/simplePrompt")
    public String getCity() {
        //client call (inline prompt)
        return client.call("Give me the name of a random city in Malaysia.");
    }
    //localhost:8080/simplePrompt


    private final OpenAiChatOptions options = new OpenAiChatOptions.Builder().
            withModel(OpenAiApi.ChatModel.GPT_3_5_TURBO.value).build();

    @GetMapping("/messageTypes")
    public ResponseEntity<String> messageTypes() {
        //prepare messages
        List<Message> messages = Arrays.asList(
                new SystemMessage("Put result in html format so it will be nice to render in the web."),
                new SystemMessage("""
                        Reply in Malaysian main languages, Malay, Chinese, and English.
                        Put each language in its own text area that is as wide as the screen\s
                         and as tall as 3 lines, with varying background colour.
                        """),
                new UserMessage("Tell me about one interesting place in Kuala Lumpur.")
        );
        //create prompt
        Prompt prompt = new Prompt(messages, options);
        //client call
        return ResponseEntity.ok(client.call(prompt).getResult().getOutput().getContent());
    }
    //localhost:8080/messageTypes

    public static final String userTemplate = """
            Give me information of city called {city}, and tell me the following details:
            - Name of the city
            - Name of the country
            - Religion (in percentage)
            - Number of population
            - GDP per capita in USD
            - Calling Code
            - Driving Side
            - The weather in that location
            - Their official language.
            - How to write "Hello!", "Thank You!", "How Much?" and "Its too expensive!".\s
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

        UserMessage userMessage = ((UserMessage) userPromptTemplate.createMessage());
        SystemMessage systemMessage = new SystemMessage(systemPromptTemplate.render());

        //create prompt
        Prompt prompt = new Prompt(Arrays.asList(userMessage, systemMessage));

        //client call
        return ResponseEntity.ok(client.call(prompt).getResult().getOutput().getContent());
    }
    //localhost:8080/promptTemplate?input=Kuala Lumpur

    @GetMapping("/outputParser")
    public Map<String, Object> returnJson(@RequestParam(required = false) String input) {
        //prepare messages
        //MapOutputParser parser = new MapOutputParser();
        MapOutputConverter parser = new MapOutputConverter();
        PromptTemplate template = new PromptTemplate(userTemplate, getTemplateModel(input, parser.getFormat()));

        //create prompt
        Prompt prompt = template.create();

        //client call
        return parser.parse(client.call(prompt).getResult().getOutput().getContent());
    }
    //localhost:8080/outputParser?input=London

    private Map<String, Object> getTemplateModel(String name, String format) {
        name = name == null ? "Tokyo" : name;
        return Map.of(
                "city", name,
                "format", format);
    }
}