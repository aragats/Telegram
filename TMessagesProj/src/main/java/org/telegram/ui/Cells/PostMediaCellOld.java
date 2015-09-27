/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import java.util.Locale;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ImageReceiver;
import org.telegram.messenger.dto.Image;
import org.telegram.messenger.dto.Post;
import org.telegram.messenger.dto.User;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.RadialProgress;

import ru.aragats.wgo.R;

//TODO-aragats
public class PostMediaCellOld extends BaseCell {

    public static interface PostMediaCellDelegate {
        public abstract void didClickedImage(PostMediaCellOld cell);

        public abstract void didPressedOther(PostMediaCellOld cell);

        public abstract void didPressedUserAvatar(PostCell cell, User userObject);

        public abstract void didPressedCancelSendButton(PostCell cell);

        public abstract void didLongPressed(PostCell cell);

        public abstract boolean canPerformActions();

    }

    private int backgroundWidth = 100;


    private int layoutWidth;
    //TODO height of cell?
    private int layoutHeight;


    private int buttonX;
    private int buttonY;

    private Post post;
    private AvatarDrawable imageDrawable;

    private static Drawable backgroundMediaDrawableOutSelected;

    private static Drawable backgroundMediaDrawableOut;


    //TODO icon on media content
    private static TextPaint infoPaint;
    private static TextPaint namePaint;

    private RadialProgress radialProgress;

    private int photoWidth;
    private int photoHeight;

    private ImageReceiver photoImage;
    private boolean photoNotSet = false;
    private boolean cancelLoading = false;


    private int buttonPressed = 0;
    private boolean imagePressed = false;
    private boolean otherPressed = false;

    private StaticLayout infoLayout;
    private int infoWidth;
    private int infoOffset = 0;
    private String currentInfoString;

    private StaticLayout nameLayout;
    private int nameWidth = 0;
    private String currentNameString;

    private PostMediaCellDelegate delegate = null;
    private RectF deleteProgressRect = new RectF();

    public PostMediaCellOld(Context context) {
        super(context);

        //static
        if (infoPaint == null) {

            infoPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            infoPaint.setTextSize(AndroidUtilities.dp(12));

            namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(0xff212121);
            namePaint.setTextSize(AndroidUtilities.dp(16));

            backgroundMediaDrawableOutSelected = getResources().getDrawable(R.drawable.msg_out_photo_selected);
            backgroundMediaDrawableOut = getResources().getDrawable(R.drawable.msg_out_photo);


        }


        photoImage = new ImageReceiver(this);
        imageDrawable = new AvatarDrawable();

        radialProgress = new RadialProgress(this);
    }


    public void setDelegate(PostMediaCellDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (photoImage != null) {
            photoImage.clearImage();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        boolean result = false;
        int side = AndroidUtilities.dp(48);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (delegate == null || delegate.canPerformActions()) {

                if (x >= photoImage.getImageX() && x <= photoImage.getImageX() + backgroundWidth && y >= photoImage.getImageY() && y <= photoImage.getImageY() + photoImage.getImageHeight()) {
                    imagePressed = true;
                    result = true;
                }
            }

            if (result) {
                startCheckLongPress();
            }

        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            imagePressed = false;
            playSoundEffect(SoundEffectConstants.CLICK);
            didClickedImage();
            invalidate();
        }


        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
    }

    private void didClickedImage() {
        if (this.delegate != null) {
            this.delegate.didClickedImage(this);
        }
    }


    private void didPressedButton(boolean animated) {
    }


    public void setPost(Post post) {

        this.post = post;
        if (this.post != null) {
            updateSecretTimeText();

            if (AndroidUtilities.isTablet()) {
                photoWidth = (int) (AndroidUtilities.getMinTabletSide() * 0.7f);
            } else {
                photoWidth = (int) (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) * 0.7f); // here
            }
            photoHeight = photoWidth + AndroidUtilities.dp(100);

            if (photoWidth > AndroidUtilities.getPhotoSize()) {
                photoWidth = AndroidUtilities.getPhotoSize();
            }
            if (photoHeight > AndroidUtilities.getPhotoSize()) {
                photoHeight = AndroidUtilities.getPhotoSize();
            }


            //TODO Full or Image
            if (this.post.getPreviewImage() != null) {
                Image previewImage = this.post.getPreviewImage();

                float scale = (float) previewImage.getWidth() / (float) photoWidth; // scale calculate

                //TODO placeholder
//                scale = 1;
//                post.setWidth(photoWidth);
//                post.setHeight(photoHeight);


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
                if (h > photoHeight) {
                    float scale2 = h;
                    h = photoHeight;
                    scale2 /= h;
                    w = (int) (w / scale2);
                } else if (h < AndroidUtilities.dp(120)) {
                    h = AndroidUtilities.dp(120);
                    float hScale = (float) previewImage.getHeight() / h;
                    if (previewImage.getWidth() / hScale < photoWidth) {
                        w = (int) (previewImage.getWidth() / hScale);
                    }
                }

                photoWidth = w;
                photoHeight = h;
                backgroundWidth = w + AndroidUtilities.dp(12);

//                photoImage.setImageCoords(avatarLeft, avatarTop + AndroidUtilities.dp(52) + this.block.textLayout.getHeight(), photoWidth, photoHeight);


                photoImage.setForcePreview(false);

                int size = (int)(AndroidUtilities.getPhotoSize() / AndroidUtilities.density);
                photoImage.setImage(previewImage.getUrl(), String.format(Locale.US, "%d_%d", size, size), imageDrawable, null, 0); // TODO fix it. Create drawable.
//                photoImage.setImageBitmap(previewImage.getBitmap()); // TODO fix it. Create drawable.


                invalidate();
            }
        }

    }

    public ImageReceiver getPhotoImage() {
        return photoImage;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), photoHeight + AndroidUtilities.dp(14));
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        layoutWidth = getMeasuredWidth();
        layoutHeight = getMeasuredHeight();



        int x;
