package uz.vv.base

interface BaseMapper<E: BaseEntity, C, R: BaseDTO> {

    fun toEntity(dto: C): E

    fun toDTO(entity: E): R

    fun toDTOList(entities: List<E>): List<R> = entities.map { toDTO(it) }

    fun mapBaseFields(entity: E, dto: R) {
        dto.id = entity.id
        dto.createdAt = entity.getCreatedAtUTCString()
        dto.updatedAt = entity.getUpdatedAtUTCString()
    }
}