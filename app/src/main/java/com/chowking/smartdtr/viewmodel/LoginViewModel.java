package com.chowking.smartdtr.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.repository.AuthRepository;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository repository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new AuthRepository(application);
    }

    public LiveData<User> login(String employeeId, String password) {
        return repository.login(employeeId, password);
    }

    public LiveData<User> loginWithGoogle(String googleId) {
        return repository.loginWithGoogle(googleId);
    }

    public void linkGoogle(int userId, String googleId) {
        repository.linkGoogleToUser(userId, googleId);
    }
}