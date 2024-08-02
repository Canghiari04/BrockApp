open class UserActivity(
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
) : UserActivity(id, userId, transitionType, timestamp)

data class UserVehicleActivityMapper(
    override val id: Long,
    override val userId: Long,
    override val transitionType: Int,
    override val timestamp: String,
    val distanceTravelled: Double
) : UserActivity(id, userId, transitionType, timestamp)

data class UserStillActivityMapper(
    override val id: Long,
    override val userId: Long,
    override val transitionType: Int,
    override val timestamp: String
) : UserActivity(id, userId, transitionType, timestamp)
