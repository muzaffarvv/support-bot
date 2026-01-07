package uz.vv.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import uz.vv.dto.*
import uz.vv.service.BotService
import uz.vv.service.ChatService
import uz.vv.service.MessageService
import uz.vv.service.UserService
import jakarta.validation.Valid

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val userService: UserService,
    private val chatService: ChatService,
    private val messageService: MessageService,
    private val botService: BotService
) {

    // ==================== USER ENDPOINTS ====================

    @PostMapping("/users")
    fun createUser(@Valid @RequestBody dto: UserCreateDTO): ResponseEntity<UserResponseDTO> {
        val created = userService.create(dto)
        return ResponseEntity.ok(created)
    }

    @GetMapping("/users")
    fun getAllUsers(): ResponseEntity<List<UserResponseDTO>> {
        val users = userService.getAll()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/users/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserResponseDTO> {
        val user = userService.getById(id)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/users/{id}")
    fun updateUser(
        @PathVariable id: Long,
        @Valid @RequestBody dto: UserUpdateDTO
    ): ResponseEntity<UserResponseDTO> {
        val updated = userService.update(id, dto)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/users/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        userService.delete(id)
        return ResponseEntity.ok(mapOf("message" to "User deleted successfully"))
    }

    @PostMapping("/users/{id}/request-support")
    fun requestSupportRole(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        val user = userService.getById(id)

        sendSupportRequest(user.telegramId)

        return ResponseEntity.ok(mapOf(
            "message" to "Support request sent to user ${user.firstName}",
            "telegramId" to user.telegramId.toString()
        ))
    }

    private fun sendSupportRequest(telegramId: Long) {
        val message = SendMessage()
        message.chatId = telegramId.toString()
        message.text = """
            🎯 Sizni support xodimi sifatida tayinlash taklif qilinmoqda.
            
            Support sifatida siz mijozlar bilan suhbatlashishingiz va ularning savollariga javob berishingiz kerak bo'ladi.
            
            Qabul qilasizmi?
        """.trimIndent()

        val markup = InlineKeyboardMarkup()
        val rows = listOf(
            listOf(
                InlineKeyboardButton("✅ Ha, qabul qilaman").apply { callbackData = "accept_support" },
                InlineKeyboardButton("❌ Yo'q, rad etaman").apply { callbackData = "reject_support" }
            )
        )
        markup.keyboard = rows
        message.replyMarkup = markup

        try {
            botService.execute(message)
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Failed to send support request: ${e.message}")
        }
    }

    // ==================== CHAT ENDPOINTS ====================

    @GetMapping("/chats")
    fun getAllChats(): ResponseEntity<List<ChatResponseDTO>> {
        val chats = chatService.getAll()
        return ResponseEntity.ok(chats)
    }

    @GetMapping("/chats/{id}")
    fun getChatById(@PathVariable id: Long): ResponseEntity<ChatResponseDTO> {
        val chat = chatService.getById(id)
        return ResponseEntity.ok(chat)
    }

    @PutMapping("/chats/{id}")
    fun updateChat(
        @PathVariable id: Long,
        @Valid @RequestBody dto: ChatUpdateDTO
    ): ResponseEntity<ChatResponseDTO> {
        val updated = chatService.update(id, dto)
        return ResponseEntity.ok(updated)
    }

    @GetMapping("/chats/pending")
    fun getPendingChats(): ResponseEntity<List<ChatResponseDTO>> {
        val pending = chatService.getPendingChats()
        return ResponseEntity.ok(pending)
    }

    @PostMapping("/chats/{chatId}/assign/{supportId}")
    fun assignSupportToChat(
        @PathVariable chatId: Long,
        @PathVariable supportId: Long
    ): ResponseEntity<ChatResponseDTO> {
        val updated = chatService.assignSupport(chatId, supportId)
        return ResponseEntity.ok(updated)
    }

    @PostMapping("/chats/{id}/close")
    fun closeChat(@PathVariable id: Long): ResponseEntity<ChatResponseDTO> {
        val closed = chatService.closeChat(id)
        return ResponseEntity.ok(closed)
    }

    // ==================== MESSAGE ENDPOINTS ====================

    @GetMapping("/messages")
    fun getAllMessages(): ResponseEntity<List<MessageResponseDTO>> {
        val messages = messageService.getAll()
        return ResponseEntity.ok(messages)
    }

    @GetMapping("/messages/{id}")
    fun getMessageById(@PathVariable id: Long): ResponseEntity<MessageResponseDTO> {
        val message = messageService.getById(id)
        return ResponseEntity.ok(message)
    }

    @GetMapping("/chats/{chatId}/messages")
    fun getMessagesByChatId(@PathVariable chatId: Long): ResponseEntity<List<MessageResponseDTO>> {
        val messages = messageService.getByChatId(chatId)
        return ResponseEntity.ok(messages)
    }

    @DeleteMapping("/messages/{id}")
    fun deleteMessage(@PathVariable id: Long): ResponseEntity<Map<String, String>> {
        messageService.delete(id)
        return ResponseEntity.ok(mapOf("message" to "Message deleted successfully"))
    }

    // ==================== STATISTICS ENDPOINTS ====================

    @GetMapping("/stats")
    fun getStatistics(): ResponseEntity<Map<String, Any>> {
        val users = userService.getAll()
        val chats = chatService.getAll()
        val messages = messageService.getAll()

        val stats = mapOf(
            "totalUsers" to users.size,
            "totalChats" to chats.size,
            "totalMessages" to messages.size,
            "pendingChats" to chats.count { it.status.name == "PENDING" },
            "activeChats" to chats.count { it.status.name == "ACTIVE" },
            "closedChats" to chats.count { it.status.name == "CLOSED" }
        )

        return ResponseEntity.ok(stats)
    }

    @GetMapping("/stats/detailed")
    fun getDetailedStatistics(): ResponseEntity<Map<String, Any>> {
        val users = userService.getAll()
        val chats = chatService.getAll()
        val messages = messageService.getAll()

        val clientsCount = users.count { user ->
            user.roles.any { it.code == "CLIENT" }
        }
        val supportsCount = users.count { user ->
            user.roles.any { it.code == "SUPPORT" }
        }

        val stats = mapOf(
            "users" to mapOf(
                "total" to users.size,
                "clients" to clientsCount,
                "supports" to supportsCount
            ),
            "chats" to mapOf(
                "total" to chats.size,
                "pending" to chats.count { it.status.name == "PENDING" },
                "active" to chats.count { it.status.name == "ACTIVE" },
                "paused" to chats.count { it.status.name == "PAUSED" },
                "closed" to chats.count { it.status.name == "CLOSED" }
            ),
            "messages" to mapOf(
                "total" to messages.size,
                "text" to messages.count { it.type.name == "TEXT" },
                "photo" to messages.count { it.type.name == "PHOTO" },
                "video" to messages.count { it.type.name == "VIDEO" },
                "document" to messages.count { it.type.name == "DOCUMENT" },
                "sticker" to messages.count { it.type.name == "STICKER" }
            )
        )

        return ResponseEntity.ok(stats)
    }

    // ==================== LANGUAGE ENDPOINTS ====================

    @GetMapping("/languages")
    fun getAllLanguages(): ResponseEntity<List<*>> {
        val languages = userService.getAllLangs()
        return ResponseEntity.ok(languages)
    }
}