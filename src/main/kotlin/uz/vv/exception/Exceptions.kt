package uz.vv.exception

class DataNotFoundException(msg: String) : RuntimeException(msg)
class LanguageNotFoundException(msg: String) : RuntimeException(msg)
class UserNotFoundException(msg: String) : RuntimeException(msg)
class UserAlreadyExistException(msg: String) : RuntimeException(msg)