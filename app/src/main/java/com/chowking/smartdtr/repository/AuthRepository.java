package com.chowking.smartdtr.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.database.dao.UserDao;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.utils.HashUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthRepository {

    private final UserDao userDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;

    public AuthRepository(Context context) {
        userDao  = AppDatabase.getInstance(context).userDao();
        firestore = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<User> login(String employeeId, String plainPassword) {
        MutableLiveData<User> result = new MutableLiveData<>();
        executor.execute(() -> {
            User user = userDao.getUserByEmployeeId(employeeId);
            if (user != null && HashUtils.verifyPassword(plainPassword, user.passwordHash)) {
                // Success: Update cloud snapshot to ensure it's synced
                syncUserToCloud(user);
                result.postValue(user);
            } else {
                result.postValue(null);
            }
        });
        return result;
    }

    private void syncUserToCloud(User user) {
        firestore.collection("users")
                .document(user.employeeId)
                .set(user, SetOptions.merge());
    }
    /** Login via Google — matches googleId or email stored on the User record */
    public LiveData<User> loginWithGoogle(String googleId, String email) {
        MutableLiveData<User> result = new MutableLiveData<>();
        executor.execute(() -> {
            // 1. Try matching by unique Google Sub ID
            User user = userDao.getUserByGoogleId(googleId);

            // 2. If no match by ID, try matching by verified Email
            if (user == null && email != null) {
                user = userDao.getUserByEmail(email);
                // Auto-link the googleId for future logins if email matched
                if (user != null) {
                    userDao.linkGoogleAccount(user.id, googleId);
                }
            }

            result.postValue(user); // null = not linked and email not found
        });
        return result;
    }

    /** Link a Google account to an existing user (call after password login) */
    public void linkGoogleToUser(int userId, String googleId) {
        executor.execute(() ->
                userDao.linkGoogleAccount(userId, googleId)
        );
    }
}