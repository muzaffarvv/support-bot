package uz.vv.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.vv.base.BaseMapper
import uz.vv.base.BaseRepo
import uz.vv.base.BaseServiceImpl
import uz.vv.dto.MessageCreateDTO
import uz.vv.dto.MessageResponseDTO
import uz.vv.dto.MessageUpdateDTO
import uz.vv.entity.Message
import uz.vv.exception.DataNotFoundException
import uz.vv.mapper.MessageMapper
import uz.vv.repo.ChatRepo
import uz.vv.repo.MessageRepo

interface MessageService {
    fun create(dto: MessageCreateDTO): MessageResponseDTO
    fun update(id: Long, dto: MessageUpdateDTO): MessageResponseDTO
    fun getById(id: Long): MessageResponseDTO
    fun getAll(): List<MessageResponseDTO>
    fun delete(id: Long)
    fun getByChatId(chatId: Long): List<MessageResponseDTO>
}

@Service
class MessageServiceImpl(
    repository: BaseRepo<Message>,
    override val mapper: BaseMapper<Message, MessageResponseDTO>,
    private val messageMapper: MessageMapper,
    private val messageRepo: MessageRepo,
    private val userService: UserService,
    private val chatRepo: ChatRepo
) : BaseServiceImpl<
        Message,
        MessageCreateDTO,
        MessageUpdateDTO,
        MessageResponseDTO
        >(repository), MessageService {

    @Transactional
    override fun create(dto: MessageCreateDTO): MessageResponseDTO {
        validateCreate(dto)
        val entity = toEntity(dto)
        val saved = repository.saveAndRefresh(entity)
        return mapper.toDTO(saved)
    }

    override fun toEntity(dto: MessageCreateDTO): Message {
        val sender = userService.getEntityById(dto.senderId)
        val chat = chatRepo.findByIdAndDeletedFalse(dto.chatId)
            ?: throw DataNotFoundException("Chat with id=${dto.chatId} not found")

        return messageMapper.toEntity(dto, sender, chat)
    }

    override fun updateEntity(dto: MessageUpdateDTO, entity: Message) {
        entity.content = dto.content ?: entity.content
    }

    override fun validateCreate(dto: MessageCreateDTO) {
        // Sender mavjudligini tekshirish
        userService.getEntityById(dto.senderId)

        // Chat mavjudligini tekshirish
        chatRepo.findByIdAndDeletedFalse(dto.chatId)
            ?: throw DataNotFoundException("Chat with id=${dto.chatId} not found")
    }

    @Transactional(readOnly = true)
    override fun getByChatId(chatId: Long): List<MessageResponseDTO> {
        val messages = messageRepo.findByChatIdAndDeletedFalse(chatId)
        return mapper.toDTOList(messages)
    }

    override fun getEntityName(): String = "Message"
}