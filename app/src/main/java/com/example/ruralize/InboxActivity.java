package com.example.ruralize;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ruralize.models.ChatSession;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends AppCompatActivity {

    private RecyclerView rvInbox;
    private TextView txtEmptyInbox;
    private MaterialToolbar toolbarInbox;
    private ChatSessionAdapter adapter;
    private List<ChatSession> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        rvInbox = findViewById(R.id.rvInbox);
        txtEmptyInbox = findViewById(R.id.txtEmptyInbox);
        toolbarInbox = findViewById(R.id.toolbarInbox);

        toolbarInbox.setNavigationOnClickListener(v -> finish());

        rvInbox.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();
        adapter = new ChatSessionAdapter(chatList, chatSession -> {
            Intent intent = new Intent(InboxActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chatSession.getId());
            intent.putExtra("buyerName", chatSession.getBuyerName());
            startActivity(intent);
        });
        rvInbox.setAdapter(adapter);

        loadChats();
    }

    private void loadChats() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String uid = auth.getCurrentUser().getUid();
        
        FirebaseFirestore.getInstance().collection("chats")
                .whereEqualTo("empresaId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(InboxActivity.this, "Erro ao carregar mensagens", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    chatList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            ChatSession session = doc.toObject(ChatSession.class);
                            if (session.getId() == null) {
                                session.setId(doc.getId());
                            }
                            chatList.add(session);
                        }
                    }
                    
                    adapter.notifyDataSetChanged();
                    
                    if (chatList.isEmpty()) {
                        txtEmptyInbox.setVisibility(View.VISIBLE);
                        rvInbox.setVisibility(View.GONE);
                    } else {
                        txtEmptyInbox.setVisibility(View.GONE);
                        rvInbox.setVisibility(View.VISIBLE);
                    }
                });
    }

    private static class ChatSessionAdapter extends RecyclerView.Adapter<ChatSessionAdapter.ChatViewHolder> {

        private final List<ChatSession> chatList;
        private final OnItemClickListener listener;

        public interface OnItemClickListener {
            void onItemClick(ChatSession chatSession);
        }

        public ChatSessionAdapter(List<ChatSession> chatList, OnItemClickListener listener) {
            this.chatList = chatList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_session, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatSession session = chatList.get(position);
            holder.bind(session, listener);
        }

        @Override
        public int getItemCount() {
            return chatList.size();
        }

        static class ChatViewHolder extends RecyclerView.ViewHolder {
            private final TextView txtBuyerName;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                txtBuyerName = itemView.findViewById(R.id.txtBuyerName);
            }

            public void bind(ChatSession session, OnItemClickListener listener) {
                txtBuyerName.setText(session.getBuyerName() != null ? session.getBuyerName() : "Desconhecido");
                itemView.setOnClickListener(v -> listener.onItemClick(session));
            }
        }
    }
}
