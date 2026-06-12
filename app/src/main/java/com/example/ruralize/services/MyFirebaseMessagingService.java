package com.example.ruralize.services;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.ruralize.network.RetrofitClient;
import com.example.ruralize.network.services.AuthService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_RURALIZE";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        enviarTokenParaServidor(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Lógica para exibir notificação local se necessário
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Mensagem recebida: " + remoteMessage.getNotification().getBody());
        }
    }

    private void enviarTokenParaServidor(String token) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("uid", user.getUid());
        data.put("fcmToken", token);

        AuthService authService = RetrofitClient.getClient().create(AuthService.class);
        authService.updateFcmToken(data).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Token FCM atualizado no servidor");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao atualizar token FCM", t);
            }
        });
    }
}
