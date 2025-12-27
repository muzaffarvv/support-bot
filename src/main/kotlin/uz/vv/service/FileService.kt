//package uz.vv.service
//
//import org.springframework.stereotype.Service
//import org.springframework.web.multipart.MultipartFile
//import org.springframework.beans.factory.annotation.Value
//import uz.vv.entity.File
//import uz.vv.repo.FileRepo
//import java.nio.file.Files
//import java.nio.file.Paths
//import java.nio.file.StandardCopyOption
//import java.time.Instant
//import java.util.UUID
//
//@Service
//class FileStorageService(
//    private val fileRepo: FileRepo,
//    private val userService: UserService
//) {
//
//    @Value("\${file.upload-dir}")
//    private lateinit var uploadDir: String
//
//    // SAVE multiple files, Faylni diskga saqlaydi va DBga yozadi
//    // KeyName: unikal nom (Long + originalName) 12 taga qirqardi
//
//    fun save(files: List<MultipartFile>): List<File> {
//        val savedFiles = mutableListOf<File>()
//        if (files.isEmpty()) return savedFiles
//
//        val uploadPath = Paths.get(System.getProperty("user.home"), uploadDir)
//        Files.createDirectories(uploadPath) // papka yo'q bo'lsa yaratadi
//
//        files.forEach { file ->
//            if (file.isEmpty) return@forEach
//
//            val originalName = file.originalFilename ?: return@forEach
//            val shortId = UUID.randomUUID().toString().replace("-", "").take(12)
//            //                                                      kamaytirish kerak ✓
//            val keyName = "${shortId}_$originalName"
//
//            // Faylni papkaga saqlash
//            val target = uploadPath.resolve(keyName)
//            Files.copy(file.inputStream, target, StandardCopyOption.REPLACE_EXISTING)
//
//            // DB entity yaratish
//            val fileEntity = File(
//                orgName = originalName,
//                keyName = keyName,
//                size = file.size,
//                type = file.,
//                message =
//            ).apply {
//                orgName = originalName
//                this.keyName = keyName
//                size = file.size
//                createdAt = Instant.now()
//
//            }
//            savedFiles.add(fileRepo.saveAndRefresh(fileEntity)) // BaseRepo save + refresh
//        }
//
//        return savedFiles
//    }
//
//    /**
//     * DELETE single file
//     * DBdan o'chirmaydi (agar soft-delete bo'lsa, set deleted=true qilinishi mumkin)
//     * Diskdan o'chiradi
//     */
//    fun delete(keyName: String?) {
//        if (keyName == null || keyName == "default.png") return
//        val path = Paths.get(System.getProperty("user.home"), uploadDir, keyName)
//        Files.deleteIfExists(path)
//        fileRepo.findByKeyName(keyName)?.let { file ->
//            file.deleted = true
//            fileRepo.saveAndRefresh(file) // soft-delete
//        }
//    }
//
//    /**
//     * DELETE multiple files
//     */
//    fun deleteAll(files: List<File>) {
//        files.forEach { delete(it.keyName) }
//    }
//
//    /**
//     * FIND files by product
//     * DBdan barcha fayllarni olib keladi
//     */
//    fun findByMessageId(msgId: Long): List<File> {
//        return fileRepo.findByMessageId(msgId)
//    }
//
//    /**
//     * UPDATE file for a product
//     * Eski fayl o'chiriladi va yangi fayl qo'yiladi
//     */
//    fun update(msgId: Long, newFile: MultipartFile): File {
//        // Avval eski faylni olish va o'chirish
//        val oldFiles = fileRepo.findByMessageId(msgId)
//        oldFiles.forEach { delete(it.keyName) }
//
//        // Yangi faylni saqlash
//        val savedFiles = save(listOf(newFile))
//        return savedFiles.first()
//    }
//
//    /**
//     * FIND by KeyName
//     */
//    fun findByKeyName(keyName: String): File? {
//        return fileRepo.findByKeyName(keyName)
//    }
//
//    /**
//     * DELETE all files by product
//     */
//    fun deleteAllByProduct(msgId: Long) {
//        val files = fileRepo.findByMessageId(msgId)
//        deleteAll(files)
//    }
//}
