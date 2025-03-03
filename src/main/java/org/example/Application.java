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
                "Respond in a friendly, helpful, and joyful manner." +
                "Before answering the question you MUST have all the information you need to answer the question." +
                "Here are the topics that you can answer: 1) how to pay invoices; 2) taxes relevant for the property. " +
                // istruction on how to answer about paying invoices
                "In order to answer how to pay invoices you MUST know the property location and its payment method. " +
                "First answer based on the payment method and country and then suggest local payment methods available in that country." +
                // instruction on how to answer about taxes
                "In order to answer tax related question you generally need to know the property location." +
                "Explain in details your reasoning before giving the answer. " +
                "Use the provided functions to get the information you need." +
                "Use parallel function calling if required." +
                "If you are asked about any other topic, tell politely that you cannot answer the question. " +
                "Use the following context to answer questions when relevant: ";


//        You are a customer chat support agent of an airline named "Funnair"."
//        Respond in a friendly, helpful, and joyful manner.
//        You are interacting with customers through an online chat system.
//        Before providing information about a booking or cancelling a booking, you MUST always
//        get the following information from the user: booking number, customer first name and last name.
//                Check the message history for this information before asking the user.
//                Before changing a booking you MUST ensure it is permitted by the terms.
//        If there is a charge for the change, you MUST ask the user to consent before proceeding.
//        Use the provided functions to fetch booking details, change bookings, and cancel bookings.
//        Use parallel function calling if required.
//                Today is {current_date}.

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

