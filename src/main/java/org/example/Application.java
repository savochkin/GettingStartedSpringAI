package org.example;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final BookingTools bookingTools;
    private PromptChatMemoryAdvisor memoryAdvisor;
    private QuestionAnswerAdvisor qaAdvisor;


    public Application(ChatClient chatClient, VectorStore vectorStore, BookingTools bookingTools, ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.bookingTools = bookingTools;
        this.memoryAdvisor = new PromptChatMemoryAdvisor(chatMemory);
        this.qaAdvisor = new QuestionAnswerAdvisor(vectorStore);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void run() {
        startCliChat();
    }

    private void startCliChat() {
        Scanner scanner = new Scanner(System.in);
        String systemMessage = "You are a booking.com AI assistant for partners. " +
                "You help partner answer question about their financial related topics." +
                "Before answering the question make sure you have all the information you need to answer the question." +
                "In order to answer how to pay invoices you generally need to know the property location, its payment method, " +
                "In order to answer tax related question you generally need to know the property location." +
                "Use the following context to answer questions when relevant: ";

        System.out.println("Welcome to the Booking.com Partner Assistant! Type 'exit' to quit.");

        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

//            // First, search for relevant documents
//            List<Document> relevantDocs = vectorStore.similaritySearch(userInput);
//            String context = relevantDocs.stream()
//                    .map(Document::getText)
//                    .reduce("", (a, b) -> a + "\n" + b);
//
//            logger.info("Relevant docs found: " + relevantDocs.size());
//            relevantDocs.forEach(doc -> {
//                        logger.info("Doc size: " + doc.getText().length());
//                        logger.info("\n----\nDoc preview: " + doc.getText().substring(0, Math.min(doc.getText().length(), 200)) + "...");
//                    }
//            );
            // Create prompt with context
            Prompt prompt = new Prompt(List.of(
                //new SystemMessage(systemMessage + "\nContext: " + context),
                new SystemMessage(systemMessage),
                new UserMessage(userInput)
            ));

            ChatResponse response = chatClient
                .prompt(prompt)
                .tools(bookingTools)
                .advisors(memoryAdvisor, qaAdvisor)
                .call()
                .chatResponse();

            System.out.println("Assistant: " + response.getResult().getOutput().getText());
        }

        scanner.close();
    }

    @Bean
    CommandLineRunner ingestDocuments(
            EmbeddingModel embeddingModel, VectorStore vectorStore,
            @Value("classpath:phub/*") Resource[] docs) {

        return args -> {
            for (Resource doc : docs) {
                vectorStore.write(
                        new TokenTextSplitter().transform(
                                new TextReader(doc).read()));

                logger.info("Ingested document: {}", doc.getFilename());
            }
        };
    }}

