/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Components;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.AnimationCompat.AnimatorListenerAdapterProxy;
import org.telegram.android.AnimationCompat.AnimatorSetProxy;
import org.telegram.android.AnimationCompat.ObjectAnimatorProxy;
import org.telegram.android.AnimationCompat.ViewProxy;
import org.telegram.android.Emoji;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.MessageObject;
import org.telegram.android.MessagesController;
import org.telegram.android.NotificationCenter;
import org.telegram.android.SendMessagesHelper;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BaseFragment;

/**
 * TODO-aragats
 */
public class PostCreateActivityEnterView extends FrameLayoutFixed implements NotificationCenter.NotificationCenterDelegate, SizeNotifierRelativeLayout.SizeNotifierRelativeLayoutDelegate {

    public interface PostCreateActivityEnterViewDelegate {
        void onMessageSend(String message);
        void needSendTyping();
        void onTextChanged(CharSequence text, boolean bigChange);
        void onAttachButtonHidden();
        void onAttachButtonShow();
        void onWindowSizeChanged(int size);
    }

    private EditText messageEditText;
    private ImageView sendButton;
    private PopupWindow emojiPopup;
    private ImageView emojiButton;
    private EmojiView emojiView;
    private View sizeNotifierLayout;
    private FrameLayout attachButton;
    private LinearLayout textFieldContainer;
    private View topView;

    private int framesDroped;

    private int keyboardTransitionState;
    private boolean showKeyboardOnEmojiButton;
    private ViewTreeObserver.OnPreDrawListener onPreDrawListener;

    private PowerManager.WakeLock mWakeLock;
    private AnimatorSetProxy runningAnimation;
    private AnimatorSetProxy runningAnimation2;
    private ObjectAnimatorProxy runningAnimationAudio;
    private int runningAnimationType;
    private int audioInterfaceState;

    private int keyboardHeight;
    private int keyboardHeightLand;
    private boolean keyboardVisible;
    private boolean sendByEnter;
    private long lastTypingTimeSend;
    private String lastTimeString;
    private float startedDraggingX = -1;
    private float distCanMove = AndroidUtilities.dp(80);
    private boolean recordingAudio;
    private boolean forceShowSendButton;
    private boolean allowStickers;

    private Activity parentActivity;
    private BaseFragment parentFragment;
    private long dialog_id;
    private boolean ignoreTextChange;
    private MessageObject replyingMessageObject;
    private TLRPC.WebPage messageWebPage;
    private boolean messageWebPageSearch = true;
    private PostCreateActivityEnterViewDelegate delegate;

    private float topViewAnimation;
    private boolean topViewShowed;
    private boolean needShowTopView;
    private boolean allowShowTopView;
    private AnimatorSetProxy currentTopViewAnimation;

    public PostCreateActivityEnterView(Activity context, View parent, BaseFragment fragment, boolean isChat) {
        super(context);
        setBackgroundResource(R.drawable.compose_panel);
        setFocusable(true);
        setFocusableInTouchMode(true);

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.hideEmojiKeyboard);
        parentActivity = context;
        parentFragment = fragment;
        sizeNotifierLayout = parent;
        if (sizeNotifierLayout instanceof SizeNotifierRelativeLayout) {
            ((SizeNotifierRelativeLayout) sizeNotifierLayout).setDelegate(this);
        } else if (sizeNotifierLayout instanceof SizeNotifierFrameLayout) {
            ((SizeNotifierFrameLayout) sizeNotifierLayout).setDelegate(this);
        }
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        sendByEnter = preferences.getBoolean("send_by_enter", false);

