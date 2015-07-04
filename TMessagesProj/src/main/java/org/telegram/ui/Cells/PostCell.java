/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.MotionEvent;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.Emoji;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.PostsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.dto.Image;
import org.telegram.messenger.object.PostObject;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.utils.StringUtils;

public class PostCell extends BaseCell {

    private static TextPaint namePaint;
    private static TextPaint nameEncryptedPaint;
    private static TextPaint nameUnknownPaint;
    private static TextPaint messagePaint;
    private static TextPaint messagePrintingPaint;
    private static TextPaint timePaint;
    private static TextPaint countPaint;

    //Text

    private int textTop = AndroidUtilities.dp(65);
    private int textLeft;

    private static TextPaint textPaint;

    private static int fontSize = AndroidUtilities.dp(16);

    private PostObject.TextLayoutBlock block;

    //Text
    private static Drawable checkDrawable;
    private static Drawable halfCheckDrawable;
    private static Drawable clockDrawable;
    private static Drawable errorDrawable;
    private static Drawable lockDrawable;
    private static Drawable countDrawable;
    private static Drawable groupDrawable;
    private static Drawable broadcastDrawable;
    private static Drawable muteDrawable;

    private static Paint linePaint;
    private static Paint backPaint;


    private String currentPostId;
    private long lastMessageDate;
    private PostObject postObject;
    private int index;
    private boolean isServerOnly;

    private ImageReceiver avatarImage;
    private AvatarDrawable avatarDrawable;


    //IMmge
    private int photoWidth;
    private int photoHeight;
    private AvatarDrawable imageDrawable;
    private ImageReceiver photoImage;
    //
    protected int backgroundWidth = 100;
    //
    private CharSequence lastPrintString = null;

    public boolean useSeparator = false;

    private int nameLeft;
    private StaticLayout nameLayout;
    private boolean drawNameLock;
    private boolean drawNameGroup;
    private boolean drawNameBroadcast;
    private int nameMuteLeft;
    private int nameLockLeft;
    private int nameLockTop;

    private int timeLeft;
    private int timeTop = AndroidUtilities.dp(17);
    private StaticLayout timeLayout;

    private boolean drawCheck1;
    private boolean drawCheck2;
    private boolean drawClock;
    private int checkDrawLeft;
    private int checkDrawTop = AndroidUtilities.dp(18);
    private int halfCheckDrawLeft;

    private int messageTop = AndroidUtilities.dp(40);
    private int messageLeft;
    private StaticLayout messageLayout;

    private boolean drawError;
    private int errorTop = AndroidUtilities.dp(39);
    private int errorLeft;

    private boolean drawCount;
    private int countTop = AndroidUtilities.dp(39);
    private int countLeft;
    private int countWidth;
    private int avatarLeft;
    private StaticLayout countLayout;

    private int avatarTop = AndroidUtilities.dp(10);

    private boolean isSelected;

