package com.example.ruralize;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ruralize.models.Message;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private String chatId;
    private String buyerName;
    private String currentUserId;
    
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    
    private EditText editMessage;
    private ImageButton buttonSend;
    
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatId = getIntent().getStringExtra("chatId");
        buyerName = getIntent().getStringExtra("buyerName");
        
        if (chatId == null) {
            Toast.makeText(this, "Erro: Chat ID não encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupRecyclerView();
        setupChatBox();
        listenForMessages();
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(buyerName != null ? buyerName : "Chat");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_chat);
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, currentUserId);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setupChatBox() {
        editMessage = findViewById(R.id.edit_chat_message);
        buttonSend = findViewById(R.id.button_chat_send);

        buttonSend.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String text = editMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        // Limpa o campo imediatamente para melhor UX
        editMessage.setText("");

        Message message = new Message(
                text,
                currentUserId,
                Timestamp.now(),
                buyerName,
                "Vendedor"
        );

        db.collection("chats").document(chatId).collection("messages")
                .add(message)
                .addOnFailureListener(e -> {
                    Toast.makeText(ChatActivity.this, "Erro ao enviar mensagem", Toast.LENGTH_SHORT).show();
                    // Opcionalmente repovoar o campo se falhar
                    editMessage.setText(text);
                });
    }

    private void listenForMessages() {
        db.collection("chats").document(chatId).collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        if (value != null) {
                            for (DocumentChange dc : value.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    Message message = dc.getDocument().toObject(Message.class);
                                    messageList.add(message);
                                    adapter.notifyItemInserted(messageList.size() - 1);
                                    recyclerView.scrollToPosition(messageList.size() - 1);
                                }
                            }
                        }
                    }
                });
    }

    private static class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_SENT = 1;
        private static final int VIEW_TYPE_RECEIVED = 2;

        private final List<Message> messages;
        private final String currentUserId;

        public MessageAdapter(List<Message> messages, String currentUserId) {
            this.messages = messages;
            this.currentUserId = currentUserId;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messages.get(position);
            if (message.getSenderId().equals(currentUserId)) {
                return VIEW_TYPE_SENT;
            } else {
                return VIEW_TYPE_RECEIVED;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_SENT) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
                return new SentMessageViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
                return new ReceivedMessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Message message = messages.get(position);
            if (holder instanceof SentMessageViewHolder) {
                ((SentMessageViewHolder) holder).bind(message);
            } else {
                ((ReceivedMessageViewHolder) holder).bind(message);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class SentMessageViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;

            SentMessageViewHolder(View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.text_message_body);
            }

            void bind(Message message) {
                messageText.setText(message.getText());
            }
        }

        static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;

            ReceivedMessageViewHolder(View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.text_message_body);
            }

            void bind(Message message) {
                messageText.setText(message.getText());
            }
        }
    }
}
