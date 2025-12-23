package uz.vv.base

interface BaseMapper<E: BaseEntity, R: BaseDTO> {

    fun toDTO(entity: E): R

    fun toDTOList(entities: List<E>): List<R> =
        entities.map { toDTO(it) }
}
