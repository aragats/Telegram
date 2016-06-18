/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.messenger.FileLog;
import ru.aragats.wgo.dto.User;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import ru.aragats.aracle.R;

//TODO-aragats
public class DrawerProfileCell extends FrameLayout {

    private BackupImageView avatarImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private ImageView shadowView;
    private Rect srcRect = new Rect();
    private Rect destRect = new Rect();
    private Paint paint = new Paint();

    public DrawerProfileCell(Context context) {
        super(context);
        setBackgroundColor(0xff4c84b5);
//        setBackgroundColor(0xff262A3B); // black_snap

        shadowView = new ImageView(context);
        shadowView.setVisibility(INVISIBLE);
        shadowView.setScaleType(ImageView.ScaleType.FIT_XY);
        shadowView.setImageResource(R.drawable.bottom_shadow);
        addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 70, Gravity.LEFT | Gravity.BOTTOM));

        avatarImageView = new BackupImageView(context);
        avatarImageView.getImageReceiver().setRoundRadius(AndroidUtilities.dp(32));
        addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 0, 67));
//        addView(avatarImageView, LayoutHelper.createFrame(64, 64, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 0, 67));

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setGravity(Gravity.LEFT);
        addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 16, 28));

        phoneTextView = new TextView(context);
        phoneTextView.setTextColor(0xffc2e5ff);
        phoneTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        phoneTextView.setLines(1);
        phoneTextView.setMaxLines(1);
        phoneTextView.setSingleLine(true);
        phoneTextView.setGravity(Gravity.LEFT);
        addView(phoneTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM, 16, 0, 16, 9));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (Build.VERSION.SDK_INT >= 21) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148) + AndroidUtilities.statusBarHeight, MeasureSpec.EXACTLY));
        } else {
            try {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(148), MeasureSpec.EXACTLY));
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        shadowView.setVisibility(INVISIBLE);
        phoneTextView.setTextColor(0xffc2e5ff);
        super.onDraw(canvas);
    }

    public void setUser() {
        nameTextView.setText(LocaleController.getString("AppFullName", R.string.AppFullName));
        phoneTextView.setText(LocaleController.getString("AppHintText", R.string.AppHintText));
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        avatarDrawable.setColor(0xff5c98cd);
//        avatarImageView.setImage(photo, "50_50", avatarDrawable);
//        avatarImageView.setImageResource(R.drawable.pin);
        avatarImageView.setImageResource(R.drawable.ic_launcher_blue_tel);
    }


    public void setUser(User user) {
        if (user == null) {
            return;
        }
        String photo = null;
        if (user.getImage() != null) {
            photo = user.getImage().getUrl();
        }
//        nameTextView.setText(ContactsController.formatName(user.getFirstName(), user.getLastName()));
//        phoneTextView.setText(PhoneFormat.getInstance().format("+" + user.getPhone()));
        nameTextView.setText(LocaleController.getString("AppFullName", R.string.AppFullName));
        phoneTextView.setText(LocaleController.getString("AppHintText", R.string.AppHintText));
        AvatarDrawable avatarDrawable = new AvatarDrawable();
        avatarDrawable.setColor(0xff5c98cd);
//        avatarImageView.setImage(photo, "50_50", avatarDrawable);
//        avatarImageView.setImageResource(R.drawable.pin);
        avatarImageView.setImageResource(R.drawable.ic_launcher_blue_tel);
    }
}
