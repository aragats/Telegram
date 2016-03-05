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
import android.location.Location;
import android.os.Build;
import android.text.Layout;
import android.text.Spannable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.Emoji;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.PostsController;
import org.telegram.android.location.LocationManagerHelper;
import org.telegram.messenger.FileLog;

import ru.aragats.wgo.dto.Coordinates;
import ru.aragats.wgo.dto.Image;
import ru.aragats.wgo.dto.Post;

import org.telegram.messenger.object.TextLayoutBlock;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.URLSpanNoUnderline;
import org.telegram.utils.StringUtils;

import java.util.Locale;

import ru.aragats.wgo.R;

public class PostCell extends BaseCell {


    public static interface PostCellDelegate {
        public abstract void didClickedImage(PostCell cell);

        public abstract void didClickedVenue(PostCell cell);

        void didPressUrl(String url);


        public abstract void didPressedOther(PostCell cell);

//        public abstract void didPressedUserAvatar(PostCell cell, UserObject userObject);

        public abstract void didPressedCancelSendButton(PostCell cell);

        public abstract void didLongPressed(PostCell cell);

        public abstract boolean canPerformActions();
    }

    private boolean imagePressed;
    private boolean venuePressed;
    private boolean textPressed;
    //Paint. set fond size for them
    private static TextPaint namePaint;
    private static TextPaint addressPaint;
    private static TextPaint messagePrintingPaint;
    private static TextPaint timePaint;

    //Text

    //TODO delete since I have             textLayoutBlock.textXOffset = textLeft;
    private int textTop = AndroidUtilities.dp(65);
    private int textLeft;

    private static TextPaint textPaint;

    //Text fond of text.
    private static int fontSize = AndroidUtilities.dp(14);
//    private static int fontSize = AndroidUtilities.dp(16);

    private TextLayoutBlock textLayoutBlock;

    //Text
    private static Drawable errorDrawable;

    private static Paint linePaint;
    private static Paint backPaint;


    private String currentPostId;
    private long lastMessageDate;
    private Post post;
    private int index;

    private ImageReceiver avatarImage;
    private AvatarDrawable avatarDrawable;


    //Image
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
    private int nameTop = AndroidUtilities.dp(15);
    private StaticLayout nameLayout;

    private int timeLeft;
    private int timeTop = AndroidUtilities.dp(15);
    //    private int timeTop = AndroidUtilities.dp(17);
    private StaticLayout timeLayout;


    private int addressTop = AndroidUtilities.dp(34);
    //    private int addressTop = AndroidUtilities.dp(40);
    private int addressLeft;
    private StaticLayout addressLayout;

    private boolean drawError;
    private int errorTop = AndroidUtilities.dp(39);
    private int errorLeft;

    private int avatarLeft;

    private int avatarTop = AndroidUtilities.dp(10);
    private int avatarSize = AndroidUtilities.dp(46);

    private boolean isSelected;


    private boolean isPressed = false;
    private ClickableSpan pressedLink;
    //    private boolean linkPreviewPressed;
    protected MyPath urlPath = new MyPath();
    protected static Paint urlPaint;
    private int linkBlockNum;


    //Photo
    private PostCellDelegate delegate;

