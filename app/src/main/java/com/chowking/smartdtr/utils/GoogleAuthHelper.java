package com.chowking.smartdtr.utils;

import android.content.Context;
import androidx.credentials.*;
import com.google.android.libraries.identity.googleid.*;
import java.security.MessageDigest;
import java.util.UUID;

public class GoogleAuthHelper {

    // Replace this with your actual Web Client ID from Firebase
    public static final String WEB_CLIENT_ID =
            "118165577053-mghqen28dn3jnmh75i5md6j2ec6qhvhn.apps.googleusercontent.com";

    public static GetCredentialRequest buildRequest() {
        GetSignInWithGoogleOption googleIdOption = new GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
                .build();

        return new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();
    }
}