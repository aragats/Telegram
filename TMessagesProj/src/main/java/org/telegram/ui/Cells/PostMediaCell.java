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
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.dto.Image;
import org.telegram.messenger.object.PostObject;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.RadialProgress;
import org.telegram.ui.Components.ResourceLoader;

import java.util.Locale;

//TODO-aragats. remove dependency to ChatBaseCell
public class PostMediaCell extends BaseCell implements MediaController.FileDownloadProgressListener {


    public interface PostBaseCellDelegate {
        void didPressedUserAvatar(PostMediaCell cell);

        void didPressedCancelSendButton(PostMediaCell cell);

        void didLongPressed(PostMediaCell cell);

        void didPressReplyMessage(PostMediaCell cell, int id);

        void didPressUrl(String url);

        void needOpenWebView(String url, String title, String originalUrl, int w, int h);

        boolean canPerformActions();
    }

    public interface PostMediaCellDelegate {
        void didClickedImage(PostMediaCell cell);

        void didPressedOther(PostMediaCell cell);
    }


    //TODO from chatBaseCell
    private PostBaseCellDelegate delegate = null;
    protected int backgroundWidth = 100;
    private AvatarDrawable imageDrawable;
//    private ImageReceiver imageDrawable;

    protected PostObject postObject;

    private int layoutWidth;
    //TODO height of cell?
    private int layoutHeight;

    ///


    private static TextPaint infoPaint;
    private static MessageObject lastDownloadedGifMessage = null;
    private static TextPaint namePaint;
    private static Paint docBackPaint;
    private static Paint deleteProgressPaint;
    private static TextPaint locationTitlePaint;
    private static TextPaint locationAddressPaint;

    private RadialProgress radialProgress;

    private int photoWidth;
    private int photoHeight;
    private TLRPC.PhotoSize currentPhotoObject;
    private TLRPC.PhotoSize currentPhotoObjectThumb;
    private String currentUrl;
    private String currentPhotoFilter;
    private ImageReceiver photoImage;
    private boolean photoNotSet = false;
    private boolean cancelLoading = false;
    private int additionHeight;

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

    private PostMediaCellDelegate mediaDelegate = null;
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

            docBackPaint = new Paint();

            deleteProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            deleteProgressPaint.setColor(0xffe4e2e0);

            locationTitlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            locationTitlePaint.setTextSize(AndroidUtilities.dp(14));
            locationTitlePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            locationAddressPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            locationAddressPaint.setTextSize(AndroidUtilities.dp(14));

        }

        TAG = MediaController.getInstance().generateObserverTag();

        photoImage = new ImageReceiver(this);
        //TODO-aragats from old.
        imageDrawable = new AvatarDrawable();
