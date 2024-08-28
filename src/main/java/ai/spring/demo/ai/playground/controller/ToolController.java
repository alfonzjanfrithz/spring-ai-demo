package ai.spring.demo.ai.playground.controller;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

@RestController
public class ToolController {

    private final OpenAiAudioTranscriptionModel transcriptionClient;
    public final OpenAiImageModel imageClient;

    public ToolController(
            OpenAiAudioTranscriptionModel transcriptionClient,
            OpenAiImageModel imageClient) {
        this.imageClient = imageClient;
        this.transcriptionClient = transcriptionClient;
    }

    @GetMapping(value = "/image")
    public ResponseEntity<InputStreamResource> image(@RequestParam(required = false) String input) throws Exception {
        //client call (inline prompt)
        ImageResponse response = imageClient.call(
                new ImagePrompt(input,
                        OpenAiImageOptions.builder()
                                .withQuality("hd")
                                .withN(1)
                                .withHeight(1024)
                                .withWidth(1024).build())
        );
        //Parse Response
        URL url = new URI(response.getResult().getOutput().getUrl()).toURL();
        InputStream in = url.openStream();

        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(new InputStreamResource(in));
    }
    //localhost:8080/image?input=A duck
    //localhost:8080/image?input=Nasi Lemak

    @Value("classpath:coffee or tea.m4a")
    private Resource coffeeOrTea;
    @Value("classpath:selamat pagi.m4a")
    private Resource selamatPagi;
    private final OpenAiAudioApi.TranscriptResponseFormat format = OpenAiAudioApi.TranscriptResponseFormat.TEXT;

    @GetMapping("/transcription")
    public String transcription(@RequestParam String input) {
        //create prompt
        Resource audioResource = input.contains("malay") ? selamatPagi : coffeeOrTea;

        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .withLanguage(input.equals("malay") ? "ms" : "en") // ISO-639-1
                .withResponseFormat(format).build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, options);

        //client call
        return transcriptionClient.call(prompt).getResult().getOutput();
    }
    //localhost:8080/transcription?input=malay
    //localhost:8080/transcription?input=english

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @GetMapping("/speech")
    public ResponseEntity<?> speech() throws Exception {
        //Manual: not calling a client
        //Initialize API
        OpenAiAudioApi api = new OpenAiAudioApi(apiKey);
        //Create Request
        OpenAiAudioApi.SpeechRequest speechRequest = new OpenAiAudioApi.SpeechRequest(
                OpenAiAudioApi.TtsModel.TTS_1_HD.value,
                "Hello and welcome to our Spring AI showcase.",
                OpenAiAudioApi.SpeechRequest.Voice.ALLOY,
                OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3, 1.0f);

        //API call
        return api.createSpeech(speechRequest);
    }
    //localhost:8080/speech

    /*@Value("classpath:chinese.m4a")
    private Resource chinese;

    @GetMapping("/translation")
    public ResponseEntity<?> translation() throws Exception {
        OpenAiAudioApi api = new OpenAiAudioApi(apiKey);
        byte[] audio = chinese.getContentAsByteArray();
        OpenAiAudioApi.TranslationRequest request = new OpenAiAudioApi.TranslationRequest(
                audio, "whisper-1", "translate to english", format, 0.7f);

        return api.createTranslation(request);
    }*/
}