    public PostCell(Context context) {
        super(context);

        if (namePaint == null) {
            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(AndroidUtilities.dp(14));
            namePaint.setColor(0xff212121);
            namePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            addressPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            addressPaint.setTextSize(AndroidUtilities.dp(14));
            addressPaint.setColor(0xff8f8f8f);
            addressPaint.linkColor = 0xff8f8f8f;

            linePaint = new Paint();
            linePaint.setColor(0xffdcdcdc);

            backPaint = new Paint();
            backPaint.setColor(0x0f000000);

            messagePrintingPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            messagePrintingPaint.setTextSize(AndroidUtilities.dp(14));
            messagePrintingPaint.setColor(0xff4d83b3);

            timePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaint.setTextSize(AndroidUtilities.dp(13));
            timePaint.setColor(0xff999999);

            //TODO deprecate. Use ResourceLoader.
            errorDrawable = getResources().getDrawable(R.drawable.dialogs_warning);


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

    public Post getPost() {
        return post;
    }

    //TODO. I have 2 method for setting posts. setPost and setPost.
    public void setPostObject(Post post, int i) {
        //TODO I should store id or object. And retrieve from Controller by id.
        currentPostId = post.getId();
        this.post = post;
        index = i;
        //TODO I use it to calculate build layout before onMeasure will be invoked. And by this way, correct layout
        buildLayout();
        update(0);
    }

    public void setPost(String postId, Post post, long date) {
        currentPostId = postId;
        this.post = post;
        lastMessageDate = date;
//        lastUnreadState = post != null && post.isUnread();
//        if (this.post != null) {
//            lastSendState = this.post.getCreatedDate();
//        }
        //TODO I use it to calculate build layout before onMeasure will be invoked. And by this way, correct layout
        buildLayout();
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

    //TODO setMeasure. size of post.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(72) + (useSeparator ? 1 : 0));
        int textHeight = 0;
        if (this.textLayoutBlock != null && this.textLayoutBlock.textLayout != null) {
            textHeight = this.textLayoutBlock.textLayout.getHeight();
        }
        int marginHeight = AndroidUtilities.dp(82);
        if (StringUtils.isEmpty(post.getText())) {
            textHeight = 0;
            marginHeight = AndroidUtilities.dp(72); //0 was
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), photoHeight + marginHeight + textHeight + (useSeparator ? 1 : 0));

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
//        onTouchEventMessage(event);
        //TODO use  backgroundWidth to detect press event
        if (Build.VERSION.SDK_INT >= 21 && getBackground() != null) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                getBackground().setHotspot(event.getX(), event.getY());
            }
        }

        float x = event.getX();
        float y = event.getY();

        boolean result = false;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (delegate == null || delegate.canPerformActions()) {

                if (x >= photoImage.getImageX() && x <= photoImage.getImageX() + backgroundWidth && y >= photoImage.getImageY() && y <= photoImage.getImageY() + photoImage.getImageHeight()) {
                    imagePressed = true;
                    venuePressed = false;
                    textPressed = false;
                    result = true;
                    // click on part upper to image // address, name of the place and venue photo.
                } else if (x >= 0 && x <= getMeasuredWidth() && y >= 0 && y < photoImage.getImageY()) {
                    venuePressed = true;
                    imagePressed = false;
                    textPressed = false;
                    result = true;
                } else {
                    // press link in text
                    venuePressed = false;
                    imagePressed = false;
                    result = handleTextDownClick(x, y);
                }
//                photoImage.setImageCoords(0, avatarTop + AndroidUtilities.dp(62), photoWidth, photoHeight);


//                else if (x >= addressLeft && x <= addressLeft + addressLayout.getWidth() && y >= addressTop && y <= addressTop + addressLayout.getHeight()) {
//                    venuePressed = true;
//                    imagePressed = false;
//                    result = true;
//                }

//                else {
//                    venuePressed = true;
//                    imagePressed = false;
//                    result = true;
//                }
            }

