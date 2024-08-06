open class UserActivityMapper(
    open val id: Long,
    open val userId: Long,
    open val transitionType: Int,
    open val timestamp: String
)

data class UserWalkActivityMapper(
    override val id: Long,
    override val userId: Long,
    override val transitionType: Int,
    override val timestamp: String,
    val stepNumber: Long
) : UserActivityMapper(id, userId, transitionType, timestamp)

data class UserVehicleActivityMapper(
    override val id: Long,
    override val userId: Long,
    override val transitionType: Int,
    override val timestamp: String,
    val distanceTravelled: Double
) : UserActivityMapper(id, userId, transitionType, timestamp)

data class UserStillActivityMapper(
    override val id: Long,
    override val userId: Long,
    override val transitionType: Int,
    override val timestamp: String
) : UserActivityMapper(id, userId, transitionType, timestamp)
