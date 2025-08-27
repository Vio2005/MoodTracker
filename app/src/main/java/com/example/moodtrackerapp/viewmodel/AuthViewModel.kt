package com.example.moodtrackerapp.viewmodel



import androidx.lifecycle.*
import com.example.moodtrackerapp.data.UserRepository
import com.example.moodtrackerapp.data.entity.UserEntity
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {

    // LiveData for registration result
    private val _registerResult = MutableLiveData<Result<UserEntity>>()
    val registerResult: LiveData<Result<UserEntity>> = _registerResult

    // LiveData for login result
    private val _loginResult = MutableLiveData<Result<UserEntity>>()
    val loginResult: LiveData<Result<UserEntity>> = _loginResult

    // Register user
    fun registerUser(email: String, password: String, username: String) {
        viewModelScope.launch {
            val newUser = UserEntity(
                email = email,
                password = password,
                username = username
            )

            // Call repository
            val result: Result<Long> = userRepository.registerUser(newUser)

            // Handle the result
            result.onSuccess { id ->
                // id is the Long value
                val registeredUser = newUser.copy(userId = id)
                _registerResult.postValue(Result.success(registeredUser))
            }.onFailure { throwable ->
                _registerResult.postValue(Result.failure(throwable))
            }
        }
    }


    // Login user
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            val result = userRepository.loginUser(email, password)
            _loginResult.postValue(result)
        }
    }

    // Optional: restore session by userId
    fun getUserById(userId: Long, callback: (UserEntity?) -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getUserById(userId)
            callback(user)
        }
    }
}

