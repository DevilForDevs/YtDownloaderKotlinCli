package demuxers

class TrakFilter(
    val handler: HandlerType? = null,
    handlers: Set<HandlerType>? = null
) {
    val handlers: Set<HandlerType>? =
        if (handler != null) null else handlers
}

