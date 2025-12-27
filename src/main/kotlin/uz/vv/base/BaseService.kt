package uz.vv.base

interface BaseService<C, U, R> {
    fun create(dto: C): R
    fun update(id: Long, dto: U): R
    fun getById(id: Long): R
    fun getAll(): List<R>
    fun delete(id: Long)
}
