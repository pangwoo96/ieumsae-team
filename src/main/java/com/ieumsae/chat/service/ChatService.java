package com.ieumsae.chat.service;

import com.ieumsae.chat.domain.Chat;
import com.ieumsae.chat.domain.ChatEntranceLog;
import com.ieumsae.chat.domain.GroupChat;
import com.ieumsae.chat.repository.ChatEntranceLogRepository;
import com.ieumsae.chat.repository.ChatRepository;
import com.ieumsae.chat.repository.GroupChatEntranceLogRepository;
import com.ieumsae.chat.repository.GroupChatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChatService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ChatRepository chatRepository;
    private final GroupChatRepository groupChatRepository;
    private final ChatEntranceLogRepository chatEntranceLogRepository;
    private final GroupChatEntranceLogRepository groupChatEntranceLogRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository, GroupChatRepository groupChatRepository,
                       ChatEntranceLogRepository chatEntranceLogRepository,
                       GroupChatEntranceLogRepository groupChatEntranceLogRepository) {
        this.chatRepository = chatRepository;
        this.groupChatRepository = groupChatRepository;
        this.chatEntranceLogRepository = chatEntranceLogRepository;
        this.groupChatEntranceLogRepository = groupChatEntranceLogRepository;
    }

    public Chat saveAndFormatChatMessage(Chat chatMessage) {
        if ("PERSONAL".equals(chatMessage.getChatType())) {
            if (chatMessage.getChatIdx() == null) {
                throw new IllegalArgumentException("chat_idx cannot be null");
            }
            String formattedTime = chatMessage.getSendDateTime().format(TIME_FORMATTER);
            String formattedContent = String.format("[개인] %s (%s) %s", chatMessage.getUserIdx(), formattedTime, chatMessage.getContent());
            chatMessage.setContent(formattedContent);
            return chatRepository.save(chatMessage);
        } else {
            throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
        }
    }

    public GroupChat saveAndFormatGroupChatMessage(GroupChat groupChatMessage) {
        throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
    }

    public Chat addUserToChat(Chat chatMessage, Integer chatIdx) {
        if ("PERSONAL".equals(chatMessage.getChatType())) {
            chatMessage.setChatIdx(chatIdx);
            chatMessage.setSendDateTime(LocalDateTime.now());
            chatMessage.setContent(chatMessage.getUserIdx() + "님이 입장하셨습니다.");

            ChatEntranceLog entranceLog = new ChatEntranceLog();
            entranceLog.setChatIdx(chatIdx);
            entranceLog.setUserIdx(chatMessage.getUserIdx());
            entranceLog.setEntranceDateTime(LocalDateTime.now());
            chatEntranceLogRepository.save(entranceLog);

            return chatRepository.save(chatMessage);
        } else {
            throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
        }
    }

    public GroupChat addUserToGroupChat(GroupChat groupChatMessage, Integer groupChatIdx) {
        throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
    }

    public List<Chat> getMessagesAfterUserJoin(Integer chatIdx, Integer userIdx) {
        ChatEntranceLog lastEntrance = chatEntranceLogRepository
                .findTopByChatIdxAndUserIdxOrderByEntranceDateTimeDesc(chatIdx, userIdx)
                .orElseThrow(() -> new RuntimeException("사용자의 입장 시간 정보를 찾을 수 없습니다."));

        return chatRepository.findByChatIdxAndSendDateTimeAfterOrderBySendDateTimeAsc(chatIdx, lastEntrance.getEntranceDateTime());
    }

    public List<GroupChat> getGroupMessagesAfterUserJoin(Integer groupChatIdx, Integer userIdx) {
        throw new UnsupportedOperationException("그룹 채팅은 현재 지원되지 않습니다.");
    }
}