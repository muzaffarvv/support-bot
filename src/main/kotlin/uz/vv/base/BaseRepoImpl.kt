package uz.vv.base

import jakarta.persistence.EntityManager
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.transaction.annotation.Transactional

class BaseRepoImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>,
    entityManager: EntityManager
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager),
    BaseRepo<T> {

    private val em: EntityManager = entityManager

    @Transactional
    override fun saveAndRefresh(entity: T): T {
        val saved = save(entity)
        em.flush()
        em.refresh(saved)
        return saved
    }

    @Transactional
    override fun trash(id: Long): Boolean {
        val entity = findById(id).orElse(null) ?: return false
        entity.deleted = true
        save(entity)
        return true
    }

    override fun findByIdAndDeletedFalse(id: Long): T? {
        return em.createQuery(
            "select e from ${domainClass.simpleName} e where e.id = :id and e.deleted = false",
            domainClass
        )
            .setParameter("id", id)
            .resultList
            .firstOrNull()
    }

    override fun findAllNotDeleted(): List<T> {
        return em.createQuery(
            "select e from ${domainClass.simpleName} e where e.deleted = false",
            domainClass
        ).resultList
    }
}