            if (result) {
                startCheckLongPress();
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            playSoundEffect(SoundEffectConstants.CLICK);
            if (imagePressed) {
                imagePressed = false;
                didClickedImage();
            } else if (venuePressed) {
                venuePressed = false;
                didClickedVenue();
            } else if (textPressed) {
                textPressed = false;
                didClickedText();
            }
            invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            resetPressedLink();
        }

        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
//        return super.onTouchEvent(event);
    }

    public void buildLayout() {
        String nameString = "";
        String timeString = "";
        CharSequence addressString = "";
        TextPaint currentNamePaint = namePaint;
        TextPaint currentAddressPaint = addressPaint;
        boolean checkMessage = true;

        String address = "";

        if (!LocaleController.isRTL) {
            nameLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
        } else {
            nameLeft = AndroidUtilities.dp(14);
        }

        //TODO could be NPE postCoordinates !!!

        //TODO change checking FIX it. I need validate post and then do what I need. MAny possibilities for NPE.
        if (post == null) {
            lastPrintString = addressString;
            currentAddressPaint = messagePrintingPaint;
            if (lastMessageDate != 0) {
                timeString = LocaleController.stringForMessageListDate(lastMessageDate);
            }
            drawError = false;
        } else {

            if (lastMessageDate != 0) {
                timeString = LocaleController.stringForMessageListDate(lastMessageDate);
            } else {
//                timeString = LocaleController.formatString("formatDateAtTime", R.string.formatDateAtTime, LocaleController.formatterYear.format(new Date(post.getCreatedDate())), LocaleController.formatterDay.format(new Date(post.getCreatedDate())));
                timeString = LocaleController.stringForMessageListDate(post.getCreatedDate() / 1000);
            }

            lastPrintString = null;

            //TODO could be NPE postCoordinates !!!
            address = post.getVenue() != null ? post.getVenue().getAddress() :
                    String.format(Locale.US, "(%f,%f)",
                            post.getPostCoordinates().getCoordinates().get(1),
                            post.getPostCoordinates().getCoordinates().get(0));

            //TODO-refactoring low performance
//            if (StringUtils.isEmpty(address)) {
//                address = LocationManagerHelper.getInstance().getAddress(getContext(), post.getPostCoordinates().getCoordinates(), "");
//                post.getVenue().setAddress(address);
//            }


            checkMessage = false;

            currentAddressPaint = messagePrintingPaint;

//            String distance = "n km";
//            String distance = LocaleController.formatString("AccurateTo", R.string.AccurateTo, LocaleController.formatPluralString("Meters", (int) gpsLocation.getAccuracy()));
//            String distance = LocaleController.formatString("AccurateTo", R.string.AccurateTo, LocaleController.formatPluralString("Meters", (int) PostsController.getInstance().getCurrentLocation().getAccuracy()));

            String distanceStr = "";
            Location userLocation = LocationManagerHelper.getInstance().getLastSavedLocation();
            if (userLocation != null && post.getPostCoordinates() != null) {
                Coordinates coordinates = post.getPostCoordinates();
                Location location = new Location("network");
                location.setLongitude(coordinates.getCoordinates().get(0));
                location.setLatitude(coordinates.getCoordinates().get(1));
                float distance = location.distanceTo(userLocation);
                if (distance < 1000) {
                    distanceStr = String.format("%d %s", (int) (distance), LocaleController.getString("MetersAway", R.string.MetersAway));
                } else {
                    distanceStr = String.format("%.2f %s", distance / 1000.0f, LocaleController.getString("KMetersAway", R.string.KMetersAway));
                }
            }

            if (distanceStr.length() > 150) {
                distanceStr = distanceStr.substring(0, 150);
            }
            distanceStr = distanceStr.replace("\n", " ");

            //addressString was address String and it was like address: distance
//                addressString = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff4d83b3>%s:</c> <c#ff4d83b3>%s</c>", address, post.messageText)), addressPaint.getFontMetricsInt(), AndroidUtilities.dp(20));
            // address: distance
            //addressString was address String and it was like address: distance

            nameString = post.getVenue() != null ? post.getVenue().getName() : "";

            // if nameString is empty then we should not use address in the addressString, but only coordinates, because in this case address goes to name field.
            if (StringUtils.isEmpty(nameString)) {
                addressString = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff808080>%s</c>", distanceStr)), addressPaint.getFontMetricsInt(), AndroidUtilities.dp(20));
            } else {
                addressString = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff4d83b3>%s:</c> <c#ff808080>%s</c>", address, distanceStr)), addressPaint.getFontMetricsInt(), AndroidUtilities.dp(20));

            }
//            addressString = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff808080>%s</c>", distanceStr)), addressPaint.getFontMetricsInt(), AndroidUtilities.dp(20));


//            if (unreadCount != 0) {
//                drawCount = true;
//                countString = String.format("%d", unreadCount);
//            } else {
//                drawCount = false;
//            }
            //TODO post.isOut(). errorSend.
        }

        int timeWidth = (int) Math.ceil(timePaint.measureText(timeString));
        timeLayout = new StaticLayout(timeString, timePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        if (!LocaleController.isRTL) {
            timeLeft = getMeasuredWidth() - AndroidUtilities.dp(15) - timeWidth;
        } else {
            timeLeft = AndroidUtilities.dp(15);
        }

        //  here was building name string


        nameString = post.getVenue() != null ? post.getVenue().getName() : "";
        if (StringUtils.isEmpty(nameString)) {
            nameString = address;
//            nameString = LocaleController.getString("HiddenName", R.string.HiddenName);
//            nameString = LocaleController.getString("Place", R.string.Place);
        }

        int nameWidth;

        if (!LocaleController.isRTL) {
            nameWidth = getMeasuredWidth() - nameLeft - AndroidUtilities.dp(14) - timeWidth;
        } else {
            nameWidth = getMeasuredWidth() - nameLeft - AndroidUtilities.dp(AndroidUtilities.leftBaseline) - timeWidth;
            nameLeft += timeWidth;
        }


//        nameStringFinal = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff4d83b3>%s:</c> <c#ff808080>%s</c>", nameString, address)), distancePaint.getFontMetricsInt(), AndroidUtilities.dp(20));

        nameWidth = Math.max(AndroidUtilities.dp(12), nameWidth);
//        CharSequence nameStringFinal = null;
//        if (StringUtils.isEmpty(nameString)) {
////            nameStringFinal = Emoji.replaceEmoji(AndroidUtilities.replaceTags(address), distancePaint.getFontMetricsInt(), AndroidUtilities.dp(20));
//            nameStringFinal = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff4d83b3>%s</c>", address)), distancePaint.getFontMetricsInt(), AndroidUtilities.dp(20));
//
//        } else {
//            nameStringFinal = Emoji.replaceEmoji(AndroidUtilities.replaceTags(String.format("<c#ff4d83b3>%s:</c> <c#ff808080>%s</c>", nameString, address)), distancePaint.getFontMetricsInt(), AndroidUtilities.dp(20));
//        }

        CharSequence nameStringFinal = TextUtils.ellipsize(nameString.replace("\n", " "), currentNamePaint, nameWidth - AndroidUtilities.dp(12), TextUtils.TruncateAt.END);

        try {
            nameLayout = new StaticLayout(nameStringFinal, currentNamePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        int addressWidth = getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline + 16);

        if (!LocaleController.isRTL) {
            addressLeft = AndroidUtilities.dp(AndroidUtilities.leftBaseline);
            avatarLeft = AndroidUtilities.dp(AndroidUtilities.isTablet() ? 13 : 9);
            textLeft = AndroidUtilities.dp(9);
        } else {
            addressLeft = AndroidUtilities.dp(16);
            avatarLeft = getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.isTablet() ? 65 : 61);
            textLeft = getMeasuredWidth() - AndroidUtilities.dp(61);
        }
//        avatarImage.setImageCoords(avatarLeft, avatarTop, AndroidUtilities.dp(52), AndroidUtilities.dp(52));
        avatarImage.setImageCoords(avatarLeft, avatarTop, avatarSize, avatarSize);
        if (drawError) {
            int w = errorDrawable.getIntrinsicWidth() + AndroidUtilities.dp(8);
            addressWidth -= w;
            if (!LocaleController.isRTL) {
                errorLeft = getMeasuredWidth() - errorDrawable.getIntrinsicWidth() - AndroidUtilities.dp(11);
            } else {
                errorLeft = AndroidUtilities.dp(11);
                addressLeft += w;
            }
        }

        if (checkMessage) {
            if (addressString == null) {
                addressString = "";
            }
            address = addressString.toString();
            if (address.length() > 150) {
                address = address.substring(0, 150);
            }
            address = address.replace("\n", " ");
            addressString = Emoji.replaceEmoji(address, addressPaint.getFontMetricsInt(), AndroidUtilities.dp(17));
        }
        addressWidth = Math.max(AndroidUtilities.dp(12), addressWidth);
        CharSequence addressStringFinal = TextUtils.ellipsize(addressString, currentAddressPaint, addressWidth - AndroidUtilities.dp(12), TextUtils.TruncateAt.END);
        try {
            addressLayout = new StaticLayout(addressStringFinal, currentAddressPaint, addressWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
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
            if (addressLayout != null && addressLayout.getLineCount() > 0) {
                left = addressLayout.getLineLeft(0);
                if (left == 0) {
                    widthpx = Math.ceil(addressLayout.getLineWidth(0));
                    if (widthpx < addressWidth) {
                        addressLeft += (addressWidth - widthpx);
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
            if (addressLayout != null && addressLayout.getLineCount() > 0) {
                left = addressLayout.getLineRight(0);
                if (left == addressWidth) {
                    widthpx = Math.ceil(addressLayout.getLineWidth(0));
                    if (widthpx < addressWidth) {
                        addressLeft -= (addressWidth - widthpx);
                    }
                }
            }
        }


        //TEXT

        // TODO It should be before creating Cell. somewhere in generating entities. Because it is static layout. in Cell we just find the position. !!
        generateTextLayout(this.post);


        /// TEXT
        //Photo

        if (this.post != null) {

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


            if (post.getPreviewImage() != null) {
                Image previewImage = this.post.getPreviewImage();

                //TODO could be use in case of over scaling when real photo less then the phone screen.
//                if (photoWidth > previewImage.getWidth()) {
//                    photoWidth = previewImage.getWidth();
//                }
//                if (photoHeight > previewImage.getHeight()) {
//                    photoHeight = previewImage.getHeight();
//                }


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

//                photoImage.setImageCoords(avatarLeft, avatarTop + AndroidUtilities.dp(62) + this.textLayoutBlock.textLayout.getHeight(), photoWidth, photoHeight);
//                photoImage.setImageCoords(0, avatarTop + AndroidUtilities.dp(62) + this.textLayoutBlock.textLayout.getHeight(), photoWidth, photoHeight);
                // orientation of image in the center of the screen.
                int x = (getMeasuredWidth() - photoWidth) / 2;
                photoImage.setImageCoords(x, avatarTop + AndroidUtilities.dp(62), photoWidth, photoHeight);


                photoImage.setForcePreview(false);
//                photoImage.setImage(post.getFullImage(), null, imageDrawable); // TODO fix it. Create drawable.

                invalidate();
            }

            textTop = AndroidUtilities.dp(75) + photoHeight;
        }

    }


    public void setPostSelected(boolean value) {
        if (isSelected != value) {
            invalidate();
        }
        isSelected = value;
    }

    public void checkCurrentPostIndex() {
        Post post = null;

        if (index < PostsController.getInstance().getPosts().size()) {
            post = PostsController.getInstance().getPosts().get(index);
        }

        if (post != null) {
            if (!StringUtils.isEmpty(post.getId())
                    && !StringUtils.isEmpty(currentPostId)
                    && !currentPostId.equals(post.getId())) {
                currentPostId = post.getId();
                update(0);
            }
        }
    }

    public void update(int mask) {

//        PostObject post = PostsController.getInstance().postsMap.get(currentPostId);


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
//        if(post == null) {
//            return;
//        }
        //

        //TODO null, 0 ??? s ext adn size. int)post.getImage().getSize()
//        avatarImage.setImage(R.drawable.pin);
        String url = post.getVenuePreviewImageUrl();
        if (!StringUtils.isEmpty(url)) {
            avatarImage.setImage(post.getVenuePreviewImageUrl(), null, avatarDrawable, null, 0);
//            avatarImage.setBackgroundResource(R.drawable.round_grey);
        } else {
            avatarImage.setImageBitmap(getResources().getDrawable(R.drawable.pin));
//            avatarImage.setBackgroundResource(R.drawable.round_grey);
//            avatarImage.setImageResource(R.drawable.pin);

        }

        //Photo
        //1 original - slow
        // TODO null ?
//        photoImage.setImage(post.getPreviewImageUrl(), null, imageDrawable, null, 0); // TODO fix it. Create drawable.

        //2. from postMediaCell . works faster
        int size = (int) (AndroidUtilities.getPhotoSize() / AndroidUtilities.density);
        photoImage.setImage(post.getPreviewImageUrl(), String.format(Locale.US, "%d_%d", size, size), imageDrawable, null, (int) post.getPreviewImage().getSize()); // TODO fix it. Create drawable.

        //Photo

        //TODO was ?
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
//        ColorDrawable cd = new ColorDrawable(0xFFFFFFFF);
//        Drawable currentBackgroundDrawable = ResourceLoader.backgroundBlue;
//        Drawable currentBackgroundDrawable = getContext().getResources().getDrawable(R.drawable.pin);;
        Drawable currentBackgroundDrawable = getContext().getResources().getDrawable(R.drawable.floating_states);
//        currentBackgroundDrawable = backgroundMediaDrawableOutSelected;


//        setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(9), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - AndroidUtilities.dp(2));
        setDrawableBounds(currentBackgroundDrawable, avatarLeft, avatarTop, avatarImage.getImageWidth(), avatarImage.getImageHeight());

        // set background for message
        currentBackgroundDrawable.draw(canvas);

        if (isSelected) {
            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), backPaint);
        }


        if (nameLayout != null) {
            canvas.save();
            canvas.translate(nameLeft, nameTop);
            nameLayout.draw(canvas);
            canvas.restore();
        }

        canvas.save();
        canvas.translate(timeLeft, timeTop);
        timeLayout.draw(canvas);
        canvas.restore();

        if (addressLayout != null) {
            canvas.save();
            canvas.translate(addressLeft, addressTop);
            addressLayout.draw(canvas);
            canvas.restore();
        }


        if (drawError) {
            setDrawableBounds(errorDrawable, errorLeft, errorTop);
            errorDrawable.draw(canvas);
        }

        if (useSeparator) {
            canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, linePaint);

//            if (LocaleController.isRTL) {
//                canvas.drawLine(0, getMeasuredHeight() - 1, getMeasuredWidth() - AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, linePaint);
//            } else {
//                canvas.drawLine(AndroidUtilities.dp(AndroidUtilities.leftBaseline), getMeasuredHeight() - 1, getMeasuredWidth(), getMeasuredHeight() - 1, linePaint);
//            }
        }

        avatarImage.draw(canvas);
        //Photo
        photoImage.draw(canvas);


        // TEXT

        if (!StringUtils.isEmpty(post.getText())) {
            canvas.save();
            canvas.translate(textLeft, textTop);
            try {
                //TODO draw text message
                this.textLayoutBlock.textLayout.draw(canvas);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            canvas.restore();
        }
    }


    //TODO utils method ?
    // TODO It should be before creating Cell. somewhere in generating entities. Because it is static layout. in Cell we just find the position. !!
    private void generateTextLayout(Post post) {
        if (post == null) {
            return;
        }
        if (post.getText() == null) {
            post.setText("");
        }

        CharSequence messageText = post.getText();

        messageText = Emoji.replaceEmoji(messageText, textPaint.getFontMetricsInt(), AndroidUtilities.dp(20));

        if (messageText instanceof Spannable && containsUrls(post.getText())) {
            if (post.getText().length() < 100) {
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

        this.textLayoutBlock = new TextLayoutBlock();
        this.textLayoutBlock.textLayout = textLayout;
        this.textLayoutBlock.textYOffset = 0;
        this.textLayoutBlock.charactersOffset = 0;


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


    private void didClickedImage() {
        if (this.delegate != null) {
            this.delegate.didClickedImage(this);
        }
    }

    private void didClickedVenue() {
        if (this.delegate != null) {
            this.delegate.didClickedVenue(this);
        }
    }

    private boolean didClickedText() {
        boolean result = false;
        if (pressedLink != null) {
            try {
                if (pressedLink instanceof URLSpanNoUnderline) {
                    String url = ((URLSpanNoUnderline) pressedLink).getURL();
                    if (url.startsWith("@") || url.startsWith("#")) {
                        if (delegate != null) {
                            delegate.didPressUrl(url);
                        }
                    }
                } else {
                    pressedLink.onClick(this);
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            resetPressedLink();
            result = true;
        }
        return result;
    }

    private boolean handleTextDownClick(float x, float y) {
        boolean result = false;
        if (textLayoutBlock != null && textLayoutBlock.textLayout != null && textLayoutBlock.textLayout.getText() != null) {

            StaticLayout textLayout = textLayoutBlock.textLayout;
            CharSequence messageText = textLayoutBlock.textLayout.getText();

            float textX = textLeft;
            float textY = textTop;
            float textWidth = textLayout.getWidth();
            float textHeight = textLayout.getHeight();
            if (x >= textX && y >= textY && x <= textX + textWidth && y <= textY + textHeight) {
                y -= textY;
                try {
                    TextLayoutBlock block = textLayoutBlock;
                    x -= textX - (int) Math.ceil(block.textXOffset);
                    y -= block.textYOffset;
                    final int line = block.textLayout.getLineForVertical((int) y);
                    final int off = block.textLayout.getOffsetForHorizontal(line, x) + block.charactersOffset;
                    Spannable buffer = (Spannable) messageText;
                    ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
                    if (link.length != 0) {
                        resetPressedLink();
                        pressedLink = link[0];
                        linkBlockNum = 1; // TODO: 10/10/15
                        try {
                            int start = buffer.getSpanStart(pressedLink) - block.charactersOffset;
                            urlPath.setCurrentLayout(block.textLayout, start);
                            block.textLayout.getSelectionPath(start, buffer.getSpanEnd(pressedLink) - block.charactersOffset, urlPath);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                        textPressed = true;
                        result = true;

                    } else {
                        resetPressedLink();
                    }
                } catch (Exception e) {
                    resetPressedLink();
                    FileLog.e("tmessages", e);
                }
            }
        } else {
            resetPressedLink();
        }
        return result;
    }


    protected void resetPressedLink() {
        if (pressedLink != null) {
            pressedLink = null;
        }
//        linkPreviewPressed = false;
        invalidate();
    }


    public void setDelegate(PostCellDelegate delegate) {
        this.delegate = delegate;
    }


    public ImageReceiver getPhotoImage() {
        return photoImage;
    }


    public int getIndex() {
        return index;
    }
}


//
// Backup press URL method.
//    public boolean onTouchEventMessage(MotionEvent event) {
//        boolean result = false;
//        if (textLayoutBlock != null && post.getText() != null && !isPressed && textLayoutBlock.textLayout != null) {
//
////            CharSequence messageText = textLayoutBlock.messageText;
//            CharSequence messageText = textLayoutBlock.textLayout.getText();
//
//            if (event.getAction() == MotionEvent.ACTION_DOWN || (pressedLink != null && event.getAction() == MotionEvent.ACTION_UP)) {
//                int x = (int) event.getX();
//                int y = (int) event.getY();
//                StaticLayout textLayout = textLayoutBlock.textLayout;
////                textLayoutBlock.textXOffset = textLeft;
////                textLayoutBlock.textYOffset = textTop;
//
//
//                float textX = textLeft;
//                float textY = textTop;
//                float textWidth = textLayout.getWidth();
//                float textHeight = textLayout.getHeight();
//                if (x >= textX && y >= textY && x <= textX + textWidth && y <= textY + textHeight) {
//                    y -= textY;
//                    try {
////                            MessageObject.TextLayoutBlock block = currentMessageObject.textLayoutBlocks.get(blockNum);
//                        TextLayoutBlock block = textLayoutBlock;
//                        x -= textX - (int) Math.ceil(block.textXOffset);
//                        y -= block.textYOffset;
//                        final int line = block.textLayout.getLineForVertical(y);
//                        final int off = block.textLayout.getOffsetForHorizontal(line, x) + block.charactersOffset;
//
//                        final float left = block.textLayout.getLineLeft(line);
//                        Spannable buffer = (Spannable) messageText;
////                        Spannable buffer = messageText;
//                        ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
//                        if (link.length != 0) {
//                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                                resetPressedLink();
//                                pressedLink = link[0];
//                                linkBlockNum = 1; // TODO: 10/10/15
//                                try {
//                                    int start = buffer.getSpanStart(pressedLink) - block.charactersOffset;
//                                    urlPath.setCurrentLayout(block.textLayout, start);
//                                    block.textLayout.getSelectionPath(start, buffer.getSpanEnd(pressedLink) - block.charactersOffset, urlPath);
//                                } catch (Exception e) {
//                                    FileLog.e("tmessages", e);
//                                }
//                                result = true;
//                            } else {
//                                if (link[0] == pressedLink) {
//                                    try {
//                                        if (pressedLink instanceof URLSpanNoUnderline) {
//                                            String url = ((URLSpanNoUnderline) pressedLink).getURL();
//                                            if (url.startsWith("@") || url.startsWith("#")) {
//                                                if (delegate != null) {
//                                                    delegate.didPressUrl(url);
//                                                }
//                                            }
//                                        } else {
//                                            pressedLink.onClick(this);
//                                        }
//                                    } catch (Exception e) {
//                                        FileLog.e("tmessages", e);
//                                    }
//                                    resetPressedLink();
//                                    result = true;
//                                }
//                            }
//                        } else {
//                            resetPressedLink();
//                        }
//                    } catch (Exception e) {
//                        resetPressedLink();
//                        FileLog.e("tmessages", e);
//                    }
//                }
//            } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
//                resetPressedLink();
//            }
//        } else {
//            resetPressedLink();
//        }
//        if (result && event.getAction() == MotionEvent.ACTION_DOWN) {
//            startCheckLongPress();
//        }
//        if (event.getAction() != MotionEvent.ACTION_DOWN && event.getAction() != MotionEvent.ACTION_MOVE) {
//            cancelCheckLongPress();
//        }
//        return result || super.onTouchEvent(event);
//    }
