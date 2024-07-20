package com.ieumsae.chat.controller;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.repository.ChatEntranceLogRepository;
import com.ieumsae.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ChatController {

    private final ChatService chatService;
    private final ChatEntranceLogRepository chatEntranceLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);


    @Autowired
    public ChatController(ChatService chatService, ChatEntranceLogRepository chatEntranceLogRepository) {
        this.chatService = chatService;
        this.chatEntranceLogRepository = chatEntranceLogRepository;
    }

    // 채팅 페이지 연결
    @GetMapping("/chat")
    public String chatPage() {
        return "chat";  // chat.html을 렌더링


    }

    // 채팅방 연결
    @PostMapping("/enterChat")
    public String enterChat(@RequestParam(value = "chatIdx", required = false) Integer chatIdx,
                            @RequestParam(value = "userIdx", required = false) Integer userIdx,
                            Model model) {

        // 파라미터 유효성 검사 및 로깅
        if (chatIdx == null || userIdx == null) {
            logger.error("Invalid parameters: chatIdx={}, userIdx={}", chatIdx, userIdx);
            return "error"; // 에러 페이지로 리다이렉트
        }

        logger.info("Entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx);

        try {
            if (!chatEntranceLogRepository.existsByChatIdxAndUserIdx(chatIdx, userIdx)) {
                // 최초 접속 시
                logger.info("First-time access for chatIdx={}, userIdx={}", chatIdx, userIdx);
                model.addAttribute("chatIdx", chatIdx);
                model.addAttribute("userIdx", userIdx);
                model.addAttribute("chatType", "PERSONAL");
            } else {
                // 재접속 시
                logger.info("Re-entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx);
                List<Chat> previousMessages = chatService.getPreviousMessages(chatIdx, userIdx);
                model.addAttribute("previousMessages", previousMessages);
            }

            // 공통 속성 설정
            model.addAttribute("chatIdx", chatIdx);
            model.addAttribute("userIdx", userIdx);
            model.addAttribute("chatType", "PERSONAL");

            return "chatRoom";  // chatRoom.html로 이동
        } catch (Exception e) {
            logger.error("Error while entering chat: chatIdx={}, userIdx={}", chatIdx, userIdx, e);
            return "error"; // 에러 페이지로 리다이렉트
        }
    }

    // 개인 채팅 메시지 전송
    @MessageMapping("/chat.sendMessage/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public Chat sendMessage(@DestinationVariable Integer chatIdx, @Payload Chat chatMessage) {
        if (chatIdx == null || chatMessage.getChatIdx() == null) {
            throw new IllegalArgumentException("chatIdx cannot be null");
        }
        chatMessage.setChatIdx(chatIdx);
        return chatService.saveAndFormatChatMessage(chatMessage);
    }

    // 그룹 채팅 메시지 전송
    @MessageMapping("/groupChat.sendMessage/{groupChatIdx}")
    @SendTo("/topic/groupChat/{groupChatIdx}")
    public GroupChat sendGroupMessage(@DestinationVariable Integer groupChatIdx, @Payload GroupChat groupChatMessage) {
        return chatService.saveAndFormatGroupChatMessage(groupChatMessage);
    }

    // 개인 채팅방 입장
    @MessageMapping("/chat.addUser/{chatIdx}")
    @SendTo("/topic/chat/{chatIdx}")
    public Chat addUser(@DestinationVariable Integer chatIdx, @Payload Chat chatMessage) {
        return chatService.addUserToChat(chatMessage, chatIdx);
    }

    // 그룹 채팅방 입장
    @MessageMapping("/groupChat.addUser/{groupChatIdx}")
    @SendTo("/topic/groupChat/{groupChatIdx}")
    public GroupChat addUserToGroupChat(@DestinationVariable Integer groupChatIdx, @Payload GroupChat groupChatMessage) {
        return chatService.addUserToGroupChat(groupChatMessage, groupChatIdx);
    }
}