    public PostCell(Context context) {
        super(context);

        if (namePaint == null) {
            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(AndroidUtilities.dp(17));
            namePaint.setColor(0xff212121);
            namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            nameEncryptedPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            nameEncryptedPaint.setTextSize(AndroidUtilities.dp(17));
            nameEncryptedPaint.setColor(0xff00a60e);
            nameEncryptedPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            nameUnknownPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            nameUnknownPaint.setTextSize(AndroidUtilities.dp(17));
            nameUnknownPaint.setColor(0xff4d83b3);
            nameUnknownPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            messagePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            messagePaint.setTextSize(AndroidUtilities.dp(16));
            messagePaint.setColor(0xff8f8f8f);
            messagePaint.linkColor = 0xff8f8f8f;

            linePaint = new Paint();
            linePaint.setColor(0xffdcdcdc);

            backPaint = new Paint();
            backPaint.setColor(0x0f000000);

            messagePrintingPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            messagePrintingPaint.setTextSize(AndroidUtilities.dp(16));
            messagePrintingPaint.setColor(0xff4d83b3);

            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(AndroidUtilities.dp(13));
            timePaint.setColor(0xff999999);

            countPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            countPaint.setTextSize(AndroidUtilities.dp(13));
            countPaint.setColor(0xffffffff);
            countPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            lockDrawable = getResources().getDrawable(R.drawable.list_secret);
            checkDrawable = getResources().getDrawable(R.drawable.dialogs_check);
            halfCheckDrawable = getResources().getDrawable(R.drawable.dialogs_halfcheck);
            clockDrawable = getResources().getDrawable(R.drawable.msg_clock);
            errorDrawable = getResources().getDrawable(R.drawable.dialogs_warning);
            countDrawable = getResources().getDrawable(R.drawable.dialogs_badge);
            groupDrawable = getResources().getDrawable(R.drawable.list_group);
            broadcastDrawable = getResources().getDrawable(R.drawable.list_broadcast);
            muteDrawable = getResources().getDrawable(R.drawable.mute_grey);


            //Text

            textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(0xff000000);
            textPaint.linkColor = 0xff316f9f;
//                textPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            textPaint.setTextSize(fontSize);

//            textPaint.setTextSize(AndroidUtilities.dp(MessagesController.getInstance().fontSize));

            //Text
        }

        setBackgroundResource(R.drawable.list_selector);

        avatarImage = new ImageReceiver(this);
        avatarImage.setRoundRadius(AndroidUtilities.dp(26));
        avatarDrawable = new AvatarDrawable();

        //PHOTO
        photoImage = new ImageReceiver(this);
        imageDrawable = new AvatarDrawable();
    }

    //TODO. I have 2 method for setting posts. setPostObject and setPost.
    public void setPostObject(PostObject postObject, int i, boolean server) {
        //TODO I should store id or object. And retrieve from Controller by id.
        currentPostId = postObject.getId();
        this.postObject = postObject;
        index = i;
        isServerOnly = server;
        update(0);
    }

    public void setPost(String postId, PostObject postObject, long date) {
        currentPostId = postId;
        this.postObject = postObject;
        lastMessageDate = date;
//        lastUnreadState = postObject != null && postObject.isUnread();
//        if (this.postObject != null) {
//            lastSendState = this.postObject.getCreatedDate();
//        }
        update(0);
    }

