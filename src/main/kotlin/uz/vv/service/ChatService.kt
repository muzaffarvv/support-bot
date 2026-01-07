package uz.vv.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.vv.dto.ChatResponseDTO
import uz.vv.dto.ChatUpdateDTO
import uz.vv.entity.Chat
import uz.vv.enum.ChatStatus
import uz.vv.exception.*
import uz.vv.mapper.ChatMapper
import uz.vv.repo.ChatRepo
import uz.vv.repo.LanguageRepo
import java.time.Instant

interface ChatService {
    fun create(clientId: Long): ChatResponseDTO
    fun getById(id: Long): ChatResponseDTO
    fun getAll(): List<ChatResponseDTO>
    fun update(id: Long, dto: ChatUpdateDTO): ChatResponseDTO
    fun assignSupport(chatId: Long, supportId: Long): ChatResponseDTO
    fun pauseChat(chatId: Long): ChatResponseDTO
    fun closeChat(chatId: Long): ChatResponseDTO
    fun getPendingChats(): List<ChatResponseDTO>
    fun getActiveChatsBySupport(supportId: Long): List<Chat>
    fun getActiveChatsByUser(userId: Long): List<Chat>
    fun findMatchingSupport(languageId: Long): Long?
    fun getEntityById(id: Long): Chat?
    fun resumeChat(chatId: Long): ChatResponseDTO
    fun getPausedChatsByUser(userId: Long): List<Chat>
    fun getActiveOrPausedChatsByUser(userId: Long): List<Chat>
}

@Service
class ChatServiceImpl(
    private val chatMapper: ChatMapper,
    private val chatRepo: ChatRepo,
    private val userService: UserService,
    private val languageRepo: LanguageRepo
) : ChatService {

    @Transactional
    override fun create(clientId: Long): ChatResponseDTO {
        val client = userService.getEntityById(clientId)

        val language = client.languages.firstOrNull()
            ?: throw LanguageNotFoundException("Client uchun til tanlanmagan")

        val chat = Chat(
            client = client,
            support = null,
            status = ChatStatus.PENDING,
            language = language
        )

        val saved = chatRepo.saveAndRefresh(chat)
        return chatMapper.toDTO(saved)
    }

    @Transactional(readOnly = true)
    override fun getById(id: Long): ChatResponseDTO {
        val chat = chatRepo.findByIdAndDeletedFalse(id)
            ?: throw ChatNotFoundException("Chat ID: $id topilmadi")
        return chatMapper.toDTO(chat)
    }

    @Transactional(readOnly = true)
    override fun getAll(): List<ChatResponseDTO> {
        return chatMapper.toDTOList(chatRepo.findAllNotDeleted())
    }

    @Transactional
    override fun update(id: Long, dto: ChatUpdateDTO): ChatResponseDTO {
        val chat = chatRepo.findByIdAndDeletedFalse(id)
            ?: throw ChatNotFoundException("Chat ID: $id topilmadi")

        dto.status?.let { chat.status = it }
        dto.supportId?.let {
            val support = userService.getEntityById(it)
            chat.support = support
        }

        val updated = chatRepo.saveAndRefresh(chat)
        return chatMapper.toDTO(updated)
    }

    @Transactional
    override fun assignSupport(chatId: Long, supportId: Long): ChatResponseDTO {
        val chat = chatRepo.findByIdAndDeletedFalse(chatId)
            ?: throw ChatNotFoundException("Chat ID: $chatId topilmadi")

        val support = userService.getEntityById(supportId)

        // Chat tilini supportning barcha tillari bilan tekshirish
        val supportSpeaksChatLanguage = support.languages.any { it.code == chat.language.code }

        if (!supportSpeaksChatLanguage) {
            throw LanguageMismatchException(
                "Support tili client tili bilan mos kelmadi. " +
                        "Client tili: ${chat.language.name}, " +
                        "Support tillari: ${support.languages.joinToString { it.name }}"
            )
        }

        chat.support = support
        chat.status = ChatStatus.ACTIVE

        val updated = chatRepo.saveAndRefresh(chat)
        return chatMapper.toDTO(updated)
    }


    @Transactional
    override fun pauseChat(chatId: Long): ChatResponseDTO {
        val chat = chatRepo.findByIdAndDeletedFalse(chatId)
            ?: throw ChatNotFoundException("Chat ID: $chatId topilmadi")

        if (chat.status != ChatStatus.ACTIVE) {
            throw ChatNotActiveException("Chat faol holatda emas")
        }

        chat.status = ChatStatus.PAUSED

        val updated = chatRepo.saveAndRefresh(chat)
        return chatMapper.toDTO(updated)
    }

    @Transactional
    override fun closeChat(chatId: Long): ChatResponseDTO {
        val chat = chatRepo.findByIdAndDeletedFalse(chatId)
            ?: throw ChatNotFoundException("Chat ID: $chatId topilmadi")

        chat.status = ChatStatus.CLOSED
        chat.closedAt = Instant.now()

        val updated = chatRepo.saveAndRefresh(chat)
        return chatMapper.toDTO(updated)
    }

    @Transactional(readOnly = true)
    override fun getPendingChats(): List<ChatResponseDTO> {
        val chats = chatRepo.findByStatus(ChatStatus.PENDING)
        return chatMapper.toDTOList(chats)
    }

    @Transactional(readOnly = true)
    override fun getActiveChatsBySupport(supportId: Long): List<Chat> {
        return chatRepo.findAllNotDeleted()
            .filter { it.support?.id == supportId && it.status == ChatStatus.ACTIVE }
    }

    @Transactional(readOnly = true)
    override fun getActiveChatsByUser(userId: Long): List<Chat> {
        return chatRepo.findAllNotDeleted()
            .filter {
                (it.client.id == userId || it.support?.id == userId) &&
                        (it.status == ChatStatus.ACTIVE || it.status == ChatStatus.PENDING)
            }
    }

    override fun getEntityById(id: Long): Chat? {
        return chatRepo.findByIdAndDeletedFalse(id)
    }

    @Transactional(readOnly = true)
    override fun findMatchingSupport(languageId: Long): Long? {
        val pendingChats = chatRepo.findByStatus(ChatStatus.PENDING)
            .filter { it.language.id == languageId }
            .sortedBy { it.createdAt }

        return pendingChats.firstOrNull()?.id
    }

    @Transactional
    override fun resumeChat(chatId: Long): ChatResponseDTO {
        val chat = chatRepo.findByIdAndDeletedFalse(chatId)
            ?: throw ChatNotFoundException("Chat ID: $chatId topilmadi")

        if (chat.status != ChatStatus.PAUSED) {
            throw ChatNotPausedException("Chat to'xtatilgan holatda emas")
        }

        chat.status = ChatStatus.ACTIVE

        val updated = chatRepo.saveAndRefresh(chat)
        return chatMapper.toDTO(updated)
    }

    @Transactional(readOnly = true)
    override fun getPausedChatsByUser(userId: Long): List<Chat> {
        return chatRepo.findAllNotDeleted()
            .filter {
                (it.client.id == userId || it.support?.id == userId) &&
                        it.status == ChatStatus.PAUSED
            }
    }

    @Transactional(readOnly = true)
    override fun getActiveOrPausedChatsByUser(userId: Long): List<Chat> {
        return chatRepo.findAllNotDeleted()
            .filter {
                (it.client.id == userId || it.support?.id == userId) &&
                        (it.status == ChatStatus.ACTIVE || it.status == ChatStatus.PAUSED)
            }
    }
}