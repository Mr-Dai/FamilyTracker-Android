package com.wetrack;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wetrack.client.EntityCallback;
import com.wetrack.client.WeTrackClient;
import com.wetrack.map.MapController;
import com.wetrack.model.Chat;
import com.wetrack.model.ChatMessage;
import com.wetrack.service.ChatServiceManager;
import com.wetrack.utils.ConstantValues;
import com.wetrack.utils.PreferenceUtils;
import com.wetrack.utils.Tools;
import com.wetrack.view.AddOptionListView;
import com.wetrack.view.SidebarView;
import com.wetrack.view.adapter.AddContactAdapter;

public class MainActivity extends AppCompatActivity {
    private MapController mMapController = null;
    private ImageButton openSidebarButton;
    private SidebarView sidebarView = null;
    private ImageButton addContactButton;
    private AddOptionListView addOptionListView = null;
    private Button chatListButton;
    private ImageButton chatListImageButton;
    private RelativeLayout buttonLayout;
    private DrawerLayout mDrawerLayout;
    private DrawerLayout.DrawerListener mDrawerListener;

    private ChatServiceManager mChatServiceManager = null;
    private TextView unreadMessage;
    private int unread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Tools.setMainContext(this);

        initChatServiceManager();

        initMapInView(R.id.map_content);
        initSidebar();
        initAddContact();
        initChatList();
        initChatButton();
        initUnreadMessage();
    }

    private void initChatServiceManager() {
        mChatServiceManager = new ChatServiceManager(this) {
            @Override
            public void onReceivedMessage(ChatMessage receivedMessage) {
                // TODO Implement this
            }

            // Message will not be sent or ACKed on this activity.
            @Override
            public void onReceivedMessageAck(String ackedMessageId) {
            }
        };
        mChatServiceManager.start();
    }

    public void initMapInView(int viewId) {
        mMapController = MapController.getInstance();
        mMapController.createFragmentInContainer(getSupportFragmentManager(), viewId);
        mMapController.start();
    }

    private void initSidebar() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerListener = new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {}
            @Override
            public void onDrawerOpened(View drawerView) {}
            @Override
            public void onDrawerClosed(View drawerView) {}
            @Override
            public void onDrawerStateChanged(int newState) {}
        };

        mDrawerLayout.addDrawerListener(mDrawerListener);

        sidebarView = new SidebarView(this);
        mDrawerLayout.addView(sidebarView, mDrawerLayout.getChildCount());

        DrawerLayout.LayoutParams layoutParams = new DrawerLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, Gravity.LEFT);
        mDrawerLayout.updateViewLayout(sidebarView, layoutParams);

        mDrawerLayout.closeDrawer(sidebarView);

        openSidebarButton = (ImageButton) findViewById(R.id.open_sidebar_button);
        openSidebarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSidebarButton.setEnabled(false);
                if (addOptionListView.getVisibility() == View.VISIBLE) {
                    addOptionListView.close();
                }
                if (mDrawerLayout.isDrawerOpen(sidebarView)) {
                    mDrawerLayout.closeDrawer(sidebarView);
                } else {
                    mDrawerLayout.openDrawer(sidebarView);
                }
                openSidebarButton.setEnabled(true);
            }
        });
        sidebarView.setLogoutListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.setCurrentToken("");
                PreferenceUtils.saveStringValue(PreferenceUtils.KEY_TOKEN_EXPIRE_TIME, "");
                Intent intent = new Intent(MainActivity.this,
                        LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
            }
        });
        sidebarView.setUserInfoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(sidebarView);

                Intent intent = new Intent(MainActivity.this,
                        UserInfoActivity.class);
                startActivityForResult(intent, ConstantValues.USER_INFO_REQUEST_CODE);
            }
        });
        sidebarView.setSettingClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawer(sidebarView);

                Intent intent = new Intent(MainActivity.this,
                        SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initAddContact() {
        final int[] imgs = {R.drawable.chat, R.drawable.add_friend};
        final String[] texts = {"New Group", "Add Friend"};
        addContactButton = (ImageButton) findViewById(R.id.add_contact_button);
        addOptionListView = (AddOptionListView) findViewById(R.id.add_option_listview);
        addOptionListView.setVisibility(View.INVISIBLE);
        AddContactAdapter adapter = new AddContactAdapter(this, imgs, texts);

        addOptionListView.setAdapter(adapter);
        addOptionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //addOptionListView.close();
                addOptionListView.setVisibility(View.INVISIBLE);
                if (texts[position].equals(texts[0])) {
                    Intent intent = new Intent(MainActivity.this, CreateChatActivity.class);
                    startActivityForResult(intent, ConstantValues.CREATE_CHAT_REQUEST_CODE);
                } else if (texts[position].equals(texts[1])) {
                    Intent intent = new Intent(MainActivity.this, AddFriendActivity.class);
                    startActivityForResult(intent, ConstantValues.ADD_FRIEND_REQUEST_CODE);
                }
            }
        });

        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContactButton.setEnabled(false);

                if (mDrawerLayout.isDrawerOpen(sidebarView)) {
                    mDrawerLayout.closeDrawer(sidebarView);
                }

                if (addOptionListView.getVisibility() == View.INVISIBLE) {
                    addOptionListView.open();
                } else {
                    addOptionListView.close();
                }
                addContactButton.setEnabled(true);
            }
        });

    }

    private void initChatList() {
        chatListButton = (Button) findViewById(R.id.chat_list_button);
        chatListImageButton = (ImageButton) findViewById(R.id.chat_list_imagebutton);
        WeTrackClient.singleton().getChatInfo(
                PreferenceUtils.getCurrentChatId(), PreferenceUtils.getCurrentToken(),
                new EntityCallback<Chat>() {
                    @Override
                    protected void onReceive(Chat chat) {
                        chatListButton.setText(chat.getName());
                    }
                }
        );

        View.OnClickListener chatListListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addOptionListView.getVisibility() == View.VISIBLE) {
                    addOptionListView.close();
                }
                if (mDrawerLayout.isDrawerOpen(sidebarView)) {
                    mDrawerLayout.closeDrawer(sidebarView);
                }
                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivityForResult(intent, ConstantValues.CHAT_LIST_REQUEST_CODE);
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
            }
        };

        chatListImageButton.setOnClickListener(chatListListener);
        chatListButton.setOnClickListener(chatListListener);
    }

    private void initChatButton() {
        //chatButton = (Button) findViewById(R.id.chat_button);
        //chatButton.setOnClickListener(new View.OnClickListener() {
        buttonLayout = (RelativeLayout) findViewById(R.id.button_layout);
        unreadMessage = (TextView) findViewById(R.id.unread_msg_number);
        buttonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ChatActivity.class);
                startActivityForResult(i, ConstantValues.CHAT_REQUEST_CODE);
            }
        });
    }

    private void initUnreadMessage() {
        unread = 0;
        mChatServiceManager = new ChatServiceManager(this) {
            @Override
            public void onReceivedMessage(ChatMessage receivedMessage) {
                if (!receivedMessage.getChatId().equals(PreferenceUtils.getCurrentChatId()))
                    return;
                unread++;
                unreadMessage.setText(String.valueOf(unread));
                unreadMessage.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedMessageAck(String ackedMessageId) {

            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ConstantValues.CHAT_LIST_REQUEST_CODE:
                overridePendingTransition(R.anim.fade_in, R.anim.slide_out_up);
                switch (resultCode) {
                    case RESULT_CANCELED:
                        Log.d(ConstantValues.debugTab, "chat list canceled");
                        break;
                    case RESULT_OK:
                        Log.d(ConstantValues.debugTab, "chat list succeed");
                        String chatName = data.getStringExtra(ChatListActivity.KEY_CHAT_NAME);
                        chatListButton.setText(chatName);
                        mMapController.clearAllSymbols();
                        break;
                }
                break;
            case ConstantValues.CREATE_CHAT_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        Log.d(ConstantValues.debugTab, "create group cenceled");
                        break;
                    case RESULT_OK:
                        Log.d(ConstantValues.debugTab, "create group succeed");
                        //TODO get information from 'data' here
                        break;
                }
                break;
            case ConstantValues.ADD_FRIEND_REQUEST_CODE:
                switch (resultCode) {
                    case RESULT_CANCELED:
                        Log.d(ConstantValues.debugTab, "adding friend cenceled");
                        break;
                    case RESULT_OK:
                        Log.d(ConstantValues.debugTab, "adding friend succeeds");
                        //TODO get information from 'data' here
                        break;
                }
                break;
            case ConstantValues.CHAT_REQUEST_CODE:
                unread = 0;
                unreadMessage.setVisibility(View.GONE);
                break;

            case ConstantValues.USER_INFO_REQUEST_CODE:

                sidebarView.updateUserPortrait();

                switch (resultCode) {
                    case RESULT_CANCELED:
                        Log.d(ConstantValues.debugTab, "user-info activity cenceled");
                        break;
                    case RESULT_OK:
                        Log.d(ConstantValues.debugTab, "user-info activity succeeds");
                        if (data.getBooleanExtra("USER_INFO", false)) {
                            sidebarView.updateUserInfo();
                        }
                        break;
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatServiceManager != null) {
            mChatServiceManager.stop();
            mChatServiceManager = null;
        }
        if (mMapController != null) {
            mMapController.stop();
        }
    }

}