//        imageDrawable = new ImageReceiver(this);

        radialProgress = new RadialProgress(this);
    }


    //TODO two delegates

    public void setMediaDelegate(PostMediaCellDelegate delegate) {
        this.mediaDelegate = delegate;
    }

    public void setDelegate(PostBaseCellDelegate delegate) {
        this.delegate = delegate;
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
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
            //TODO my
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            imagePressed = false;
            playSoundEffect(SoundEffectConstants.CLICK);
            didClickedImage();
            invalidate();
        } else {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                cancelCheckLongPress();
            }
            if (imagePressed) {
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
                    if (mediaDelegate != null) {
                        mediaDelegate.didPressedOther(this);
                    }
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    otherPressed = false;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
//                    if (postObject.type == 9) {
//                        if (!(x >= photoImage.getImageX() + backgroundWidth - AndroidUtilities.dp(50) && x <= photoImage.getImageX() + backgroundWidth && y >= photoImage.getImageY() && y <= photoImage.getImageY() + photoImage.getImageHeight())) {
//                            otherPressed = false;
//                        }
//                    }
                }
            }
        }
        if (!result) {
            result = super.onTouchEvent(event);
        }

        return result;
    }

    private void didClickedImage() {
        if (this.delegate != null) {
            this.mediaDelegate.didClickedImage(this);
        }
    }


    private void didPressedButton(boolean animated) {

    }


    //TODO-aragats old method from PostMediaOld.
    public void setPostObject(PostObject postObjectObject) {

        this.postObject = postObjectObject;
        if (this.postObject != null) {
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
            if (this.postObject.getPreviewImage() != null) {
                Image previewImage = this.postObject.getPreviewImage();

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


//    @Override
//    public void setPostObject(PostObject postObject) {
//        boolean dataChanged = postObject == postObject ;
//        if (postObject != postObject) {
//            currentPhotoObject = null;
//            currentPhotoObjectThumb = null;
//            currentUrl = null;
//            photoNotSet = false;
//            drawBackground = true;
//
//            photoImage.setForcePreview(postObject.isSecretPhoto());
//            if (postObject.type == 9) {
//                String name = postObject.getDocumentName();
//                if (name == null || name.length() == 0) {
//                    name = LocaleController.getString("AttachDocument", R.string.AttachDocument);
//                }
//                int maxWidth;
//                if (AndroidUtilities.isTablet()) {
//                    maxWidth = AndroidUtilities.getMinTabletSide() - AndroidUtilities.dp(122 + 86 + 24);
//                } else {
//                    maxWidth = Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) - AndroidUtilities.dp(122 + 86 + 24);
//                }
//                if (currentNameString == null || !currentNameString.equals(name)) {
//                    currentNameString = name;
//                    nameLayout = StaticLayoutEx.createStaticLayout(currentNameString, namePaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TextUtils.TruncateAt.END, maxWidth, 1);
//                    if (nameLayout.getLineCount() > 0) {
//                        nameWidth = Math.min(maxWidth, (int) Math.ceil(nameLayout.getLineWidth(0)));
//                    } else {
//                        nameWidth = maxWidth;
//                    }
//                }
//
//                String fileName = postObject.getFileName();
//                int idx = fileName.lastIndexOf(".");
//                String ext = null;
//                if (idx != -1) {
//                    ext = fileName.substring(idx + 1);
//                }
//                if (ext == null || ext.length() == 0) {
//                    ext = postObject.messageOwner.media.document.mime_type;
//                }
//                ext = ext.toUpperCase();
//
//                String str = AndroidUtilities.formatFileSize(postObject.messageOwner.media.document.size) + " " + ext;
//
//                if (currentInfoString == null || !currentInfoString.equals(str)) {
//                    currentInfoString = str;
//                    infoOffset = 0;
//                    infoWidth = Math.min(maxWidth, (int) Math.ceil(infoPaint.measureText(currentInfoString)));
//                    CharSequence str2 = TextUtils.ellipsize(currentInfoString, infoPaint, infoWidth, TextUtils.TruncateAt.END);
//                    infoLayout = new StaticLayout(str2, infoPaint, infoWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
//                }
//            } else if (postObject.type == 8) {
//                gifDrawable = MediaController.getInstance().getGifDrawable(this, false);
//
//                String str = AndroidUtilities.formatFileSize(postObject.messageOwner.media.document.size);
//                if (currentInfoString == null || !currentInfoString.equals(str)) {
//                    currentInfoString = str;
//                    infoOffset = 0;
//                    infoWidth = (int) Math.ceil(infoPaint.measureText(currentInfoString));
//                    infoLayout = new StaticLayout(currentInfoString, infoPaint, infoWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
//                }
//                nameLayout = null;
//                currentNameString = null;
//            } else if (postObject.type == 3) {
//                int duration = postObject.messageOwner.media.video.duration;
//                int minutes = duration / 60;
//                int seconds = duration - minutes * 60;
//                String str = String.format("%d:%02d, %s", minutes, seconds, AndroidUtilities.formatFileSize(postObject.messageOwner.media.video.size));
//                if (currentInfoString == null || !currentInfoString.equals(str)) {
//                    currentInfoString = str;
//                    infoOffset = ResourceLoader.videoIconDrawable.getIntrinsicWidth() + AndroidUtilities.dp(4);
//                    infoWidth = (int) Math.ceil(infoPaint.measureText(currentInfoString));
//                    infoLayout = new StaticLayout(currentInfoString, infoPaint, infoWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
//                }
//                nameLayout = null;
//                currentNameString = null;
//            } else {
//                currentInfoString = null;
//                currentNameString = null;
//                infoLayout = null;
//                nameLayout = null;
//                updateSecretTimeText();
//            }
//            if (postObject.type == 9) { //doc
//                photoWidth = AndroidUtilities.dp(86);
//                photoHeight = AndroidUtilities.dp(86);
//                backgroundWidth = photoWidth + Math.max(nameWidth, infoWidth) + AndroidUtilities.dp(68);
//                currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(postObject.photoThumbs, AndroidUtilities.getPhotoSize());
//                photoImage.setNeedsQualityThumb(true);
//                photoImage.setShouldGenerateQualityThumb(true);
//                photoImage.setParentMessageObject(postObject);
//                if (currentPhotoObject != null) {
//                    currentPhotoFilter = String.format(Locale.US, "%d_%d_b", photoWidth, photoHeight);
//                    photoImage.setImage(null, null, null, null, currentPhotoObject.location, currentPhotoFilter, 0, null, true);
//                } else {
//                    photoImage.setImageBitmap((BitmapDrawable) null);
//                }
//            } else if (postObject.type == 4) { //geo
//                double lat = postObject.messageOwner.media.geo.lat;
//                double lon = postObject.messageOwner.media.geo._long;
//
//                if (postObject.messageOwner.media.title != null && postObject.messageOwner.media.title.length() > 0) {
//                    int maxWidth = (AndroidUtilities.isTablet() ? AndroidUtilities.getMinTabletSide() : Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)) - AndroidUtilities.dp((isChat && !postObject.isOut() ? 102 : 40) + 86 + 24);
//                    nameLayout = StaticLayoutEx.createStaticLayout(postObject.messageOwner.media.title, locationTitlePaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TextUtils.TruncateAt.END, maxWidth - AndroidUtilities.dp(4), 3);
//                    int lineCount = nameLayout.getLineCount();
//                    if (postObject.messageOwner.media.address != null && postObject.messageOwner.media.address.length() > 0) {
//                        infoLayout = StaticLayoutEx.createStaticLayout(postObject.messageOwner.media.address, locationAddressPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TextUtils.TruncateAt.END, maxWidth - AndroidUtilities.dp(4), Math.min(3, 4 - lineCount));
//                    } else {
//                        infoLayout = null;
//                    }
//
//                    media = false;
//                    measureTime(postObject);
//                    photoWidth = AndroidUtilities.dp(86);
//                    photoHeight = AndroidUtilities.dp(86);
//                    maxWidth = timeWidth + AndroidUtilities.dp(postObject.isOut() ? 29 : 9);
//                    for (int a = 0; a < lineCount; a++) {
//                        maxWidth = (int) Math.max(maxWidth, nameLayout.getLineWidth(a) + AndroidUtilities.dp(16));
//                    }
//                    if (infoLayout != null) {
//                        for (int a = 0; a < infoLayout.getLineCount(); a++) {
//                            maxWidth = (int) Math.max(maxWidth, infoLayout.getLineWidth(a) + AndroidUtilities.dp(16));
//                        }
//                    }
//                    backgroundWidth = photoWidth + AndroidUtilities.dp(21) + maxWidth;
//                    currentUrl = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=13&size=72x72&maptype=roadmap&scale=%d&markers=color:red|size:big|%f,%f&sensor=false", lat, lon, Math.min(2, (int) Math.ceil(AndroidUtilities.density)), lat, lon);
//                } else {
//                    photoWidth = AndroidUtilities.dp(200);
//                    photoHeight = AndroidUtilities.dp(100);
//                    backgroundWidth = photoWidth + AndroidUtilities.dp(12);
//                    currentUrl = String.format(Locale.US, "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=13&size=200x100&maptype=roadmap&scale=%d&markers=color:red|size:big|%f,%f&sensor=false", lat, lon, Math.min(2, (int) Math.ceil(AndroidUtilities.density)), lat, lon);
//                }
//
//                photoImage.setNeedsQualityThumb(false);
//                photoImage.setShouldGenerateQualityThumb(false);
//                photoImage.setParentMessageObject(null);
//                photoImage.setImage(currentUrl, null, postObject.isOut() ? ResourceLoader.geoOutDrawable : ResourceLoader.geoInDrawable, null, 0);
//            } else if (postObject.type == 13) { //webp
//                drawBackground = false;
//                for (TLRPC.DocumentAttribute attribute : postObject.messageOwner.media.document.attributes) {
//                    if (attribute instanceof TLRPC.TL_documentAttributeImageSize) {
//                        photoWidth = attribute.w;
//                        photoHeight = attribute.h;
//                        break;
//                    }
//                }
//                float maxHeight = AndroidUtilities.displaySize.y * 0.4f;
//                float maxWidth;
//                if (AndroidUtilities.isTablet()) {
//                    maxWidth = AndroidUtilities.getMinTabletSide() * 0.5f;
//                } else {
//                    maxWidth = AndroidUtilities.displaySize.x * 0.5f;
//                }
//                if (photoWidth == 0) {
//                    photoHeight = (int) maxHeight;
//                    photoWidth = photoHeight + AndroidUtilities.dp(100);
//                }
//                if (photoHeight > maxHeight) {
//                    photoWidth *= maxHeight / photoHeight;
//                    photoHeight = (int) maxHeight;
//                }
//                if (photoWidth > maxWidth) {
//                    photoHeight *= maxWidth / photoWidth;
//                    photoWidth = (int) maxWidth;
//                }
//                backgroundWidth = photoWidth + AndroidUtilities.dp(12);
//                currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(postObject.photoThumbs, 80);
//                photoImage.setNeedsQualityThumb(false);
//                photoImage.setShouldGenerateQualityThumb(false);
//                photoImage.setParentMessageObject(null);
//                if (postObject.messageOwner.attachPath != null && postObject.messageOwner.attachPath.length() > 0) {
//                    File f = new File(postObject.messageOwner.attachPath);
//                    if (f.exists()) {
//                        photoImage.setImage(null, postObject.messageOwner.attachPath,
//                                String.format(Locale.US, "%d_%d", photoWidth, photoHeight),
//                                null,
//                                currentPhotoObjectThumb != null ? currentPhotoObjectThumb.location : null,
//                                "b1",
//                                postObject.messageOwner.media.document.size, "webp", true);
//                    }
//                } else if (postObject.messageOwner.media.document.id != 0) {
//                    photoImage.setImage(postObject.messageOwner.media.document, null,
//                            String.format(Locale.US, "%d_%d", photoWidth, photoHeight),
//                            null,
//                            currentPhotoObjectThumb != null ? currentPhotoObjectThumb.location : null,
//                            "b1",
//                            postObject.messageOwner.media.document.size, "webp", true);
//                }
//            } else {
//                if (AndroidUtilities.isTablet()) {
//                    photoWidth = (int) (AndroidUtilities.getMinTabletSide() * 0.7f);
//                } else {
//                    photoWidth = (int) (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) * 0.7f);
//                }
//                photoHeight = photoWidth + AndroidUtilities.dp(100);
//
//                if (photoWidth > AndroidUtilities.getPhotoSize()) {
//                    photoWidth = AndroidUtilities.getPhotoSize();
//                }
//                if (photoHeight > AndroidUtilities.getPhotoSize()) {
//                    photoHeight = AndroidUtilities.getPhotoSize();
//                }
//
//                if (postObject.type == 1) {
//                    photoImage.setNeedsQualityThumb(false);
//                    photoImage.setShouldGenerateQualityThumb(false);
//                    photoImage.setParentMessageObject(null);
//                    currentPhotoObjectThumb = FileLoader.getClosestPhotoSizeWithSize(postObject.photoThumbs, 80);
//                } else if (postObject.type == 3) {
//                    photoImage.setNeedsQualityThumb(true);
//                    photoImage.setShouldGenerateQualityThumb(true);
//                    photoImage.setParentMessageObject(postObject);
//                } else if (postObject.type == 8) {
//                    photoImage.setNeedsQualityThumb(true);
//                    photoImage.setShouldGenerateQualityThumb(true);
//                    photoImage.setParentMessageObject(postObject);
//                }
//                //8 - gif, 1 - photo, 3 - video
//
//                if (postObject.caption != null) {
//                    media = false;
//                }
//
//                currentPhotoObject = FileLoader.getClosestPhotoSizeWithSize(postObject.photoThumbs, AndroidUtilities.getPhotoSize());
//
//                if (currentPhotoObject != null) {
//                    if (currentPhotoObject == currentPhotoObjectThumb) {
//                        currentPhotoObjectThumb = null;
//                    }
//                    boolean noSize = false;
//                    if (postObject.type == 3 || postObject.type == 8) {
//                        noSize = true;
//                    }
//                    float scale = (float) currentPhotoObject.w / (float) photoWidth;
//
//                    if (!noSize && currentPhotoObject.size == 0) {
//                        currentPhotoObject.size = -1;
//                    }
//
//                    int w = (int) (currentPhotoObject.w / scale);
//                    int h = (int) (currentPhotoObject.h / scale);
//                    if (w == 0) {
//                        if (postObject.type == 3) {
//                            w = infoWidth + infoOffset + AndroidUtilities.dp(16);
//                        } else {
//                            w = AndroidUtilities.dp(100);
//                        }
//                    }
//                    if (h == 0) {
//                        h = AndroidUtilities.dp(100);
//                    }
//                    if (h > photoHeight) {
//                        float scale2 = h;
//                        h = photoHeight;
//                        scale2 /= h;
//                        w = (int) (w / scale2);
//                    } else if (h < AndroidUtilities.dp(120)) {
//                        h = AndroidUtilities.dp(120);
//                        float hScale = (float) currentPhotoObject.h / h;
//                        if (currentPhotoObject.w / hScale < photoWidth) {
//                            w = (int) (currentPhotoObject.w / hScale);
//                        }
//                    }
//                    measureTime(postObject);
//                    int timeWidthTotal = timeWidth + AndroidUtilities.dp(14 + (postObject.isOut() ? 20 : 0));
//                    if (w < timeWidthTotal) {
//                        w = timeWidthTotal;
//                    }
//
//                    if (postObject.isSecretPhoto()) {
//                        if (AndroidUtilities.isTablet()) {
//                            w = h = (int) (AndroidUtilities.getMinTabletSide() * 0.5f);
//                        } else {
//                            w = h = (int) (Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y) * 0.5f);
//                        }
//                    }
//
//                    photoWidth = w;
//                    photoHeight = h;
//                    backgroundWidth = w + AndroidUtilities.dp(12);
//                    if (!media) {
//                        backgroundWidth += AndroidUtilities.dp(9);
//                    }
//                    if (postObject.caption != null) {
//                        nameLayout = new StaticLayout(postObject.caption, MessageObject.textPaint, photoWidth - AndroidUtilities.dp(10), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
//                        if (nameLayout.getLineCount() > 0) {
//                            captionHeight = nameLayout.getHeight();
//                            additionHeight += captionHeight + AndroidUtilities.dp(9);
//                            float lastLineWidth = nameLayout.getLineWidth(nameLayout.getLineCount() - 1) + nameLayout.getLineLeft(nameLayout.getLineCount() - 1);
//                            if (photoWidth - AndroidUtilities.dp(8) - lastLineWidth < timeWidthTotal) {
//                                additionHeight += AndroidUtilities.dp(14);
//                            }
//                        }
//                    }
//
//                    currentPhotoFilter = String.format(Locale.US, "%d_%d", (int) (w / AndroidUtilities.density), (int) (h / AndroidUtilities.density));
//                    if (postObject.photoThumbs.size() > 1 || postObject.type == 3 || postObject.type == 8) {
//                        if (postObject.isSecretPhoto()) {
//                            currentPhotoFilter += "_b2";
//                        } else {
//                            currentPhotoFilter += "_b";
//                        }
//                    }
//
//                    String fileName = FileLoader.getAttachFileName(currentPhotoObject);
//                    if (postObject.type == 1) {
//                        boolean photoExist = true;
//                        File cacheFile = FileLoader.getPathToMessage(postObject.messageOwner);
//                        if (!cacheFile.exists()) {
//                            photoExist = false;
//                        } else {
//                            MediaController.getInstance().removeLoadingFileObserver(this);
//                        }
//
//                        if (photoExist || MediaController.getInstance().canDownloadMedia(MediaController.AUTODOWNLOAD_MASK_PHOTO) || FileLoader.getInstance().isLoadingFile(fileName)) {
//                            if (allowedToSetPhoto || ImageLoader.getInstance().getImageFromMemory(currentPhotoObject.location, null, currentPhotoFilter) != null) {
//                                allowedToSetPhoto = true;
//                                photoImage.setImage(currentPhotoObject.location, currentPhotoFilter, currentPhotoObjectThumb != null ? currentPhotoObjectThumb.location : null, currentPhotoFilter, noSize ? 0 : currentPhotoObject.size, null, false);
//                            } else if (currentPhotoObjectThumb != null) {
//                                photoImage.setImage(null, null, currentPhotoObjectThumb.location, currentPhotoFilter, 0, null, false);
//                            } else {
//                                photoImage.setImageBitmap((Drawable) null);
//                            }
//                        } else {
//                            photoNotSet = true;
//                            if (currentPhotoObjectThumb != null) {
//                                photoImage.setImage(null, null, currentPhotoObjectThumb.location, currentPhotoFilter, 0, null, false);
//                            } else {
//                                photoImage.setImageBitmap((Drawable) null);
//                            }
//                        }
//                    } else {
//                        photoImage.setImage(null, null, currentPhotoObject.location, currentPhotoFilter, 0, null, false);
//                    }
//                } else {
//                    photoImage.setImageBitmap((Bitmap) null);
//                }
//            }
//            super.setMessageObject(postObject);
//
//            invalidate();
//        }
//    }

    public ImageReceiver getPhotoImage() {
        return photoImage;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), photoHeight + AndroidUtilities.dp(14) + additionHeight);
//        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), photoHeight + AndroidUtilities.dp(14) + namesOffset + additionHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

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
        if (postObject != null) {
            allowedToSetPhoto = value;
            if (value) {
                PostObject temp = postObject;
                postObject = null;
                setPostObject(temp);
            }
        }
    }


//    @Override
//    protected void onDraw(Canvas canvas) {
//        if (postObject == null) {
//            return;
//        }
//
////        if (!wasLayout) {
////            requestLayout();
////            return;
////        }
//
//        if (isAvatarVisible) {
//            avatarImage.draw(canvas);
//        }
//
//        Drawable currentBackgroundDrawable;
//        if (currentMessageObject.isOut()) {
//            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed || isHighlighted) {
//                if (!media) {
//                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableOutSelected;
//                } else {
//                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOutSelected;
//                }
//            } else {
//                if (!media) {
//                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableOut;
//                } else {
//                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOut;
//                }
//            }
//            setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - (!media ? 0 : AndroidUtilities.dp(9)), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - AndroidUtilities.dp(2));
//        } else {
//            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed || isHighlighted) {
//                if (!media) {
//                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableInSelected;
//                } else {
//                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableInSelected;
//                }
//            } else {
//                if (!media) {
//                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableIn;
//                } else {
//                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableIn;
//                }
//            }
//            if (isChat) {
//                setDrawableBounds(currentBackgroundDrawable, AndroidUtilities.dp(52 + (!media ? 0 : 9)), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - AndroidUtilities.dp(2));
//            } else {
//                setDrawableBounds(currentBackgroundDrawable, (!media ? 0 : AndroidUtilities.dp(9)), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - AndroidUtilities.dp(2));
//            }
//        }
//        if (drawBackground) {
//            currentBackgroundDrawable.draw(canvas);
//        }
//
//        onAfterBackgroundDraw(canvas);
//
//        if (drawName && nameLayout != null) {
//            canvas.save();
//            canvas.translate(currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(19) - nameOffsetX, AndroidUtilities.dp(10));
//            namePaint.setColor(AvatarDrawable.getNameColorForId(currentUser.id));
//            nameLayout.draw(canvas);
//            canvas.restore();
//        }
//
//        if (drawForwardedName && forwardedNameLayout != null) {
//            forwardNameY = AndroidUtilities.dp(10 + (drawName ? 19 : 0));
//            if (currentMessageObject.isOut()) {
//                forwardNamePaint.setColor(0xff4a923c);
//                forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(10);
//            } else {
//                forwardNamePaint.setColor(0xff006fc8);
//                forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(19);
//            }
//            canvas.save();
//            canvas.translate(forwardNameX - forwardNameOffsetX, forwardNameY);
//            forwardedNameLayout.draw(canvas);
//            canvas.restore();
//        }
//
//        if (currentMessageObject.isReply()) {
//            if (currentMessageObject.type == 13) {
//                replyLinePaint.setColor(0xffffffff);
//                replyNamePaint.setColor(0xffffffff);
//                replyTextPaint.setColor(0xffffffff);
//                int backWidth;
//                if (currentMessageObject.isOut()) {
//                    backWidth = currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(32);
//                    replyStartX = currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(9) - backWidth;
//                } else {
//                    backWidth = getWidth() - currentBackgroundDrawable.getBounds().right - AndroidUtilities.dp(32);
//                    replyStartX = currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(23);
//                }
//                Drawable back;
//                if (ApplicationLoader.isCustomTheme()) {
//                    back = ResourceLoader.backgroundBlack;
//                } else {
//                    back = ResourceLoader.backgroundBlue;
//                }
//                replyStartY = layoutHeight - AndroidUtilities.dp(58);
//                back.setBounds(replyStartX - AndroidUtilities.dp(7), replyStartY - AndroidUtilities.dp(6), replyStartX - AndroidUtilities.dp(7) + backWidth, replyStartY + AndroidUtilities.dp(41));
//                back.draw(canvas);
//            } else {
//                if (currentMessageObject.isOut()) {
//                    replyLinePaint.setColor(0xff8dc97a);
//                    replyNamePaint.setColor(0xff61a349);
//                    if (currentMessageObject.replyMessageObject != null && currentMessageObject.replyMessageObject.type == 0) {
//                        replyTextPaint.setColor(0xff000000);
//                    } else {
//                        replyTextPaint.setColor(0xff70b15c);
//                    }
//                    replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11);
//                } else {
//                    replyLinePaint.setColor(0xff6c9fd2);
//                    replyNamePaint.setColor(0xff377aae);
//                    if (currentMessageObject.replyMessageObject != null && currentMessageObject.replyMessageObject.type == 0) {
//                        replyTextPaint.setColor(0xff000000);
//                    } else {
//                        replyTextPaint.setColor(0xff999999);
//                    }
//                    if (currentMessageObject.contentType == 1 && media) {
//                        replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11);
//                    } else {
//                        replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(20);
//                    }
//                }
//                replyStartY = AndroidUtilities.dp(12 + (drawForwardedName && forwardedNameLayout != null ? 36 : 0) + (drawName && nameLayout != null ? 20 : 0));
//            }
//            canvas.drawRect(replyStartX, replyStartY, replyStartX + AndroidUtilities.dp(2), replyStartY + AndroidUtilities.dp(35), replyLinePaint);
//            if (needReplyImage) {
//                replyImageReceiver.setImageCoords(replyStartX + AndroidUtilities.dp(10), replyStartY, AndroidUtilities.dp(35), AndroidUtilities.dp(35));
//                replyImageReceiver.draw(canvas);
//            }
//            if (replyNameLayout != null) {
//                canvas.save();
//                canvas.translate(replyStartX - replyNameOffset + AndroidUtilities.dp(10 + (needReplyImage ? 44 : 0)), replyStartY);
//                replyNameLayout.draw(canvas);
//                canvas.restore();
//            }
//            if (replyTextLayout != null) {
//                canvas.save();
//                canvas.translate(replyStartX - replyTextOffset + AndroidUtilities.dp(10 + (needReplyImage ? 44 : 0)), replyStartY + AndroidUtilities.dp(19));
//                replyTextLayout.draw(canvas);
//                canvas.restore();
//            }
//        }
//
//        if (drawTime || !media) {
//            if (media) {
//                setDrawableBounds(ResourceLoader.mediaBackgroundDrawable, timeX - AndroidUtilities.dp(3), layoutHeight - AndroidUtilities.dp(27.5f), timeWidth + AndroidUtilities.dp(6 + (currentMessageObject.isOut() ? 20 : 0)), AndroidUtilities.dp(16.5f));
//                ResourceLoader.mediaBackgroundDrawable.draw(canvas);
//
//                canvas.save();
//                canvas.translate(timeX, layoutHeight - AndroidUtilities.dp(12.0f) - timeLayout.getHeight());
//                timeLayout.draw(canvas);
//                canvas.restore();
//            } else {
//                canvas.save();
//                canvas.translate(timeX, layoutHeight - AndroidUtilities.dp(6.5f) - timeLayout.getHeight());
//                timeLayout.draw(canvas);
//                canvas.restore();
//            }
//
//            if (currentMessageObject.isOut()) {
//                boolean drawCheck1 = false;
//                boolean drawCheck2 = false;
//                boolean drawClock = false;
//                boolean drawError = false;
//                boolean isBroadcast = (int)(currentMessageObject.getDialogId() >> 32) == 1;
//
//                if (currentMessageObject.isSending()) {
//                    drawCheck1 = false;
//                    drawCheck2 = false;
//                    drawClock = true;
//                    drawError = false;
//                } else if (currentMessageObject.isSendError()) {
//                    drawCheck1 = false;
//                    drawCheck2 = false;
//                    drawClock = false;
//                    drawError = true;
//                } else if (currentMessageObject.isSent()) {
//                    if (!currentMessageObject.isUnread()) {
//                        drawCheck1 = true;
//                        drawCheck2 = true;
//                    } else {
//                        drawCheck1 = false;
//                        drawCheck2 = true;
//                    }
//                    drawClock = false;
//                    drawError = false;
//                }
//
//                if (drawClock) {
//                    if (!media) {
//                        setDrawableBounds(ResourceLoader.clockDrawable, layoutWidth - AndroidUtilities.dp(18.5f) - ResourceLoader.clockDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.5f) - ResourceLoader.clockDrawable.getIntrinsicHeight());
//                        ResourceLoader.clockDrawable.draw(canvas);
//                    } else {
//                        setDrawableBounds(ResourceLoader.clockMediaDrawable, layoutWidth - AndroidUtilities.dp(22.0f) - ResourceLoader.clockMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.clockMediaDrawable.getIntrinsicHeight());
//                        ResourceLoader.clockMediaDrawable.draw(canvas);
//                    }
//                }
//                if (isBroadcast) {
//                    if (drawCheck1 || drawCheck2) {
//                        if (!media) {
//                            setDrawableBounds(ResourceLoader.broadcastDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.broadcastDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.broadcastDrawable.getIntrinsicHeight());
//                            ResourceLoader.broadcastDrawable.draw(canvas);
//                        } else {
//                            setDrawableBounds(ResourceLoader.broadcastMediaDrawable, layoutWidth - AndroidUtilities.dp(24.0f) - ResourceLoader.broadcastMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.broadcastMediaDrawable.getIntrinsicHeight());
//                            ResourceLoader.broadcastMediaDrawable.draw(canvas);
//                        }
//                    }
//                } else {
//                    if (drawCheck2) {
//                        if (!media) {
//                            if (drawCheck1) {
//                                setDrawableBounds(ResourceLoader.checkDrawable, layoutWidth - AndroidUtilities.dp(22.5f) - ResourceLoader.checkDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.checkDrawable.getIntrinsicHeight());
//                            } else {
//                                setDrawableBounds(ResourceLoader.checkDrawable, layoutWidth - AndroidUtilities.dp(18.5f) - ResourceLoader.checkDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.checkDrawable.getIntrinsicHeight());
//                            }
//                            ResourceLoader.checkDrawable.draw(canvas);
//                        } else {
//                            if (drawCheck1) {
//                                setDrawableBounds(ResourceLoader.checkMediaDrawable, layoutWidth - AndroidUtilities.dp(26.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicHeight());
//                            } else {
//                                setDrawableBounds(ResourceLoader.checkMediaDrawable, layoutWidth - AndroidUtilities.dp(22.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicHeight());
//                            }
//                            ResourceLoader.checkMediaDrawable.draw(canvas);
//                        }
//                    }
//                    if (drawCheck1) {
//                        if (!media) {
//                            setDrawableBounds(ResourceLoader.halfCheckDrawable, layoutWidth - AndroidUtilities.dp(18) - ResourceLoader.halfCheckDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.halfCheckDrawable.getIntrinsicHeight());
//                            ResourceLoader.halfCheckDrawable.draw(canvas);
//                        } else {
//                            setDrawableBounds(ResourceLoader.halfCheckMediaDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.halfCheckMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.halfCheckMediaDrawable.getIntrinsicHeight());
//                            ResourceLoader.halfCheckMediaDrawable.draw(canvas);
//                        }
//                    }
//                }
//                if (drawError) {
//                    if (!media) {
//                        setDrawableBounds(ResourceLoader.errorDrawable, layoutWidth - AndroidUtilities.dp(18) - ResourceLoader.errorDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(6.5f) - ResourceLoader.errorDrawable.getIntrinsicHeight());
//                        ResourceLoader.errorDrawable.draw(canvas);
//                    } else {
//                        setDrawableBounds(ResourceLoader.errorDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.errorDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(12.5f) - ResourceLoader.errorDrawable.getIntrinsicHeight());
//                        ResourceLoader.errorDrawable.draw(canvas);
//                    }
//                }
//            }
//        }
//    }

    //TODO-aragats is old method from previous method. But I should rake from ChatBaseCell
    @Override
    protected void onDraw(Canvas canvas) {
        if (this.postObject == null) {
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
//        photoImage.setVisible(!PhotoViewer.getInstance().isShowingImage(postObject), false);
//        imageDrawn = photoImage.draw(canvas);


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

    }

    @Override
    public void onFailedDownload(String fileName) {
//        updateButtonState(false);
    }

    @Override
    public void onSuccessDownload(String fileName) {
//        radialProgress.setProgress(1, true);
//        if (postObject.type == 8 && lastDownloadedGifMessage != null && lastDownloadedGifMessage.getId() == postObject.getId()) {
//            buttonState = 2;
//            didPressedButton(true);
//        } else if (!photoNotSet) {
//            updateButtonState(true);
//        }
//        if (photoNotSet) {
//            setPostObject(postObject);
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



    public PostObject getPostObject() {
        return postObject;
    }
}
