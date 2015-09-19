/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui.Components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import org.telegram.android.ImageReceiver;
import org.telegram.messenger.TLObject;
import org.telegram.messenger.TLRPC;

public class BackupImageView extends View {

    private ImageReceiver imageReceiver;
    private int width = -1;
    private int height = -1;

    public BackupImageView(Context context) {
        super(context);
        init();
    }

    public BackupImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BackupImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        imageReceiver = new ImageReceiver(this);
    }

    public void setImage(String path, String filter, String ext, Drawable thumb) {
        setImage(null, filter, thumb, null, null, ext, 0);
    }

    public void setImage(String filter, Drawable thumb) {
        setImage(null, filter, thumb, null, null, null, 0);
    }

    public void setImage(String filter, Bitmap thumb) {
        setImage(null, filter, null, thumb, null, null, 0);
    }

    public void setImage(String filter, Drawable thumb, int size) {
        setImage(null, filter, thumb, null, null, null, size);
    }

    public void setImage(String filter, Bitmap thumb, int size) {
        setImage(null, filter, null, thumb, null, null, size);
    }

    public void setImage(String filter, int size) {
        setImage(null, filter, null, null, null, null, size);
    }

    public void setImage(String path, String filter, Drawable thumb) {
        setImage(path, filter, thumb, null, null, null, 0);
    }

    public void setOrientation(int angle, boolean center) {
        imageReceiver.setOrientation(angle, center);
    }

    public void setImage(String httpUrl, String filter, Drawable thumb, Bitmap thumbBitmap, String thumbFilter, String ext, int size) {
        if (thumbBitmap != null) {
            thumb = new BitmapDrawable(null, thumbBitmap);
        }
        imageReceiver.setImage(httpUrl, filter, thumb, thumbFilter, size, ext, false);
    }

    public void setImageBitmap(Bitmap bitmap) {
        imageReceiver.setImageBitmap(bitmap);
    }

    public void setImageResource(int resId) {
        imageReceiver.setImageBitmap(getResources().getDrawable(resId));
    }

    public void setImageDrawable(Drawable drawable) {
        imageReceiver.setImageBitmap(drawable);
    }

    public void setRoundRadius(int value) {
        imageReceiver.setRoundRadius(value);
    }

    public void setAspectFit(boolean value) {
        imageReceiver.setAspectFit(value);
    }

    public ImageReceiver getImageReceiver() {
        return imageReceiver;
    }

    public void setSize(int w, int h) {
        width = w;
        height = h;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        imageReceiver.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        imageReceiver.onAttachedToWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (width != -1 && height != -1) {
            imageReceiver.setImageCoords((getWidth() - width) / 2, (getHeight() - height) / 2, width, height);
        } else {
            imageReceiver.setImageCoords(0, 0, getWidth(), getHeight());
        }
        imageReceiver.draw(canvas);
    }


//    //TODO-aragats
//    public void draw(Canvas canvas) {
//        super.draw(canvas);
//        imageReceiver.draw(canvas);
//    }
//
//
//    //TODO-aragats
//    public void setImageCoords(int x, int y, int width, int height) {
//        imageReceiver.setImageCoords(x, y, width, height);
//    }
}
