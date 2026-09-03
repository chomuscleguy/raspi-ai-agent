package com.chomu.raspiaiagent.service;

import com.chomu.raspiaiagent.entity.UserInterest;
import com.chomu.raspiaiagent.repository.UserInterestRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class InterestExtractionService {

    private final ChatClient chatClient;
    private final UserInterestRepository userInterestRepository;

    public InterestExtractionService(ChatModel chatModel, UserInterestRepository userInterestRepository) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.userInterestRepository = userInterestRepository;
    }

    /**
     * 사용자 메시지에서 관심사로 볼 만한 키워드를 추출해 저장한다.
     * 관심사가 없다고 판단되면 "NONE"을 반환하도록 프롬프트로 유도한다.
     */
    public void extractAndSaveInterest(String userMessage) {
        String prompt = """
                다음 사용자 메시지에서 취미, 관심사, 좋아하는 것으로 볼 수 있는 핵심 키워드를
                하나만 추출해줘. 없으면 "NONE"이라고만 답해.
                키워드 외의 다른 설명은 절대 붙이지 마.
                
                메시지: %s
                """.formatted(userMessage);

        String extracted = chatClient.prompt()
                .user(prompt)
                .call()
                .content()
                .trim();

        if (extracted.isBlank() || extracted.equalsIgnoreCase("NONE")) {
            return;
        }

        // 이미 저장된 키워드면 중복 저장하지 않음
        if (userInterestRepository.findByKeywordIgnoreCase(extracted).isEmpty()) {
            userInterestRepository.save(new UserInterest(extracted));
        }
    }
}