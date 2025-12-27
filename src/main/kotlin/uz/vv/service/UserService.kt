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
        user.roles.clear()
        user.roles.add(roleRepo.findByCodeAndDeletedFalse(support)!!)
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
            entity.languages = ids.map { id -> getLangByIdOrThrow(id) }.toMutableSet()
        }
    }

    fun getLangByIdOrThrow(id: Long) =
        langRepo.findByIdAndDeletedFalse(id) ?: throw UserNotFoundException(
            "Language with id=$id not found"
        )

    @Transactional(readOnly = true)
    fun getAllLangs() = langRepo.findAllByDeletedFalse()

    override fun getEntityName(): String = "User"


}