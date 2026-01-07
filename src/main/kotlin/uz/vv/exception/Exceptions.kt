package uz.vv.exception

open class BaseException(
    override val message: String,
    val errorCode: String
) : RuntimeException(message)

class UserNotFoundException(message: String) : BaseException(message, "USER_NOT_FOUND")
class UserAlreadyExistException(message: String) : BaseException(message, "USER_ALREADY_EXISTS")
class UserAlreadySupportException(message: String) : BaseException(message, "USER_ALREADY_SUPPORT")
class InvalidUserRoleException(message: String) : BaseException(message, "INVALID_USER_ROLE")

class ChatNotFoundException(message: String) : BaseException(message, "CHAT_NOT_FOUND")
class ChatAlreadyActiveException(message: String) : BaseException(message, "CHAT_ALREADY_ACTIVE")
class ChatNotActiveException(message: String) : BaseException(message, "CHAT_NOT_ACTIVE")
class ChatNotPausedException(message: String) : BaseException(message, "CHAT_NOT_PAUSED")
class NoAvailableSupportException(message: String) : BaseException(message, "NO_AVAILABLE_SUPPORT")

class LanguageNotFoundException(message: String) : BaseException(message, "LANGUAGE_NOT_FOUND")
class LanguageMismatchException(message: String) : BaseException(message, "LANGUAGE_MISMATCH")

class MessageNotFoundException(message: String) : BaseException(message, "MESSAGE_NOT_FOUND")
class InvalidMessageTypeException(message: String) : BaseException(message, "INVALID_MESSAGE_TYPE")

class FileNotFoundException(message: String) : BaseException(message, "FILE_NOT_FOUND")
class FileUploadException(message: String) : BaseException(message, "FILE_UPLOAD_FAILED")
class FileSizeLimitException(message: String) : BaseException(message, "FILE_SIZE_LIMIT_EXCEEDED")

class DataNotFoundException(message: String) : BaseException(message, "DATA_NOT_FOUND")

class UnauthorizedException(message: String) : BaseException(message, "UNAUTHORIZED")
class ForbiddenException(message: String) : BaseException(message, "FORBIDDEN")
class InvalidTokenException(message: String) : BaseException(message, "INVALID_TOKEN")
class TokenExpiredException(message: String) : BaseException(message, "TOKEN_EXPIRED")