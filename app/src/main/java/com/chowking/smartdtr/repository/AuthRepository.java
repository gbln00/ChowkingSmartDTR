package com.chowking.smartdtr.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.database.dao.UserDao;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.utils.HashUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {

    private final UserDao userDao;
    private final ExecutorService executor;

    public AuthRepository(Context context) {
        userDao  = AppDatabase.getInstance(context).userDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> login(String employeeId, String plainPassword) {
        MutableLiveData<User> result = new MutableLiveData<>();
        executor.execute(() -> {
            User user = userDao.getUserByEmployeeId(employeeId);
            if (user != null && HashUtils.verifyPassword(plainPassword, user.passwordHash)) {
                result.postValue(user);   // success
            } else {
                result.postValue(null);   // wrong ID or password
            }
        });
        return result;
    }
}