    public String getPostId() {
        return currentPostId;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        avatarImage.onDetachedFromWindow();
        photoImage.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        avatarImage.onAttachedToWindow();
        photoImage.onAttachedToWindow();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(72) + (useSeparator ? 1 : 0));
        int textHeight = 0;
        if (this.block != null && this.block.textLayout != null) {
            textHeight = this.block.textLayout.getHeight();
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), photoHeight + AndroidUtilities.dp(82) + textHeight + (useSeparator ? 1 : 0));

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (StringUtils.isEmpty(currentPostId)) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        if (changed) {
            buildLayout();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //TODO use  backgroundWidth to detect press event
        if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                getBackground().setHotspot(event.getX(), event.getY());
            }
        }
        return super.onTouchEvent(event);
    }

    public void buildLayout() {
        String nameString = "";
        String timeString = "";
        String countString = null;
        CharSequence messageString = "";
        TextPaint currentNamePaint = namePaint;
        TextPaint currentMessagePaint = messagePaint;
        boolean checkMessage = true;

        drawNameGroup = false;
        drawNameBroadcast = false;
        drawNameLock = false;


//        if (chat != null) {
//            if (chat.id < 0) {
//                drawNameBroadcast = true;
//                nameLockTop = AndroidUtilities.dp(16.5f);
//            } else {
//                drawNameGroup = true;
//                nameLockTop = AndroidUtilities.dp(17.5f);
//            }
//
//            if (!LocaleController.isRTL) {
//                nameLockLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
//                nameLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline + 4) + (drawNameGroup ? groupDrawable.getIntrinsicWidth() : broadcastDrawable.getIntrinsicWidth());
//            } else {
//                nameLockLeft = getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline) - (drawNameGroup ? groupDrawable.getIntrinsicWidth() : broadcastDrawable.getIntrinsicWidth());
//                nameLeft = AndroidUtilities.dp(14);
//            }
//        } else {
        if (!LocaleController.isRTL) {
            nameLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
        } else {
            nameLeft = AndroidUtilities.dp(14);
        }
//        }


        if (postObject == null) {
            lastPrintString = messageString;
            currentMessagePaint = messagePrintingPaint;
            if (lastMessageDate != 0) {
                timeString = LocaleController.stringForMessageListDate(lastMessageDate);
            }
            drawCheck1 = false;
            drawCheck2 = false;
            drawClock = false;
            drawCount = false;
            drawError = false;
        } else {

            if (lastMessageDate != 0) {
                timeString = LocaleController.stringForMessageListDate(lastMessageDate);
            } else {
                timeString = LocaleController.stringForMessageListDate(postObject.getCreatedDate());
            }

            lastPrintString = null;

            String name = "";
            name = postObject.getAuthor();


            checkMessage = false;

            currentMessagePaint = messagePrintingPaint;

            String mess = postObject.getMessage();
            if (mess.length() > 150) {
                mess = mess.substring(0, 150);
            }
            mess = mess.replace("\n", " ");

//                messageString = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff4d83b3>%s:</c> <c#ff4d83b3>%s</c>", name, postObject.messageText)), messagePaint.getFontMetricsInt(), AndroidUtilities.dp(20));
            messageString = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff4d83b3>%s:</c> <c#ff808080>%s</c>", name, mess)), messagePaint.getFontMetricsInt(), AndroidUtilities.dp(20));


//            if (unreadCount != 0) {
//                drawCount = true;
//                countString = String.format("%d", unreadCount);
//            } else {
//                drawCount = false;
//            }
            //TODO postObject.isOut(). errorSend.
        }

        int timeWidth = (int) Math.ceil(timePaint.measureText(timeString));
        timeLayout = new StaticLayout(timeString, timePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        if (!LocaleController.isRTL) {
            timeLeft = getMeasuredWidth() - AndroidUtilities.dp(15) - timeWidth;
        } else {
            timeLeft = AndroidUtilities.dp(15);
        }

        //  here was building name string


        if (nameString.length() == 0) {
            nameString = LocaleController.getString("HiddenName", R.string.HiddenName);
        }

        int nameWidth;

        if (!LocaleController.isRTL) {
            nameWidth = getMeasuredWidth() - nameLeft - AndroidUtilities.dp(14) - timeWidth;
        } else {
            nameWidth = getMeasuredWidth() - nameLeft - AndroidUtilities.dp(AndroidUtilities.leftBaseline) - timeWidth;
            nameLeft += timeWidth;
        }
        if (drawNameLock) {
            nameWidth -= AndroidUtilities.dp(4) + lockDrawable.getIntrinsicWidth();
        } else if (drawNameGroup) {
            nameWidth -= AndroidUtilities.dp(4) + groupDrawable.getIntrinsicWidth();
        } else if (drawNameBroadcast) {
            nameWidth -= AndroidUtilities.dp(4) + broadcastDrawable.getIntrinsicWidth();
        }
        if (drawClock) {
            int w = clockDrawable.getIntrinsicWidth() + AndroidUtilities.dp(5);
            nameWidth -= w;
            if (!LocaleController.isRTL) {
                checkDrawLeft = timeLeft - w;
            } else {
                checkDrawLeft = timeLeft + timeWidth + AndroidUtilities.dp(5);
                nameLeft += w;
            }
        } else if (drawCheck2) {
            int w = checkDrawable.getIntrinsicWidth() + AndroidUtilities.dp(5);
            nameWidth -= w;
            if (drawCheck1) {
                nameWidth -= halfCheckDrawable.getIntrinsicWidth() - AndroidUtilities.dp(8);
                if (!LocaleController.isRTL) {
                    halfCheckDrawLeft = timeLeft - w;
                    checkDrawLeft = halfCheckDrawLeft - AndroidUtilities.dp(5.5f);
                } else {
                    checkDrawLeft = timeLeft + timeWidth + AndroidUtilities.dp(5);
                    halfCheckDrawLeft = checkDrawLeft + AndroidUtilities.dp(5.5f);
                    nameLeft += w + halfCheckDrawable.getIntrinsicWidth() - AndroidUtilities.dp(8);
                }
            } else {
                if (!LocaleController.isRTL) {
                    checkDrawLeft = timeLeft - w;
                } else {
                    checkDrawLeft = timeLeft + timeWidth + AndroidUtilities.dp(5);
                    nameLeft += w;
                }
            }
        }


        nameWidth = Math.max(AndroidUtilities.dp(12), nameWidth);
        CharSequence nameStringFinal = TextUtils.ellipsize(nameString.replace("\n", " "), currentNamePaint, nameWidth - AndroidUtilities.dp(12), TextUtils.TruncateAt.END);
        try {
            nameLayout = new StaticLayout(nameStringFinal, currentNamePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        int messageWidth = getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline + 16);

        if (!LocaleController.isRTL) {
            messageLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
            avatarLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 13 : 9);
            textLeft = AndroidUtilities.dp(9);
        } else {
            messageLeft = AndroidUtilities.dp(16);
            avatarLeft = getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.isTablet() ? 65 : 61);
            textLeft = getMeasuredWidth() - AndroidUtilities.dp(61);
        }
        avatarImage.setImageCoords(avatarLeft, avatarTop, AndroidUtilities.dp(52), AndroidUtilities.dp(52));
        if (drawError) {
            int w = errorDrawable.getIntrinsicWidth() + AndroidUtilities.dp(8);
            messageWidth -= w;
            if (!LocaleController.isRTL) {
                errorLeft = getMeasuredWidth() - errorDrawable.getIntrinsicWidth() - AndroidUtilities.dp(11);
            } else {
                errorLeft = AndroidUtilities.dp(11);
                messageLeft += w;
            }
        } else if (countString != null) {
            countWidth = Math.max(AndroidUtilities.dp(12), (int) Math.ceil(countPaint.measureText(countString)));
            countLayout = new StaticLayout(countString, countPaint, countWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
            int w = countWidth + AndroidUtilities.dp(18);
            messageWidth -= w;
            if (!LocaleController.isRTL) {
                countLeft = getMeasuredWidth() - countWidth - AndroidUtilities.dp(19);
            } else {
                countLeft = AndroidUtilities.dp(19);
                messageLeft += w;
            }
            drawCount = true;
        } else {
            drawCount = false;
        }

        if (checkMessage) {
            if (messageString == null) {
                messageString = "";
            }
            String mess = messageString.toString();
            if (mess.length() > 150) {
                mess = mess.substring(0, 150);
            }
            mess = mess.replace("\n", " ");
            messageString = Emoji.replaceEmoji(mess, messagePaint.getFontMetricsInt(), AndroidUtilities.dp(17));
        }
        messageWidth = Math.max(AndroidUtilities.dp(12), messageWidth);
        CharSequence messageStringFinal = TextUtils.ellipsize(messageString, currentMessagePaint, messageWidth - AndroidUtilities.dp(12), TextUtils.TruncateAt.END);
        try {
            messageLayout = new StaticLayout(messageStringFinal, currentMessagePaint, messageWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        double widthpx;
        float left;
        if (LocaleController.isRTL) {
            if (nameLayout != null && nameLayout.getLineCount() > 0) {
                left = nameLayout.getLineLeft(0);
                widthpx = Math.ceil(nameLayout.getLineWidth(0));
//                if (dialogMuted) {
//                    nameMuteLeft = (int) (nameLeft + (nameWidth - widthpx) - AndroidUtilities.dp(6) - muteDrawable.getIntrinsicWidth());
//                }
                if (left == 0) {
                    if (widthpx < nameWidth) {
                        nameLeft += (nameWidth - widthpx);
                    }
                }
            }
            if (messageLayout != null && messageLayout.getLineCount() > 0) {
                left = messageLayout.getLineLeft(0);
                if (left == 0) {
                    widthpx = Math.ceil(messageLayout.getLineWidth(0));
                    if (widthpx < messageWidth) {
                        messageLeft += (messageWidth - widthpx);
                    }
                }
            }
        } else {
            if (nameLayout != null && nameLayout.getLineCount() > 0) {
                left = nameLayout.getLineRight(0);
                if (left == nameWidth) {
                    widthpx = Math.ceil(nameLayout.getLineWidth(0));
                    if (widthpx < nameWidth) {
                        nameLeft -= (nameWidth - widthpx);
                    }
                }
//                if (dialogMuted) {
//                    nameMuteLeft = (int) (nameLeft + left + AndroidUtilities.dp(6));
//                }
            }
            if (messageLayout != null && messageLayout.getLineCount() > 0) {
                left = messageLayout.getLineRight(0);
                if (left == messageWidth) {
                    widthpx = Math.ceil(messageLayout.getLineWidth(0));
                    if (widthpx < messageWidth) {
                        messageLeft -= (messageWidth - widthpx);
                    }
                }
            }
        }


        //TEXT

        // TODO It should be before creating Cell. somewhere in generating entities. Because it is static layout. in Cell we just find the position. !!
        generateTextLayout(this.postObject);


        /// TEXT
        //Photo

        if (this.postObject != null) {

            if (AndroidUtilities.isTablet()) {
                photoWidth = (int) (AndroidUtilities.getMinTabletSide() * 1.0f); //0.7f
            } else {
                photoWidth = (int) (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) * 1.0f); // here
            }


            //TODO my way of calculating imageWidth
//            int imageMaxWidth = getMeasuredWidth() - AndroidUtilities.dp(12);
//
//            int imageMaxWidthOpt;
//            if (AndroidUtilities.isTablet()) {
//                imageMaxWidthOpt = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(12);
//            } else {
//                imageMaxWidthOpt = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(12);
//            }
//
//            imageMaxWidth = Math.max(imageMaxWidthOpt, imageMaxWidth);
            ///

//            photoWidth = imageMaxWidth;


            photoHeight = photoWidth + AndroidUtilities.dp(100);

            if (photoWidth > AndroidUtilities.getPhotoSize()) {
                photoWidth = AndroidUtilities.getPhotoSize();
            }
            if (photoHeight > AndroidUtilities.getPhotoSize()) {
                photoHeight = AndroidUtilities.getPhotoSize();
            }


            if (postObject.getPreviewImage() != null) {
                Image previewImage = this.postObject.getPreviewImage();

                float scale = (float) previewImage.getWidth() / (float) photoWidth; // scale calculate

                if (previewImage.getSize() == 0) { // size of the file.
                    previewImage.setSize(-1);
                }

                int w = (int) (previewImage.getWidth() / scale);
                int h = (int) (previewImage.getHeight() / scale);
                if (w == 0) {
                    w = AndroidUtilities.dp(100);
                }

                if (h == 0) {
                    h = AndroidUtilities.dp(100);
                }
                //TODO it is important part to prevent very high and small images!!
//                if (h > photoHeight) {
//                    float scale2 = h;
//                    h = photoHeight;
//                    scale2 /= h;
//                    w = (int) (w / scale2);
//                } else if (h < AndroidUtilities.dp(120)) {
//                    h = AndroidUtilities.dp(120);
//                    float hScale = (float) post.getHeight() / h;
//                    if (post.getWidth() / hScale < photoWidth) {
//                        w = (int) (post.getWidth() / hScale);
//                    }
//                }

                photoWidth = w;
                photoHeight = h;
                backgroundWidth = w + AndroidUtilities.dp(12);

//                photoImage.setImageCoords(avatarLeft, avatarTop + AndroidUtilities.dp(62) + this.block.textLayout.getHeight(), photoWidth, photoHeight);
                photoImage.setImageCoords(0, avatarTop + AndroidUtilities.dp(62) + this.block.textLayout.getHeight(), photoWidth, photoHeight);


                photoImage.setForcePreview(false);
//                photoImage.setImage(post.getFullImage(), null, imageDrawable); // TODO fix it. Create drawable.

                invalidate();
            }
        }

    }


    public void setPostSelected(boolean value) {
        if (isSelected != value) {
            invalidate();
        }
        isSelected = value;
    }

    public void checkCurrentPostIndex() {
        PostObject postObject = null;

        if (index < PostsController.getInstance().postObjects.size()) {
            postObject = PostsController.getInstance().postObjects.get(index);
        }

        if (postObject != null) {
            if (!StringUtils.isEmpty(postObject.getId())
                    && !StringUtils.isEmpty(currentPostId)
                    && !currentPostId.equals(postObject.getId())) {
                currentPostId = postObject.getId();
                update(0);
            }
        }
    }

    public void update(int mask) {

//        PostObject postObject = PostsController.getInstance().postsMap.get(currentPostId);


        if (mask != 0) {
            //TODO different masks. look at examples.
        }


//        TLRPC.FileLocation photo = null;
//        if (user != null) {
//            if (user.photo != null) {
//                photo = user.photo.photo_small;
//            }
//            avatarDrawable.setInfo(user);
//        } else if (chat != null) {
//            if (chat.photo != null) {
//                photo = chat.photo.photo_small;
//            }
//            avatarDrawable.setInfo(chat);
//        }
//        avatarImage.setImage(photo, "50_50", avatarDrawable, null, false);


        //TODO-DELETE
//        if(postObject == null) {
//            return;
//        }
        //

        //TODO null, 0 ??? s ext adn size. int)postObject.getImage().getSize()
        avatarImage.setImage(postObject.getVenuePreviewImageUrl(), null, avatarDrawable, null, 0);

        //Photo
        // TODO null ?
        photoImage.setImage(postObject.getPreviewImageUrl(), null, imageDrawable, null, 0); // TODO fix it. Create drawable.

        //Photo


        if (getMeasuredWidth() != 0 || getMeasuredHeight() != 0) {
            buildLayout();
        } else {
            requestLayout();
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (StringUtils.isEmpty(currentPostId)) {
            return;
        }

        if (isSelected) {
            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), backPaint);
        }

        if (drawNameLock) {
            setDrawableBounds(lockDrawable, nameLockLeft, nameLockTop);
            lockDrawable.draw(canvas);
        } else if (drawNameGroup) {
            setDrawableBounds(groupDrawable, nameLockLeft, nameLockTop);
            groupDrawable.draw(canvas);
        } else if (drawNameBroadcast) {
            setDrawableBounds(broadcastDrawable, nameLockLeft, nameLockTop);
            broadcastDrawable.draw(canvas);
        }

        if (nameLayout != null) {
            canvas.save();
            canvas.translate(nameLeft, AndroidUtilities.dp(13));
            nameLayout.draw(canvas);
            canvas.restore();
        }

        canvas.save();
        canvas.translate(timeLeft, timeTop);
        timeLayout.draw(canvas);
        canvas.restore();

        if (messageLayout != null) {
            canvas.save();
            canvas.translate(messageLeft, messageTop);
            messageLayout.draw(canvas);
            canvas.restore();
        }

        if (drawClock) {
            setDrawableBounds(clockDrawable, checkDrawLeft, checkDrawTop);
            clockDrawable.draw(canvas);
        } else if (drawCheck2) {
            if (drawCheck1) {
                setDrawableBounds(halfCheckDrawable, halfCheckDrawLeft, checkDrawTop);
                halfCheckDrawable.draw(canvas);
                setDrawableBounds(checkDrawable, checkDrawLeft, checkDrawTop);
                checkDrawable.draw(canvas);
            } else {
                setDrawableBounds(checkDrawable, checkDrawLeft, checkDrawTop);
                checkDrawable.draw(canvas);
            }
        }


        if (drawError) {
            setDrawableBounds(errorDrawable, errorLeft, errorTop);
            errorDrawable.draw(canvas);
        } else if (drawCount) {
            setDrawableBounds(countDrawable, countLeft - AndroidUtilities.dp(5.5f), countTop, countWidth + AndroidUtilities.dp(11), countDrawable.getIntrinsicHeight());
            countDrawable.draw(canvas);
            canvas.save();
            canvas.translate(countLeft, countTop + AndroidUtilities.dp(4));
            countLayout.draw(canvas);
            canvas.restore();
        }

        if (useSeparator) {
            if (LocaleController.isRTL) {
                canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, linePaint);
            } else {
                canvas.drawLine(AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, linePaint);
            }
        }

        avatarImage.draw(canvas);
        //Photo
        photoImage.draw(canvas);


        // TEXT

        canvas.save();
        canvas.translate(textLeft, textTop);
        try {
            //TODO draw text message
            this.block.textLayout.draw(canvas);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        canvas.restore();
    }


    //TODO utils method ?
    // TODO It should be before creating Cell. somewhere in generating entities. Because it is static layout. in Cell we just find the position. !!
    private void generateTextLayout(PostObject postObject) {
        if (postObject == null || postObject.getMessage() == null || postObject.getMessage().length() == 0) {
            return;
        }

        CharSequence messageText = postObject.getMessage();

        messageText = Emoji.replaceEmoji(messageText, textPaint.getFontMetricsInt(), AndroidUtilities.dp(20));

        if (messageText instanceof Spannable && containsUrls(postObject.getMessage())) {
            if (postObject.getMessage().length() < 100) {
                Linkify.addLinks((Spannable) messageText, Linkify.WEB_URLS | Linkify.PHONE_NUMBERS);
            } else {
                Linkify.addLinks((Spannable) messageText, Linkify.WEB_URLS);
            }
        }


        int textMaxWidth;

        if (!LocaleController.isRTL) {
            textMaxWidth = getMeasuredWidth() - avatarLeft - AndroidUtilities.dp(14);
        } else {
            textMaxWidth = getMeasuredWidth() - avatarLeft - AndroidUtilities.dp(72);
        }


        int textMaxWidthOpt;
        if (AndroidUtilities.isTablet()) {
            textMaxWidthOpt = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(40);
        } else {
            textMaxWidthOpt = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(40);
        }

        textMaxWidth = Math.max(textMaxWidthOpt, textMaxWidth);

        StaticLayout textLayout = null;

        try {
            textLayout = new StaticLayout(messageText, textPaint, textMaxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
            return;
        }
        this.block = new PostObject.TextLayoutBlock();

        this.block.textLayout = textLayout;
        this.block.textYOffset = 0;
        this.block.charactersOffset = 0;


    }


    //TODO utils method ?
    private boolean containsUrls(String message) {
        if (message == null || message.length() < 3 || message.length() > 1024 * 20) {
            return false;
        }

        boolean containsSomething = false;

        int length = message.length();

        int digitsInRow = 0;
        int schemeSequence = 0;
        int dotSequence = 0;

        char lastChar = 0;

        for (int i = 0; i < length; i++) {
            char c = message.charAt(i);

            if (c >= '0' && c <= '9') {
                digitsInRow++;
                if (digitsInRow >= 6) {
                    return true;
                }
                schemeSequence = 0;
                dotSequence = 0;
            } else if (c == ':') {
                if (schemeSequence == 0) {
                    schemeSequence = 1;
                } else {
                    schemeSequence = 0;
                }
            } else if (c == '/') {
                if (schemeSequence == 2) {
                    return true;
                }
                if (schemeSequence == 1) {
                    schemeSequence++;
                } else {
                    schemeSequence = 0;
                }
            } else if (c == '.') {
                if (dotSequence == 0 && lastChar != ' ') {
                    dotSequence++;
                } else {
                    dotSequence = 0;
                }
            } else if (c != ' ' && lastChar == '.' && dotSequence == 1) {
                return true;
            }
            lastChar = c;
        }
        return false;
    }
}
