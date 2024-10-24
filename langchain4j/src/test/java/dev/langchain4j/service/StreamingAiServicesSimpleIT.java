package dev.langchain4j.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static dev.langchain4j.model.output.FinishReason.STOP;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class StreamingAiServicesSimpleIT {

    protected abstract List<StreamingChatLanguageModel> models();

    interface Assistant {

        TokenStream chat(String userMessage);
    }

    @Test
    void should_answer_simple_question() throws Exception {

        for (StreamingChatLanguageModel model : models()) {

            // given
            Assistant assistant = AiServices.create(Assistant.class, model);

            StringBuilder answerBuilder = new StringBuilder();
            CompletableFuture<String> futureAnswer = new CompletableFuture<>();
            CompletableFuture<Response<AiMessage>> futureResponse = new CompletableFuture<>();

            assistant.chat("What is the capital of Germany?")
                    .onNext(answerBuilder::append)
                    .onComplete(response -> {
                        futureAnswer.complete(answerBuilder.toString());
                        futureResponse.complete(response);
                    })
                    .onError(futureAnswer::completeExceptionally)
                    .start();

            String answer = futureAnswer.get(30, SECONDS);
            Response<AiMessage> response = futureResponse.get(30, SECONDS);

            assertThat(answer).containsIgnoringCase("Berlin");
            assertThat(response.content().text()).isEqualTo(answer);

            if (assertTokenUsage()) {
                TokenUsage tokenUsage = response.tokenUsage();
                assertThat(tokenUsage.inputTokenCount()).isGreaterThan(0);
                assertThat(tokenUsage.outputTokenCount()).isGreaterThan(0);
                assertThat(tokenUsage.totalTokenCount())
                        .isEqualTo(tokenUsage.inputTokenCount() + tokenUsage.outputTokenCount());
            }

            if (assertFinishReason()) {
                assertThat(response.finishReason()).isEqualTo(STOP);
            }
        }
    }

    protected boolean assertTokenUsage() {
        return true;
    }

    protected boolean assertFinishReason() {
        return true;
    }

    // TODO test token usage is summed for tools?
}
