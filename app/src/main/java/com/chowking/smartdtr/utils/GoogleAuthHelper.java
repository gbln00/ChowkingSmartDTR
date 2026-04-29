package com.chowking.smartdtr.utils;

import android.content.Context;
import androidx.credentials.*;
import com.google.android.libraries.identity.googleid.*;
import java.security.MessageDigest;
import java.util.UUID;

public class GoogleAuthHelper {

    // Paste your Web Client ID from Firebase console →
    // Project Settings → General → Web API Key area,
    // OR from google-services.json: client[0].oauth_client
    // where client_type = 3
    public static final String WEB_CLIENT_ID =
            "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com";

    public static GetGoogleIdOption buildSignInOption() {
        return new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // show all accounts
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build();
    }

    public static GetCredentialRequest buildRequest() {
        return new GetCredentialRequest.Builder()
                .addCredentialOption(buildSignInOption())
                .build();
    }
}