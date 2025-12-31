package uz.vv.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.vv.base.BaseMapper
import uz.vv.base.BaseRepo
import uz.vv.base.BaseServiceImpl
import uz.vv.dto.UserCreateDTO
import uz.vv.dto.UserResponseDTO
import uz.vv.dto.UserUpdateDTO
import uz.vv.entity.Language
import uz.vv.entity.User
import uz.vv.exception.UserNotFoundException
import uz.vv.exception.UserAlreadyExistException
import uz.vv.mapper.UserMapper
import uz.vv.repo.LanguageRepo
import uz.vv.repo.RoleRepo
import uz.vv.repo.UserRepo

@Service
class UserService(
    repository: BaseRepo<User>,
    override val mapper: BaseMapper<User, UserResponseDTO>,
    val userMapper: UserMapper,
    val langRepo: LanguageRepo,
    val userRepo: UserRepo,
    val roleRepo: RoleRepo,

    @Value("\${support.name:SUPPORT}")
    private var support: String,

    @Value("\${client.name:CLIENT}")
    private var client: String
) : BaseServiceImpl<
        User,
        UserCreateDTO,
        UserUpdateDTO,
        UserResponseDTO,
        >(repository) {

    @Transactional
    fun assignToSupport(id: Long) {
        val user = getEntityById(id)
        val supportRole = roleRepo.findByCodeAndDeletedFalse(support)
            ?: throw IllegalStateException("SUPPORT role not found") // todo IllegalStateException

        if (user.roles.any { it.id == supportRole.id }) {
            throw IllegalStateException("USER_ALREADY_SUPPORT") // todo IllegalStateException
        }

        user.roles.clear()
        user.roles.add(supportRole)

        repository.saveAndRefresh(user)
    }


    override fun create(dto: UserCreateDTO): UserResponseDTO {
        validateCreate(dto)
        val entity = toEntity(dto)
        // set role
        entity.roles.add(roleRepo.findByCodeAndDeletedFalse(client)!!)

        val saved = repository.saveAndRefresh(entity)
        return mapper.toDTO(saved)
    }

    override fun update(id: Long, dto: UserUpdateDTO): UserResponseDTO {
        validateUpdate(id, dto)
        val entity = getEntityById(id)
        updateEntity(dto, entity)
        val updated = repository.saveAndRefresh(entity)
        return mapper.toDTO(updated)
    }

    override fun toEntity(dto: UserCreateDTO): User = userMapper.toEntity(dto)

    override fun validateCreate(dto: UserCreateDTO) {
        // telegramId unique
        userRepo.findByTelegramIdAndDeletedFalse(dto.telegramId)?.let {
            throw UserAlreadyExistException("User with telegramId=${dto.telegramId} already exists")
        }
        // phone unique
        userRepo.findByPhoneNumberAndDeletedFalse(dto.phoneNumber)?.let {
            throw UserAlreadyExistException("User with phoneNumber=${dto.phoneNumber} already exists")
        }
    }

    override fun validateUpdate(id: Long, dto: UserUpdateDTO) {
        getById(id)
        dto.languageIds?.forEach { id -> getLangByIdOrThrow(id) }
    }

    override fun updateEntity(dto: UserUpdateDTO, entity: User) {
        entity.firstName = dto.firstName ?: entity.firstName
        entity.lastName = dto.lastName ?: entity.lastName
        dto.languageIds?.let { ids ->
            entity.languages.clear()
            entity.languages.addAll(ids.map { id -> getLangByIdOrThrow(id) })
        }
    }

    fun getLangByIdOrThrow(id: Long): Language =
        langRepo.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException(
            "Language with id=$id not found"
        )

    @Transactional(readOnly = true)
    fun getAllLangs(): List<Language> = langRepo.findAllByDeletedFalse()

    override fun getEntityName(): String = "User"

    @Transactional(readOnly = true)
    fun findByTelegramId(telegramId: Long): UserResponseDTO? {
        val user = userRepo.findByTelegramIdAndDeletedFalse(telegramId)
        return user?.let { mapper.toDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getEntityByTelegramId(telegramId: Long): User? {
        return userRepo.findByTelegramIdAndDeletedFalse(telegramId)
    }
}