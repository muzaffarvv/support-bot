package uz.vv.enum

enum class UserState {
    OFFLINE,                 // Hech narsa qilmayapti
    SELECTING_LANGUAGE,      // Client til tanlayapti
    WAITING_FOR_SUPPORT,     // Support kutayapti
    IN_CHAT,                 // Suhbatda
    REQUESTING_PAUSE,        // Pauza so'rayapti
    REQUESTING_END           // Tugatish so'rayapti
}