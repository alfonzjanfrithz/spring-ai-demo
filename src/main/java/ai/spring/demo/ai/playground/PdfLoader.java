//package ai.spring.demo.ai.playground;
//
//import jakarta.annotation.PostConstruct;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.openai.OpenAiEmbeddingModel;
//import org.springframework.ai.reader.ExtractedTextFormatter;
//import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
//import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
//import org.springframework.ai.transformer.splitter.TokenTextSplitter;
//import org.springframework.ai.vectorstore.SimpleVectorStore;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//public class PdfLoader {
//    //SimpleVectorStore does not support metadata filtering.
//    private final SimpleVectorStore vectorStore;
//
//    @Value("classpath:CRISTIANO_RONALDO_CV.pdf")
//    private Resource pdfResource;
//
//    public PdfLoader(OpenAiEmbeddingModel embeddingClient) {
//        this.vectorStore = new SimpleVectorStore(embeddingClient);
//    }
//
//    @PostConstruct
//    public void init() {
//        var config = PdfDocumentReaderConfig.builder()
//                .withPageExtractedTextFormatter(
//                        new ExtractedTextFormatter.Builder()
//                                .build())
//                .build();
//
//        //Document Reader
//        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfResource, config);
//        //Document Transformer
//        List<Document> textSplitter = new TokenTextSplitter().apply(pdfReader.get());
//        //Document Writer
//        vectorStore.add(textSplitter);
//    }
//
//    public List<Document> vectorStoreSimilaritySearch(String question) {
//        return vectorStore.similaritySearch(question);
//    }
//}