//
//        x = layoutWidth - backgroundWidth + AndroidUtilities.dp(8);
//        x = AndroidUtilities.dp(16);
//        x = AndroidUtilities.dp(69);


        x = layoutWidth - backgroundWidth - AndroidUtilities.dp(3);

//        x = AndroidUtilities.dp(67);
        photoImage.setImageCoords(x, AndroidUtilities.dp(7), photoWidth, photoHeight);
        int size = AndroidUtilities.dp(48);
        buttonX = (int) (x + (photoWidth - size) / 2.0f);
        buttonY = (int) (AndroidUtilities.dp(7) + (photoHeight - size) / 2.0f);

        radialProgress.setProgressRect(buttonX, buttonY, buttonX + AndroidUtilities.dp(48), buttonY + AndroidUtilities.dp(48));
        deleteProgressRect.set(buttonX + AndroidUtilities.dp(3), buttonY + AndroidUtilities.dp(3), buttonX + AndroidUtilities.dp(45), buttonY + AndroidUtilities.dp(45));
    }

    private void updateSecretTimeText() {
        String currentInfoString = "";

        if (this.post == null) {
            return;
        }
        //TODO info about image. Size. or so on.
        String str = "" + this.post.getPreviewImage().getSize();
        if (str == null) {
            return;
        }
        infoLayout = null;
        if (!currentInfoString.equals(str)) {
            currentInfoString = str;
            infoOffset = 0;
            infoWidth = (int) Math.ceil(infoPaint.measureText(currentInfoString));
            CharSequence str2 = TextUtils.ellipsize(currentInfoString, infoPaint, infoWidth, TextUtils.TruncateAt.END);
            infoLayout = new StaticLayout(str2, infoPaint, infoWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            invalidate();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (this.post == null) {
            return;
        }

//        if (!wasLayout) {
//            requestLayout();
//            return;
//        }


        Drawable currentBackgroundDrawable = backgroundMediaDrawableOut;
//        currentBackgroundDrawable = backgroundMediaDrawableOutSelected;


        setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(9), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - AndroidUtilities.dp(2));

        // set background for message
        currentBackgroundDrawable.draw(canvas);

        onAfterBackgroundDraw(canvas);

//        if (nameLayout != null) {
//            canvas.save();
//            canvas.translate(currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(19) - nameOffsetX, AndroidUtilities.dp(10));
//            namePaint.setColor(AvatarDrawable.getNameColorForId(currentUser.id));
//            nameLayout.draw(canvas);
//            canvas.restore();
//        }

    }


    private void onAfterBackgroundDraw(Canvas canvas) {

//        photoImage.setVisible(true, false);

        radialProgress.setHideCurrentDrawable(false);

        radialProgress.setProgressColor(0xffffffff);


        radialProgress.onDraw(canvas);


        //TODO name is the name of the document in case of PDF
//        if (infoLayout != null) {
//            infoPaint.setColor(0xffffffff);
//            //TODO background of the media content. without image. just gray background. as with sending pdf.
////            setDrawableBounds(mediaBackgroundDrawable, photoImage.getImageX() + AndroidUtilities.dp(4), photoImage.getImageY() + AndroidUtilities.dp(4), infoWidth + AndroidUtilities.dp(8) + infoOffset, AndroidUtilities.dp(16.5f));
////            mediaBackgroundDrawable.draw(canvas);
//
//            //TODO save state before change rotation or translate
//            canvas.save();
//            canvas.translate(photoImage.getImageX() + AndroidUtilities.dp(8) + infoOffset, photoImage.getImageY() + AndroidUtilities.dp(5.5f));
//            infoLayout.draw(canvas);
//            // TODO after complete draw, we can restore saved state: rotation, tranlation
//            canvas.restore();
//        }

        photoImage.draw(canvas);


    }


    public Post getPost() {
        return post;
    }
}
