package uz.vv.base

import org.springframework.transaction.annotation.Transactional
import uz.vv.exception.DataNotFoundException

@Transactional
abstract class BaseServiceImpl<
        E : BaseEntity,
        C,
        U,
        R : BaseDTO
        >(
    protected val repository: BaseRepo<E>
) : BaseService<C, U, R> {

    protected abstract val mapper: BaseMapper<E, R>

    protected abstract fun toEntity(dto: C): E
    protected abstract fun updateEntity(dto: U, entity: E)
    protected abstract fun getEntityName(): String

    protected fun validateCreate(dto: C) {}
    protected fun validateUpdate(id: Long, dto: U) {}

    override fun create(dto: C): R {
        validateCreate(dto)
        val entity = toEntity(dto)
        val saved = repository.saveAndRefresh(entity) // saveAndRefresh
        return mapper.toDTO(saved)
    }

    override fun update(id: Long, dto: U): R {
        validateUpdate(id, dto)
        val entity = getEntityById(id)
        updateEntity(dto, entity)
        val updated = repository.saveAndRefresh(entity) // saveAndRefresh
        return mapper.toDTO(updated)
    }

    override fun getById(id: Long): R =
        mapper.toDTO(getEntityById(id))

    override fun getAll(): List<R> =
        mapper.toDTOList(repository.findAllNotDeleted()) // not deleted

    override fun delete(id: Long) {
        if (!repository.trash(id)) { // softly delete
            throw DataNotFoundException("${getEntityName()} with id=$id not found")
        }
    }

    protected fun getEntityById(id: Long): E =
        repository.findByIdAndDeletedFalse(id) // not deleted
            ?: throw DataNotFoundException("${getEntityName()} with id=$id not found")
}
