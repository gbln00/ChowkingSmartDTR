package com.chowking.smartdtr.utils.biometric;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.concurrent.Executor;

public class BiometricHelper {

    public interface BiometricCallback {
        void onAuthenticationSuccess();
        void onAuthenticationError(String error);
    }

    public static void showBiometricPrompt(
            @NonNull Fragment fragment,
            @NonNull String title,
            @NonNull String subtitle,
            @NonNull BiometricCallback callback) {

        BiometricManager biometricManager = BiometricManager.from(fragment.requireContext());
        int canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            callback.onAuthenticationError("Biometric authentication is not available or not set up.");
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(fragment.requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(fragment, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        callback.onAuthenticationError(errString.toString());
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onAuthenticationSuccess();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // This is called when a fingerprint is recognized but not registered
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}