        parent.getViewTreeObserver().addOnPreDrawListener(onPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (keyboardTransitionState == 1) {
                    if (keyboardVisible || framesDroped >= 60) {
                        showEmojiPopup(false, false);
                        keyboardTransitionState = 0;
                    } else {
                        openKeyboard();
                    }
                    framesDroped++;
                    return false;
                } else if (keyboardTransitionState == 2) {
                    if (!keyboardVisible || framesDroped >= 60) {
                        int currentHeight = AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? keyboardHeightLand : keyboardHeight;
                        sizeNotifierLayout.setPadding(0, 0, 0, currentHeight);
                        keyboardTransitionState = 0;
                    }
                    framesDroped++;
                    return false;
                }
                return true;
            }
        });

        textFieldContainer = new LinearLayout(context);
        textFieldContainer.setBackgroundColor(0xffffffff);
        textFieldContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(textFieldContainer);
        LayoutParams layoutParams2 = (LayoutParams) textFieldContainer.getLayoutParams();
        layoutParams2.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams2.width = LayoutHelper.MATCH_PARENT;
        layoutParams2.height = LayoutHelper.WRAP_CONTENT;
        layoutParams2.topMargin = AndroidUtilities.dp(2);
        textFieldContainer.setLayoutParams(layoutParams2);

        FrameLayoutFixed frameLayout = new FrameLayoutFixed(context);
        textFieldContainer.addView(frameLayout);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) frameLayout.getLayoutParams();
        layoutParams.width = 0;
        layoutParams.height = LayoutHelper.WRAP_CONTENT;
        layoutParams.weight = 1;
        frameLayout.setLayoutParams(layoutParams);

        emojiButton = new ImageView(context);
        emojiButton.setImageResource(R.drawable.ic_msg_panel_smiles);
        emojiButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        emojiButton.setPadding(AndroidUtilities.dp(4), AndroidUtilities.dp(1), 0, 0);
        frameLayout.addView(emojiButton);
        LayoutParams layoutParams1 = (LayoutParams) emojiButton.getLayoutParams();
        layoutParams1.width = AndroidUtilities.dp(48);
        layoutParams1.height = AndroidUtilities.dp(48);
        layoutParams1.gravity = Gravity.BOTTOM;
        emojiButton.setLayoutParams(layoutParams1);
        emojiButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showKeyboardOnEmojiButton) {
                    setKeyboardTransitionState(1);
                    int selection = messageEditText.getSelectionStart();
                    MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0, 0, 0);
                    messageEditText.onTouchEvent(event);
                    event.recycle();
                    messageEditText.setSelection(selection);
                } else {
                    showEmojiPopup(emojiPopup == null || !emojiPopup.isShowing(), true);
                }
            }
        });

        messageEditText = new EditText(context);
        messageEditText.setHint(LocaleController.getString("TypeMessage", R.string.TypeMessage));
        messageEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        messageEditText.setInputType(messageEditText.getInputType() | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        messageEditText.setSingleLine(false);
        messageEditText.setMaxLines(4);
        messageEditText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        messageEditText.setGravity(Gravity.BOTTOM);
        messageEditText.setPadding(0, AndroidUtilities.dp(11), 0, AndroidUtilities.dp(12));
        messageEditText.setBackgroundDrawable(null);
        AndroidUtilities.clearCursorDrawable(messageEditText);
        messageEditText.setTextColor(0xff000000);
        messageEditText.setHintTextColor(0xffb2b2b2);
        frameLayout.addView(messageEditText);
        layoutParams1 = (LayoutParams) messageEditText.getLayoutParams();
        layoutParams1.width = LayoutHelper.MATCH_PARENT;
        layoutParams1.height = LayoutHelper.WRAP_CONTENT;
        layoutParams1.gravity = Gravity.BOTTOM;
        layoutParams1.leftMargin = AndroidUtilities.dp(52);
        layoutParams1.rightMargin = AndroidUtilities.dp(isChat ? 50 : 2);
        messageEditText.setLayoutParams(layoutParams1);
        messageEditText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == 4 && !keyboardVisible && emojiPopup != null && emojiPopup.isShowing()) {
                    if (keyEvent.getAction() == 1) {
                        showEmojiPopup(false, true);
                    }
                    return true;
                } else if (i == KeyEvent.KEYCODE_ENTER && sendByEnter && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });
        messageEditText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (emojiPopup != null && emojiPopup.isShowing()) {
                    setKeyboardTransitionState(1);
                }
            }
        });
        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                    return true;
                } else if (sendByEnter) {
                    if (keyEvent != null && i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        sendMessage();
                        return true;
                    }
                }
                return false;
            }
        });
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                String message = getTrimmedString(charSequence.toString());
                checkSendButton(true);

                if (delegate != null) {
                    if (count > 2 || charSequence == null || charSequence.length() == 0) {
                        messageWebPageSearch = true;
                    }
                    delegate.onTextChanged(charSequence, before > count + 1 || (count - before) > 2);
                }

                if (message.length() != 0 && lastTypingTimeSend < System.currentTimeMillis() - 5000 && !ignoreTextChange) {
                    int currentTime = ConnectionsManager.getInstance().getCurrentTime();
                    TLRPC.User currentUser = null;
                    if ((int) dialog_id > 0) {
                        currentUser = MessagesController.getInstance().getUser((int) dialog_id);
                    }
                    if (currentUser != null && (currentUser.id == UserConfig.getClientUserId() || currentUser.status != null && currentUser.status.expires < currentTime)) {
                        return;
                    }
                    lastTypingTimeSend = System.currentTimeMillis();
                    if (delegate != null) {
                        delegate.needSendTyping();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (sendByEnter && editable.length() > 0 && editable.charAt(editable.length() - 1) == '\n') {
                    sendMessage();
                }
                int i = 0;
                ImageSpan[] arrayOfImageSpan = editable.getSpans(0, editable.length(), ImageSpan.class);
                int j = arrayOfImageSpan.length;
                while (true) {
                    if (i >= j) {
                        Emoji.replaceEmoji(editable, messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20));
                        return;
                    }
                    editable.removeSpan(arrayOfImageSpan[i]);
                    i++;
                }
            }
        });

        if (isChat) {
            attachButton = new FrameLayout(context);
            attachButton.setEnabled(false);
            ViewProxy.setPivotX(attachButton, AndroidUtilities.dp(48));
            frameLayout.addView(attachButton);
            layoutParams1 = (LayoutParams) attachButton.getLayoutParams();
            layoutParams1.width = AndroidUtilities.dp(48);
            layoutParams1.height = AndroidUtilities.dp(48);
            layoutParams1.gravity = Gravity.BOTTOM | Gravity.RIGHT;
            attachButton.setLayoutParams(layoutParams1);
        }



        FrameLayout frameLayout1 = new FrameLayout(context);
        textFieldContainer.addView(frameLayout1);
        layoutParams = (LinearLayout.LayoutParams) frameLayout1.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(48);
        layoutParams.height = AndroidUtilities.dp(48);
        layoutParams.gravity = Gravity.BOTTOM;
        frameLayout1.setLayoutParams(layoutParams);

        sendButton = new ImageView(context);
        sendButton.setVisibility(View.INVISIBLE);
        sendButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        sendButton.setImageResource(R.drawable.ic_send);
        sendButton.setSoundEffectsEnabled(false);
        ViewProxy.setScaleX(sendButton, 0.1f);
        ViewProxy.setScaleY(sendButton, 0.1f);
        ViewProxy.setAlpha(sendButton, 0.0f);
        sendButton.clearAnimation();
        frameLayout1.addView(sendButton);
        layoutParams1 = (LayoutParams) sendButton.getLayoutParams();
        layoutParams1.width = AndroidUtilities.dp(48);
        layoutParams1.height = AndroidUtilities.dp(48);
        sendButton.setLayoutParams(layoutParams1);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        checkSendButton(false);
    }

    private void setKeyboardTransitionState(int state) {
        if (AndroidUtilities.usingHardwareInput) {
            if (state == 1) {
                showEmojiPopup(false, false);
                keyboardTransitionState = 0;

            } else if (state == 2) {
                int currentHeight = AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? keyboardHeightLand : keyboardHeight;
                sizeNotifierLayout.setPadding(0, 0, 0, currentHeight);
                keyboardTransitionState = 0;
            }
        } else {
            framesDroped = 0;
            keyboardTransitionState = state;
            if (state == 1) {
                sizeNotifierLayout.setPadding(0, 0, 0, 0);
            }
        }
    }

    public void addTopView(View view, int height) {
        if (view == null) {
            return;
        }
        addView(view, 0);
        topView = view;
        topView.setVisibility(GONE);
        needShowTopView = false;
        LayoutParams layoutParams = (LayoutParams) topView.getLayoutParams();
        layoutParams.width = LayoutHelper.MATCH_PARENT;
        layoutParams.height = height;
        layoutParams.topMargin = AndroidUtilities.dp(2);
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        topView.setLayoutParams(layoutParams);
    }

    public void setTopViewAnimation(float progress) {
        topViewAnimation = progress;
        LayoutParams layoutParams2 = (LayoutParams) textFieldContainer.getLayoutParams();
        layoutParams2.topMargin = AndroidUtilities.dp(2) + (int) (topView.getLayoutParams().height * progress);
        textFieldContainer.setLayoutParams(layoutParams2);
    }

    public float getTopViewAnimation() {
        return topViewAnimation;
    }

    public void setForceShowSendButton(boolean value, boolean animated) {
        forceShowSendButton = value;
        checkSendButton(animated);
    }

    public void setAllowStickers(boolean value) {
        allowStickers = value;
    }

    public void showTopView(boolean animated) {
        if (topView == null || topViewShowed) {
            return;
        }
        needShowTopView = true;
        topViewShowed = true;
        if (allowShowTopView) {
            topView.setVisibility(VISIBLE);
            if (currentTopViewAnimation != null) {
                currentTopViewAnimation.cancel();
                currentTopViewAnimation = null;
            }
            if (animated) {
                if (keyboardVisible || emojiPopup != null && emojiPopup.isShowing()) {
                    currentTopViewAnimation = new AnimatorSetProxy();
                    currentTopViewAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(PostCreateActivityEnterView.this, "topViewAnimation", 1.0f)
                    );
                    currentTopViewAnimation.addListener(new AnimatorListenerAdapterProxy() {
                        @Override
                        public void onAnimationEnd(Object animation) {
                            if (animation == currentTopViewAnimation) {
                                setTopViewAnimation(1.0f);
                                if (!forceShowSendButton) {
                                    openKeyboard();
                                }
                                currentTopViewAnimation = null;
                            }
                        }
                    });
                    currentTopViewAnimation.setDuration(200);
                    currentTopViewAnimation.start();
                } else {
                    setTopViewAnimation(1.0f);
                    if (!forceShowSendButton) {
                        openKeyboard();
                    }
                }
            } else {
                setTopViewAnimation(1.0f);
            }
        }
    }

    public void hideTopView(final boolean animated) {
        if (topView == null || !topViewShowed) {
            return;
        }

        topViewShowed = false;
        needShowTopView = false;
        if (allowShowTopView) {
            float resumeValue = 1.0f;
            if (currentTopViewAnimation != null) {
                resumeValue = topViewAnimation;
                currentTopViewAnimation.cancel();
                currentTopViewAnimation = null;
            }
            if (animated) {
                currentTopViewAnimation = new AnimatorSetProxy();
                currentTopViewAnimation.playTogether(
                        ObjectAnimatorProxy.ofFloat(PostCreateActivityEnterView.this, "topViewAnimation", resumeValue, 0.0f)
                );
                currentTopViewAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (animation == currentTopViewAnimation) {
                            topView.setVisibility(GONE);
                            setTopViewAnimation(0.0f);
                            currentTopViewAnimation = null;
                        }
                    }
                });
                currentTopViewAnimation.setDuration(200);
                currentTopViewAnimation.start();
            } else {
                topView.setVisibility(GONE);
                setTopViewAnimation(0.0f);
            }
        }
    }

    public boolean isTopViewVisible() {
        return topView != null && topView.getVisibility() == VISIBLE;
    }

    private void onWindowSizeChanged(int size) {
        if (delegate != null) {
            delegate.onWindowSizeChanged(size);
        }
        if (topView != null) {
            if (size < AndroidUtilities.dp(72) + AndroidUtilities.getCurrentActionBarHeight()) {
                if (allowShowTopView) {
                    allowShowTopView = false;
                    if (needShowTopView) {
                        topView.setVisibility(View.GONE);
                        setTopViewAnimation(0.0f);
                    }
                }
            } else {
                if (!allowShowTopView) {
                    allowShowTopView = true;
                    if (needShowTopView) {
                        topView.setVisibility(View.VISIBLE);
                        setTopViewAnimation(1.0f);
                    }
                }
            }
        }
    }

    public void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.hideEmojiKeyboard);
        sizeNotifierLayout.getViewTreeObserver().removeOnPreDrawListener(onPreDrawListener);
        if (mWakeLock != null) {
            try {
                mWakeLock.release();
                mWakeLock = null;
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
        if (sizeNotifierLayout != null) {
            if (sizeNotifierLayout instanceof SizeNotifierRelativeLayout) {
                ((SizeNotifierRelativeLayout) sizeNotifierLayout).setDelegate(null);
            } else if (sizeNotifierLayout instanceof SizeNotifierFrameLayout) {
                ((SizeNotifierFrameLayout) sizeNotifierLayout).setDelegate(null);
            }
        }
    }

    public void setDialogId(long id) {
        dialog_id = id;
    }

    public void setReplyingMessageObject(MessageObject messageObject) {
        replyingMessageObject = messageObject;
    }

    public void setWebPage(TLRPC.WebPage webPage, boolean searchWebPages) {
        messageWebPage = webPage;
        messageWebPageSearch = searchWebPages;
    }

    public boolean isMessageWebPageSearchEnabled() {
        return messageWebPageSearch;
    }

    private void sendMessage() {
        if (parentFragment != null) {
            String action;
            TLRPC.Chat currentChat;
            if ((int) dialog_id < 0) {
                currentChat = MessagesController.getInstance().getChat(-(int) dialog_id);
                if (currentChat != null && currentChat.participants_count > MessagesController.getInstance().groupBigSize) {
                    action = "bigchat_message";
                } else {
                    action = "chat_message";
                }
            } else {
                action = "pm_message";
            }
            if (!MessagesController.isFeatureEnabled(action, parentFragment)) {
                return;
            }
        }
        String message = messageEditText.getText().toString();
        if (processSendingText(message)) {
            messageEditText.setText("");
            lastTypingTimeSend = 0;
            if (delegate != null) {
                delegate.onMessageSend(message);
            }
        } else if (forceShowSendButton) {
            if (delegate != null) {
                delegate.onMessageSend(null);
            }
        }
    }

    public boolean processSendingText(String text) {
        text = getTrimmedString(text);
        if (text.length() != 0) {
            int count = (int) Math.ceil(text.length() / 4096.0f);
            for (int a = 0; a < count; a++) {
                String mess = text.substring(a * 4096, Math.min((a + 1) * 4096, text.length()));
                SendMessagesHelper.getInstance().sendMessage(mess, dialog_id, replyingMessageObject, messageWebPage, messageWebPageSearch);
            }
            return true;
        }
        return false;
    }

    private String getTrimmedString(String src) {
        String result = src.trim();
        if (result.length() == 0) {
            return result;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }

    private void checkSendButton(final boolean animated) {
        String message = getTrimmedString(messageEditText.getText().toString());
        if (message.length() > 0 || forceShowSendButton) {
            if (attachButton.getVisibility() == View.VISIBLE) {
                if (animated) {
                    if (runningAnimationType == 1) {
                        return;
                    }
                    if (runningAnimation != null) {
                        runningAnimation.cancel();
                        runningAnimation = null;
                    }
                    if (runningAnimation2 != null) {
                        runningAnimation2.cancel();
                        runningAnimation2 = null;
                    }

                    if (attachButton != null) {
                        runningAnimation2 = new AnimatorSetProxy();
                        runningAnimation2.playTogether(
                                ObjectAnimatorProxy.ofFloat(attachButton, "alpha", 0.0f),
                                ObjectAnimatorProxy.ofFloat(attachButton, "scaleX", 0.0f)
                        );
                        runningAnimation2.setDuration(100);
                        runningAnimation2.addListener(new AnimatorListenerAdapterProxy() {
                            @Override
                            public void onAnimationEnd(Object animation) {
                                if (runningAnimation2.equals(animation)) {
                                    attachButton.setVisibility(View.GONE);
                                    attachButton.clearAnimation();
                                }
                            }
                        });
                        runningAnimation2.start();

                        if (messageEditText != null) {
                            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageEditText.getLayoutParams();
                            layoutParams.rightMargin = AndroidUtilities.dp(0);
                            messageEditText.setLayoutParams(layoutParams);
                        }

                        delegate.onAttachButtonHidden();
                    }

                    sendButton.setVisibility(View.VISIBLE);
                    runningAnimation = new AnimatorSetProxy();
                    runningAnimationType = 1;

                    runningAnimation.playTogether(
                            ObjectAnimatorProxy.ofFloat(sendButton, "scaleX", 1.0f),
                            ObjectAnimatorProxy.ofFloat(sendButton, "scaleY", 1.0f),
                            ObjectAnimatorProxy.ofFloat(sendButton, "alpha", 1.0f)
                    );

                    runningAnimation.setDuration(150);
                    runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                        @Override
                        public void onAnimationEnd(Object animation) {
                            if (runningAnimation.equals(animation)) {
                                sendButton.setVisibility(View.VISIBLE);
                                runningAnimation = null;
                                runningAnimationType = 0;
                            }
                        }
                    });
                    runningAnimation.start();
                } else {
                    ViewProxy.setScaleX(sendButton, 1.0f);
                    ViewProxy.setScaleY(sendButton, 1.0f);
                    ViewProxy.setAlpha(sendButton, 1.0f);
                    sendButton.setVisibility(View.VISIBLE);
                    if (attachButton != null) {
                        attachButton.setVisibility(View.GONE);
                        attachButton.clearAnimation();
                        delegate.onAttachButtonHidden();
                        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) messageEditText.getLayoutParams();
                        layoutParams.rightMargin = AndroidUtilities.dp(0);
                        messageEditText.setLayoutParams(layoutParams);
                    }
                }
            }

        } else if (sendButton.getVisibility() == View.VISIBLE) {
            if (animated) {
                if (runningAnimationType == 2) {
                    return;
                }

                if (runningAnimation != null) {
                    runningAnimation.cancel();
                    runningAnimation = null;
                }
                if (runningAnimation2 != null) {
                    runningAnimation2.cancel();
                    runningAnimation2 = null;
                }

                if (attachButton != null) {
                    attachButton.setVisibility(View.VISIBLE);
                    runningAnimation2 = new AnimatorSetProxy();
                    runningAnimation2.playTogether(
                            ObjectAnimatorProxy.ofFloat(attachButton, "alpha", 1.0f),
                            ObjectAnimatorProxy.ofFloat(attachButton, "scaleX", 1.0f)
                    );
                    runningAnimation2.setDuration(100);
                    runningAnimation2.start();

                    if (messageEditText != null) {
                        LayoutParams layoutParams = (LayoutParams) messageEditText.getLayoutParams();
                        layoutParams.rightMargin = AndroidUtilities.dp(50);
                        messageEditText.setLayoutParams(layoutParams);
                    }

                    delegate.onAttachButtonShow();
                }

                runningAnimation = new AnimatorSetProxy();
                runningAnimationType = 2;

                runningAnimation.playTogether(
                        ObjectAnimatorProxy.ofFloat(sendButton, "scaleX", 0.1f),
                        ObjectAnimatorProxy.ofFloat(sendButton, "scaleY", 0.1f),
                        ObjectAnimatorProxy.ofFloat(sendButton, "alpha", 0.0f)
                );

                runningAnimation.setDuration(150);
                runningAnimation.addListener(new AnimatorListenerAdapterProxy() {
                    @Override
                    public void onAnimationEnd(Object animation) {
                        if (runningAnimation.equals(animation)) {
                            sendButton.setVisibility(View.GONE);
                            sendButton.clearAnimation();
                            runningAnimation = null;
                            runningAnimationType = 0;
                        }
                    }
                });
                runningAnimation.start();
            } else {
                ViewProxy.setScaleX(sendButton, 0.1f);
                ViewProxy.setScaleY(sendButton, 0.1f);
                ViewProxy.setAlpha(sendButton, 0.0f);
                sendButton.setVisibility(View.GONE);
                sendButton.clearAnimation();
                if (attachButton != null) {
                    delegate.onAttachButtonShow();
                    attachButton.setVisibility(View.VISIBLE);
                    LayoutParams layoutParams = (LayoutParams) messageEditText.getLayoutParams();
                    layoutParams.rightMargin = AndroidUtilities.dp(50);
                    messageEditText.setLayoutParams(layoutParams);
                }
            }
        }
    }



    private void showEmojiPopup(boolean show, boolean post) {
        if (show) {
            if (emojiPopup == null) {
                if (parentActivity == null) {
                    return;
                }
                emojiView = new EmojiView(allowStickers, parentActivity);
                emojiView.setListener(new EmojiView.Listener() {
                    public boolean onBackspace() {
                        if (messageEditText.length() == 0) {
                            return false;
                        }
                        messageEditText.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                        return true;
                    }

                    public void onEmojiSelected(String symbol) {
                        int i = messageEditText.getSelectionEnd();
                        if (i < 0) {
                            i = 0;
                        }
                        try {//TODO check
                            CharSequence localCharSequence = Emoji.replaceEmoji(symbol/* + "\uFE0F"*/, messageEditText.getPaint().getFontMetricsInt(), AndroidUtilities.dp(20));
                            messageEditText.setText(messageEditText.getText().insert(i, localCharSequence));
                            int j = i + localCharSequence.length();
                            messageEditText.setSelection(j, j);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    }

                    public void onStickerSelected(TLRPC.Document sticker) {
                        SendMessagesHelper.getInstance().sendSticker(sticker, dialog_id, replyingMessageObject);
                        if (delegate != null) {
                            delegate.onMessageSend(null);
                        }
                    }
                });
                emojiPopup = new PopupWindow(emojiView);
            }

            if (keyboardHeight <= 0) {
                keyboardHeight = ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0).getInt("kbd_height", AndroidUtilities.dp(200));
            }
            if (keyboardHeightLand <= 0) {
                keyboardHeightLand = ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0).getInt("kbd_height_land3", AndroidUtilities.dp(200));
            }
            int currentHeight = AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y ? keyboardHeightLand : keyboardHeight;
            FileLog.e("tmessages", "show emoji with height = " + currentHeight);
            emojiPopup.setHeight(MeasureSpec.makeMeasureSpec(currentHeight, MeasureSpec.EXACTLY));
            if (sizeNotifierLayout != null) {
                emojiPopup.setWidth(MeasureSpec.makeMeasureSpec(AndroidUtilities.displaySize.x, MeasureSpec.EXACTLY));
            }

            emojiPopup.showAtLocation(parentActivity.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.LEFT, 0, 0);

            if (!keyboardVisible) {
                if (sizeNotifierLayout != null) {
                    sizeNotifierLayout.setPadding(0, 0, 0, currentHeight);
                    emojiButton.setImageResource(R.drawable.ic_msg_panel_hide);
                    showKeyboardOnEmojiButton = false;
                    onWindowSizeChanged(sizeNotifierLayout.getHeight() - sizeNotifierLayout.getPaddingBottom());
                }
                return;
            } else {
                setKeyboardTransitionState(2);
                AndroidUtilities.hideKeyboard(messageEditText);
            }
            emojiButton.setImageResource(R.drawable.ic_msg_panel_kb);
            showKeyboardOnEmojiButton = true;
            return;
        }
        if (emojiButton != null) {
            showKeyboardOnEmojiButton = false;
            emojiButton.setImageResource(R.drawable.ic_msg_panel_smiles);
        }
        if (emojiPopup != null) {
            try {
                emojiPopup.dismiss();
            } catch (Exception e) {
                //don't promt
            }
        }
        if (keyboardTransitionState == 0) {
            if (sizeNotifierLayout != null) {
                if (post) {
                    sizeNotifierLayout.post(new Runnable() {
                        public void run() {
                            if (sizeNotifierLayout != null) {
                                sizeNotifierLayout.setPadding(0, 0, 0, 0);
                                onWindowSizeChanged(sizeNotifierLayout.getHeight());
                            }
                        }
                    });
                } else {
                    sizeNotifierLayout.setPadding(0, 0, 0, 0);
                    onWindowSizeChanged(sizeNotifierLayout.getHeight());
                }
            }
        }
    }

    public void hideEmojiPopup() {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            showEmojiPopup(false, true);
        }
    }

    public void openKeyboard() {
        AndroidUtilities.showKeyboard(messageEditText);
    }

    public void setDelegate(PostCreateActivityEnterViewDelegate delegate) {
        this.delegate = delegate;
    }

    public void setFieldText(String text) {
        if (messageEditText == null) {
            return;
        }
        ignoreTextChange = true;
        messageEditText.setText(text);
        messageEditText.setSelection(messageEditText.getText().length());
        ignoreTextChange = false;
        if (delegate != null) {
            delegate.onTextChanged(messageEditText.getText(), true);
        }
    }

    public int getCursorPosition() {
        if (messageEditText == null) {
            return 0;
        }
        return messageEditText.getSelectionStart();
    }

    public void replaceWithText(int start, int len, String text) {
        try {
            StringBuilder builder = new StringBuilder(messageEditText.getText());
            builder.replace(start, start + len, text);
            messageEditText.setText(builder);
            messageEditText.setSelection(start + text.length());
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public void setFieldFocused(boolean focus) {
        if (messageEditText == null) {
            return;
        }
        if (focus) {
            if (!messageEditText.isFocused()) {
                messageEditText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (messageEditText != null) {
                            try {
                                messageEditText.requestFocus();
                            } catch (Exception e) {
                                FileLog.e("tmessages", e);
                            }
                        }
                    }
                }, 600);
            }
        } else {
            if (messageEditText.isFocused() && !keyboardVisible) {
                messageEditText.clearFocus();
            }
        }
    }

    public boolean hasText() {
        return messageEditText != null && messageEditText.length() > 0;
    }

    public String getFieldText() {
        if (messageEditText != null && messageEditText.length() > 0) {
            return messageEditText.getText().toString();
        }
        return null;
    }

    public boolean isEmojiPopupShowing() {
        return emojiPopup != null && emojiPopup.isShowing();
    }

    public void addToAttachLayout(View view) {
        if (attachButton == null) {
            return;
        }
        if (view.getParent() != null) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            viewGroup.removeView(view);
        }
        attachButton.addView(view);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = AndroidUtilities.dp(48);
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    @Override
    public void onSizeChanged(int height, boolean isWidthGreater) {
        if (height > AndroidUtilities.dp(50) && keyboardVisible) {
            if (isWidthGreater) {
                keyboardHeightLand = height;
                ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0).edit().putInt("kbd_height_land3", keyboardHeightLand).commit();
            } else {
                keyboardHeight = height;
                ApplicationLoader.applicationContext.getSharedPreferences("emoji", 0).edit().putInt("kbd_height", keyboardHeight).commit();
            }
        }

        if (emojiPopup != null && emojiPopup.isShowing()) {
            int newHeight = isWidthGreater ? keyboardHeightLand : keyboardHeight;
            final WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) emojiPopup.getContentView().getLayoutParams();
            FileLog.e("tmessages", "update emoji height to = " + newHeight);
            if (layoutParams.width != AndroidUtilities.displaySize.x || layoutParams.height != newHeight) {
                layoutParams.width = AndroidUtilities.displaySize.x;
                layoutParams.height = newHeight;
                WindowManager wm = (WindowManager) ApplicationLoader.applicationContext.getSystemService(Activity.WINDOW_SERVICE);
                if (wm != null) {
                    wm.updateViewLayout(emojiPopup.getContentView(), layoutParams);
                    if (!keyboardVisible) {
                        if (sizeNotifierLayout != null) {
                            sizeNotifierLayout.setPadding(0, 0, 0, layoutParams.height);
                            sizeNotifierLayout.requestLayout();
                            onWindowSizeChanged(sizeNotifierLayout.getHeight() - sizeNotifierLayout.getPaddingBottom());
                        }
                    }
                }
            }
        }

        boolean oldValue = keyboardVisible;
        keyboardVisible = height > 0;
        if (keyboardVisible && (sizeNotifierLayout.getPaddingBottom() > 0 || keyboardTransitionState == 1)) {
            setKeyboardTransitionState(1);
        } else if (keyboardTransitionState != 2 && !keyboardVisible && keyboardVisible != oldValue && emojiPopup != null && emojiPopup.isShowing()) {
            showEmojiPopup(false, true);
        }
        if (keyboardTransitionState == 0) {
            onWindowSizeChanged(sizeNotifierLayout.getHeight() - sizeNotifierLayout.getPaddingBottom());
        }
    }

    public int getEmojiHeight() {
        if (AndroidUtilities.displaySize.x > AndroidUtilities.displaySize.y) {
            return keyboardHeightLand;
        } else {
            return keyboardHeight;
        }
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.emojiDidLoaded) {
            if (emojiView != null) {
                emojiView.invalidateViews();
            }
        } else if (id == NotificationCenter.closeChats) {
            if (messageEditText != null && messageEditText.isFocused()) {
                AndroidUtilities.hideKeyboard(messageEditText);
            }
        } else if (id == NotificationCenter.hideEmojiKeyboard) {
            hideEmojiPopup();
        }
    }
}
