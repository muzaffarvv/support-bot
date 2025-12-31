package uz.vv.service

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.beans.factory.annotation.Value
import uz.vv.entity.File
import uz.vv.entity.Message
import uz.vv.enum.MessageType
import uz.vv.repo.FileRepo
import uz.vv.repo.MessageRepo
import uz.vv.exception.DataNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class FileService(
    private val fileRepo: FileRepo,
    private val messageRepo: MessageRepo
) {

    @Value("\${file.upload-dir:uploads}")
    private lateinit var uploadDir: String

    /**
     * SAVE multiple files
     * Faylni diskga saqlaydi va DBga yozadi
     * KeyName: unikal nom (12 char UUID + originalName)
     */
    fun save(files: List<MultipartFile>, message: Message): List<File> {
        val savedFiles = mutableListOf<File>()
        if (files.isEmpty()) return savedFiles

        val uploadPath = Paths.get(System.getProperty("user.home"), uploadDir)
        Files.createDirectories(uploadPath) // papka yo'q bo'lsa yaratadi

        files.forEach { file ->
            if (file.isEmpty) return@forEach

            val originalName = file.originalFilename ?: return@forEach
            val shortId = UUID.randomUUID().toString().replace("-", "").take(12)
            val keyName = "${shortId}_$originalName"

            // Faylni papkaga saqlash
            val target = uploadPath.resolve(keyName)
            Files.copy(file.inputStream, target, StandardCopyOption.REPLACE_EXISTING)

            // Fayl turini aniqlash
            val fileType = when {
                file.contentType?.startsWith("image/") == true -> MessageType.PHOTO
                file.contentType?.startsWith("video/") == true -> MessageType.VIDEO
                file.contentType?.startsWith("audio/") == true -> MessageType.VOICE
                else -> MessageType.DOCUMENT
            }

            // DB entity yaratish
            val fileEntity = File(
                orgName = originalName,
                keyName = keyName,
                size = file.size,
                type = fileType,
                message = message
            )

            savedFiles.add(fileRepo.saveAndRefresh(fileEntity))
        }

        return savedFiles
    }

    /**
     * DELETE single file
     * DBdan soft-delete qiladi va diskdan o'chiradi
     */
    fun delete(keyName: String?) {
        if (keyName == null || keyName == "default.png") return

        val path = Paths.get(System.getProperty("user.home"), uploadDir, keyName)
        Files.deleteIfExists(path)

        fileRepo.findByKeyName(keyName)?.let { file ->
            file.deleted = true
            fileRepo.saveAndRefresh(file)
        }
    }

    /**
     * DELETE multiple files
     */
    fun deleteAll(files: List<File>) {
        files.forEach { delete(it.keyName) }
    }

    /**
     * FIND files by message
     * DBdan barcha fayllarni olib keladi
     */
    fun findByMessageId(msgId: Long): File? {
        return fileRepo.findByMessageId(msgId)
    }

    /**
     * UPDATE file for a message
     * Eski fayl o'chiriladi va yangi fayl qo'yiladi
     */
    fun update(msgId: Long, newFile: MultipartFile): File {
        val message = messageRepo.findByIdAndDeletedFalse(msgId)
            ?: throw DataNotFoundException("Message with id=$msgId not found")

        // Avval eski faylni olish va o'chirish
        val oldFile = fileRepo.findByMessageId(msgId)
        oldFile?.let { delete(it.keyName) }

        // Yangi faylni saqlash
        val savedFiles = save(listOf(newFile), message)
        return savedFiles.first()
    }

    /**
     * FIND by KeyName
     */
    fun findByKeyName(keyName: String): File? {
        return fileRepo.findByKeyName(keyName)
    }

    /**
     * DELETE all files by message
     */
    fun deleteAllByMessage(msgId: Long) {
        val file = fileRepo.findByMessageId(msgId)
        file?.let { delete(it.keyName) }
    }

    /**
     * Get file path for download
     */
    fun getFilePath(keyName: String): String {
        return Paths.get(System.getProperty("user.home"), uploadDir, keyName).toString()
    }


}