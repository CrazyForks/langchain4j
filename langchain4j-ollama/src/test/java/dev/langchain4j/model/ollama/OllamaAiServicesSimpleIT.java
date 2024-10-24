package dev.langchain4j.model.ollama;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServicesSimpleIT;

import java.util.List;

import static dev.langchain4j.model.ollama.AbstractOllamaLanguageModelInfrastructure.LOCAL_OLLAMA_IMAGE;
import static dev.langchain4j.model.ollama.OllamaImage.OLLAMA_IMAGE;
import static dev.langchain4j.model.ollama.OllamaImage.TINY_DOLPHIN_MODEL;
import static dev.langchain4j.model.ollama.OllamaImage.resolve;

class OllamaAiServicesSimpleIT extends AiServicesSimpleIT {

    static LC4jOllamaContainer ollama = new LC4jOllamaContainer(resolve(OLLAMA_IMAGE, LOCAL_OLLAMA_IMAGE))
            .withModel(TINY_DOLPHIN_MODEL);

    static {
        ollama.start();
        ollama.commitToImage(LOCAL_OLLAMA_IMAGE);
    }

    @Override
    protected List<ChatLanguageModel> models() {
        return List.of(
                OllamaChatModel.builder()
                        .baseUrl(ollama.getEndpoint())
                        .modelName(TINY_DOLPHIN_MODEL)
                        .build(),
                OpenAiChatModel.builder()
                        .apiKey("does not matter") // TODO make apiKey optional when using custom baseUrl?
                        .baseUrl(ollama.getEndpoint() + "/v1") // TODO add "/v1" by default?
                        .modelName(TINY_DOLPHIN_MODEL)
                        .build()
        );
    }

    @Override
    protected boolean assertFinishReason() {
        return false; // TODO why?
    }
}
