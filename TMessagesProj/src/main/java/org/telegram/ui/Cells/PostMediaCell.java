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
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ImageReceiver;
import org.telegram.android.MediaController;
import org.telegram.android.MessageObject;
import org.telegram.messenger.dto.Image;
import org.telegram.messenger.dto.Post;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.ResourceLoader;

import java.util.Locale;

//TODO-aragats. remove dependency to ChatBaseCell
public class PostMediaCell extends BaseCell implements MediaController.FileDownloadProgressListener {


    public interface PostMediaCellDelegate {

        //base delegate
        void didPressedUserAvatar(PostMediaCell cell);

        void didPressedCancelSendButton(PostMediaCell cell);

        void didLongPressed(PostMediaCell cell);

        void didPressReplyMessage(PostMediaCell cell, int id);

        void didPressUrl(String url);

        void needOpenWebView(String url, String title, String originalUrl, int w, int h);

        boolean canPerformActions();


        // media delegate

        void didClickedImage(PostMediaCell cell);

        void didPressedOther(PostMediaCell cell);
    }


    //TODO from chatBaseCell
    private PostMediaCellDelegate delegate = null;
    protected int backgroundWidth = 100;
    private AvatarDrawable imageDrawable;
//    private ImageReceiver imageDrawable;

    protected Post post;

    private int layoutWidth;
    //TODO height of cell?
    private int layoutHeight;

    ///


    private boolean buttonPressed = false;

    private static TextPaint infoPaint;
    private static MessageObject lastDownloadedGifMessage = null;
    private static TextPaint namePaint;

    private RadialProgress radialProgress;

    private int photoWidth;
    private int photoHeight;
    private ImageReceiver photoImage;

    private boolean allowedToSetPhoto = true;

    private int TAG;

    private boolean imagePressed = false;
    private boolean otherPressed = false;
    private int buttonX;
    private int buttonY;

    private StaticLayout infoLayout;
    private int infoWidth;
    private int infoOffset = 0;
    private String currentInfoString;

    private StaticLayout nameLayout;
    private int nameWidth = 0;
    private String currentNameString;

    private RectF deleteProgressRect = new RectF();

    private int captionX;
    private int captionY;
    private int captionHeight;

    public PostMediaCell(Context context) {
        super(context);

        if (infoPaint == null) {
            infoPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            infoPaint.setTextSize(AndroidUtilities.dp(12));

            namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(0xff212121);
            namePaint.setTextSize(AndroidUtilities.dp(16));
        }

        TAG = MediaController.getInstance().generateObserverTag();

        photoImage = new ImageReceiver(this);
        //TODO-aragats from old.
        imageDrawable = new AvatarDrawable();
//        imageDrawable = new ImageReceiver(this);

        radialProgress = new RadialProgress(this);
    }


    //TODO two delegates

    public void setDelegate(PostMediaCellDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //old
//        if (photoImage != null) {
//            photoImage.clearImage();
//        }
        photoImage.onDetachedFromWindow();
        MediaController.getInstance().removeLoadingFileObserver(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (photoImage.onAttachedToWindow()) {
//            updateButtonState(false);
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

                if (x >= buttonX && x <= buttonX + side && y >= buttonY && y <= buttonY + side) {
                    buttonPressed = true;
                    invalidate();
                    result = true;
                } else {
                    if (x >= photoImage.getImageX() && x <= photoImage.getImageX() + backgroundWidth && y >= photoImage.getImageY() && y <= photoImage.getImageY() + photoImage.getImageHeight()) {
                        imagePressed = true;
                        result = true;
                    } else {
                        //TODO it is my//
                        otherPressed = true;
                    }
                    if (result) {
                        startCheckLongPress();
                    }
                }
            }
            //TODO my
        } else {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                cancelCheckLongPress();
            }
            if (buttonPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    buttonPressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    didPressedButton(false);
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    buttonPressed = false;
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!(x >= buttonX && x <= buttonX + side && y >= buttonY && y <= buttonY + side)) {
                        buttonPressed = false;
                        invalidate();
                    }
                }
            } else if (imagePressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    imagePressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    didClickedImage();
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    imagePressed = false;
                    invalidate();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!photoImage.isInsideImage(x, y)) {
                        imagePressed = false;
                        invalidate();
                    }

                }
            } else if (otherPressed) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    otherPressed = false;
                    playSoundEffect(SoundEffectConstants.CLICK);
                    if (delegate != null) {
                        delegate.didPressedOther(this);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    otherPressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                    if (post.type == 9) {
//                        if (!(x >= photoImage.getImageX() + backgroundWidth - AndroidUtilities.dp(50) && x <= photoImage.getImageX() + backgroundWidth && y >= photoImage.getImageY() && y <= photoImage.getImageY() + photoImage.getImageHeight())) {
//                            otherPressed = false;
//                        }
//                    }
                }
            }
