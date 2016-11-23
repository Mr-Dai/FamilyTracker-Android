package com.wetrack;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.wetrack.adapter.ChatMessageAdapter;
import com.wetrack.model.Chat;
import com.wetrack.model.ChatMessage;
import com.wetrack.model.User;

import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView messageListView;
    private EditText messageEditText;
    private RelativeLayout messageEditLayout;
    private ChatMessageAdapter adapter;
    private InputMethodManager imeManager;
    private List<ChatMessage> chatMessageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send_btn) {
            String s = messageEditText.getText().toString();
            sendMessage(s);
        }
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button buttonSend = (Button) findViewById(R.id.send_btn);
        buttonSend.setOnClickListener(this);

        messageListView = (ListView) findViewById(R.id.message_list_view);
        messageEditText = (EditText) findViewById(R.id.message_edit);

        messageEditLayout = (RelativeLayout) findViewById(R.id.message_edit_layout);
        messageEditLayout.setBackgroundResource(R.drawable.input_bar_bg_normal);
        messageEditLayout.requestFocus();

        messageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    messageEditLayout.setBackgroundResource(R.drawable.input_bar_bg_active);
                else
                    messageEditLayout.setBackgroundResource(R.drawable.input_bar_bg_normal);
            }
        });

        imeManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Chat chatTest = new Chat();
        User user1 = new User();
        User user2 = new User();
        user1.setUsername("Amy");
        user1.setIconUrl("head1.png");
        user2.setUsername("Bob");
        user2.setIconUrl("head2.png");
        chatTest.setChatId("chatId1");
        List<String> usernames = new ArrayList<>();
        usernames.add(user1.getUsername());
        usernames.add(user2.getUsername());
        chatTest.setMemberNames(usernames);
        ChatMessage defaultMessage = new ChatMessage();
        defaultMessage.setChatId(chatTest.getChatId());
        defaultMessage.setFromUsername(user1.getUsername());
        defaultMessage.setContent("This is a default message sent by Amy.");
        defaultMessage.setSendTime(LocalDateTime.parse("2016-11-23"));

        chatMessageList = new ArrayList<>();
        adapter = new ChatMessageAdapter(this, chatMessageList, chatTest, user2);
        messageListView.setAdapter(adapter);
        messageListView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard();
                messageEditLayout.requestFocus();
                return false;
            }
        });
        messageListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);

        // Test on receive message
        onReceiveMessage(defaultMessage);
    }

    public void editClick(View v) {
        messageListView.setSelection(messageListView.getCount() - 1);
    }

    private void hideKeyboard(){
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null)
                imeManager.hideSoftInputFromWindow(getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void onReceiveMessage(ChatMessage message) {
        chatMessageList.add(message);
        adapter.refresh(chatMessageList);
        messageListView.setSelection(messageListView.getCount() - 1);
    }

    private void sendMessage(String message){
        ChatMessage sendMessage = new ChatMessage();
        sendMessage.setFromUsername("Bob");
        sendMessage.setSendTime(LocalDateTime.now());
        sendMessage.setContent(message);
        chatMessageList.add(sendMessage);
        adapter.refresh(chatMessageList);
        messageListView.setSelection(adapter.getCount() - 1);
        messageEditText.setText("");
    }
}
