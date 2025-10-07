import com.app.driftchat.data.UserDataDao
import com.app.driftchat.domainmodel.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserDataRepository @Inject constructor(
    private val userDataDao: UserDataDao
) {

    /**
     * Get a user by their ID as a Flow.
     * The Flow will automatically emit new values when the data changes.
     */
    fun getUserById(id: Int): Flow<UserData?> {
        return userDataDao.getUserDataById(id)
    }

    /**
     * Insert or update a user. This is a one-shot operation.
     */
    suspend fun insertUser(user: UserData) {
        userDataDao.insertUserData(user)
    }
}