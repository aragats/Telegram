package org.telegram.ui.Cells;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.ui.Components.LayoutHelper;

import ru.aragats.wgo.R;

/**
 * Created by aragats on 06/05/16.
 */
// TODO Deprecate. It works but I need to design cell.
public class LoadNextButton extends FrameLayout {

    public LoadNextButton(Context context) {
        super(context);

        Button tryAgainButton = new Button(context);
        tryAgainButton.setTransformationMethod(null);
        tryAgainButton.setText(LocaleController.getString("TryAgain", R.string.TryAgain));
        tryAgainButton.setTextColor(0xff377aae);
//        tryAgainButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf")); // ??
        tryAgainButton.setBackgroundColor(0xffd7e8f7); // background is font color of the text in address subtitle
        tryAgainButton.setGravity(Gravity.CENTER);
        tryAgainButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                refreshPosts(false);
            }
        });

        addView(tryAgainButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(54), MeasureSpec.EXACTLY));
    }
}