//            }
        }
        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
    }


    //Drawable for delete icon
    private Drawable getDrawableForCurrentState() {
        return ResourceLoader.buttonStatesDrawables[4];

    }

    private void didClickedImage() {
//        if (this.delegate != null) {
//            this.delegate.didClickedImage(this);
//        }
    }


    private void didPressedButton(boolean animated) {
        if (this.delegate != null) {
            this.delegate.didPressedCancelSendButton(this);
        }

    }


    //TODO-aragats-old old method from PostMediaOld.
    public void setPost(Post post) {


//        currentInfoString = "INFO";
//        infoWidth = 50;
//        CharSequence str2 = TextUtils.ellipsize(currentInfoString, infoPaint, infoWidth, TextUtils.TruncateAt.END);
//        infoLayout = new StaticLayout(str2, infoPaint, infoWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);


        this.post = post;
        if (this.post != null) {
//            updateSecretTimeText();

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

                int size = (int) (AndroidUtilities.getPhotoSize() / AndroidUtilities.density);
                photoImage.setImage(previewImage.getUrl(), String.format(Locale.US, "%d_%d", size, size), imageDrawable, null, (int) previewImage.getSize()); // TODO fix it. Create drawable.
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

        //TODO from old. It was a reason why new PostMedia was not displayed
        layoutWidth = getMeasuredWidth();
        layoutHeight = getMeasuredHeight();


        int x = layoutWidth - backgroundWidth - AndroidUtilities.dp(3);


//        photoImage.setImageCoords(x, AndroidUtilities.dp(7) + namesOffset, photoWidth, photoHeight);
        photoImage.setImageCoords(x, AndroidUtilities.dp(7), photoWidth, photoHeight);
        int size = AndroidUtilities.dp(48);
        buttonX = (int) (x + (photoWidth - size) / 2.0f);
//        buttonY = (int) (AndroidUtilities.dp(7) + (photoHeight - size) / 2.0f) + namesOffset;
        buttonY = (int) (AndroidUtilities.dp(7) + (photoHeight - size) / 2.0f);

        radialProgress.setProgressRect(buttonX, buttonY, buttonX + AndroidUtilities.dp(48), buttonY + AndroidUtilities.dp(48));
        deleteProgressRect.set(buttonX + AndroidUtilities.dp(3), buttonY + AndroidUtilities.dp(3), buttonX + AndroidUtilities.dp(45), buttonY + AndroidUtilities.dp(45));
    }


    public void setAllowedToSetPhoto(boolean value) {
        if (allowedToSetPhoto == value) {
            return;
        }
        if (post != null) {
            allowedToSetPhoto = value;
            if (value) {
                Post temp = post;
                post = null;
                setPost(temp);
            }
        }
    }


    //TODO-aragats-old is old method from previous method. But I should rake from ChatBaseCell
    @Override
    protected void onDraw(Canvas canvas) {
        if (this.post == null) {
            return;
        }

//        if (!wasLayout) {
//            requestLayout();
//            return;
//        }


        Drawable currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOut;
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


    //    @Override
    protected void onAfterBackgroundDraw(Canvas canvas) {
        boolean imageDrawn = false;

//            photoImage.setPressed(isPressed() && isCheckPressed || !isCheckPressed && isPressed || isHighlighted);
//        photoImage.setPressed(isPressed());
//        photoImage.setVisible(!PhotoViewer.getInstance().isShowingImage(post), false);
//        imageDrawn = photoImage.draw(canvas);


        radialProgress.setBackground(getDrawableForCurrentState(), false, false);
        radialProgress.setHideCurrentDrawable(false);
        radialProgress.setProgressColor(0xffffffff);


        radialProgress.onDraw(canvas);


//        if (nameLayout != null) {
//            canvas.save();
//            canvas.translate(captionX = photoImage.getImageX() + AndroidUtilities.dp(5), captionY = photoImage.getImageY() + photoHeight + AndroidUtilities.dp(6));
//            if (pressedLink != null) {
//                canvas.drawPath(urlPath, urlPaint);
//            }
//            nameLayout.draw(canvas);
//            canvas.restore();
//        }
//        if (infoLayout != null ) {
//            infoPaint.setColor(0xffffffff);
//            setDrawableBounds(ResourceLoader.mediaBackgroundDrawable, photoImage.getImageX() + AndroidUtilities.dp(4), photoImage.getImageY() + AndroidUtilities.dp(4), infoWidth + AndroidUtilities.dp(8) + infoOffset, AndroidUtilities.dp(16.5f));
//            ResourceLoader.mediaBackgroundDrawable.draw(canvas);
//
//            canvas.save();
//            canvas.translate(photoImage.getImageX() + AndroidUtilities.dp(8) + infoOffset, photoImage.getImageY() + AndroidUtilities.dp(5.5f));
//            infoLayout.draw(canvas);
//            canvas.restore();
//        }

        photoImage.draw(canvas);


//        if (infoLayout != null) {
//            infoPaint.setColor(0xffffffff);
//            setDrawableBounds(ResourceLoader.mediaBackgroundDrawable, photoImage.getImageX() + AndroidUtilities.dp(4), photoImage.getImageY() + AndroidUtilities.dp(4), infoWidth + AndroidUtilities.dp(8) + infoOffset, AndroidUtilities.dp(16.5f));
//            ResourceLoader.mediaBackgroundDrawable.draw(canvas);
//
//            canvas.save();
//            canvas.translate(photoImage.getImageX() + AndroidUtilities.dp(8) + infoOffset, photoImage.getImageY() + AndroidUtilities.dp(5.5f));
//            infoLayout.draw(canvas);
//            canvas.restore();
//        }

        //DELETE BUTTON
        Drawable buttonDrawable = ResourceLoader.audioStatesDrawable[4][0];
        setDrawableBounds(buttonDrawable, buttonX, buttonY);
        buttonDrawable.draw(canvas);

//        ImageView imageView = new ImageView(this.getContext());
//        imageView.setImageResource(R.drawable.delete_reply);
//        imageView.setScaleType(ImageView.ScaleType.CENTER);
//        canvas.save();
//        canvas.translate(photoImage.getImageX() + photoWidth + AndroidUtilities.dp(8) + infoOffset, photoImage.getImageY() + AndroidUtilities.dp(5.5f));
//        imageView.draw(canvas);
//        canvas.restore();
////        replyLayout.addView(imageView, LayoutHelper.createFrame(52, 46, Gravity.RIGHT | Gravity.TOP, 0, 0.5f, 0, 0));
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Toast.makeText(PostMediaCell.this.getContext(), "DELETE CLICKED", Toast.LENGTH_SHORT).show();
//
//            }
//        });
    }

    @Override
    public void onFailedDownload(String fileName) {
//        updateButtonState(false);
    }

    @Override
    public void onSuccessDownload(String fileName) {
//        radialProgress.setProgress(1, true);
//        if (post.type == 8 && lastDownloadedGifMessage != null && lastDownloadedGifMessage.getId() == post.getId()) {
//            buttonState = 2;
//            didPressedButton(true);
//        } else if (!photoNotSet) {
//            updateButtonState(true);
//        }
//        if (photoNotSet) {
//            setPost(post);
//        }
    }

    @Override
    public void onProgressDownload(String fileName, float progress) {
//        radialProgress.setProgress(progress, true);
//        if (buttonState != 1) {
//            updateButtonState(false);
//        }
    }

    @Override
    public void onProgressUpload(String fileName, float progress, boolean isEncrypted) {
        radialProgress.setProgress(progress, true);
    }

    @Override
    public int getObserverTag() {
        return TAG;
    }


    public Post getPost() {
        return post;
    }
}
