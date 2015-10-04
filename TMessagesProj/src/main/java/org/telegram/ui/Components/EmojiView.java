/*
 * This is the source code of Telegram for Android v. 1.3.2.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.ui.Components;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.Emoji;
import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;
import org.telegram.messenger.FileLog;

import java.util.ArrayList;

import ru.aragats.wgo.R;

public class EmojiView extends LinearLayout implements NotificationCenter.NotificationCenterDelegate {

    public interface Listener {
        boolean onBackspace();
        void onEmojiSelected(String emoji);
//        void onStickerSelected(TLRPC.Document sticker);
    }

    private ArrayList<EmojiGridAdapter> adapters = new ArrayList<>();

    private int[] icons = {
            R.drawable.ic_emoji_recent,
            R.drawable.ic_emoji_smile,
            R.drawable.ic_emoji_flower,
            R.drawable.ic_emoji_bell,
            R.drawable.ic_emoji_car,
            R.drawable.ic_emoji_symbol,
            R.drawable.ic_emoji_sticker};

    private Listener listener;
    private ViewPager pager;
    private FrameLayout recentsWrap;
    private FrameLayout emojiWrap;
    private ArrayList<GridView> views = new ArrayList<>();
    private ImageView backspaceButton;

    private boolean backspacePressed;
    private boolean backspaceOnce;

    public EmojiView(Context context) {
        super(context);


        setOrientation(LinearLayout.VERTICAL);
        for (int i = 0; i < Emoji.data.length; i++) {
            GridView gridView = new GridView(context);
            if (AndroidUtilities.isTablet()) {
                gridView.setColumnWidth(AndroidUtilities.dp(60));
            } else {
                gridView.setColumnWidth(AndroidUtilities.dp(45));
            }
            gridView.setNumColumns(-1);
            views.add(gridView);

            EmojiGridAdapter emojiGridAdapter = new EmojiGridAdapter(Emoji.data[i]);
            gridView.setAdapter(emojiGridAdapter);
            AndroidUtilities.setListViewEdgeEffectColor(gridView, 0xfff5f6f7);
            adapters.add(emojiGridAdapter);
        }

        setBackgroundColor(0xfff5f6f7);

        pager = new ViewPager(context);
        pager.setAdapter(new EmojiPagesAdapter());

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setBackgroundColor(0xfff5f6f7);
        addView(linearLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, AndroidUtilities.dp(48)));

        PagerSlidingTabStrip tabs = new PagerSlidingTabStrip(context);
        tabs.setViewPager(pager);
        tabs.setShouldExpand(true);
        tabs.setIndicatorHeight(AndroidUtilities.dp(2));
        tabs.setUnderlineHeight(AndroidUtilities.dp(1));
        tabs.setIndicatorColor(0xff2b96e2);
        tabs.setUnderlineColor(0xffe2e5e7);
        linearLayout.addView(tabs, new LinearLayout.LayoutParams(0, AndroidUtilities.dp(48), 1.0f));

        FrameLayout frameLayout = new FrameLayout(context);
        linearLayout.addView(frameLayout, new LinearLayout.LayoutParams(AndroidUtilities.dp(52), AndroidUtilities.dp(48)));

        backspaceButton = new ImageView(context) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    backspacePressed = true;
                    backspaceOnce = false;
                    postBackspaceRunnable(350);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    backspacePressed = false;
                    if (!backspaceOnce) {
                        if (EmojiView.this.listener != null && EmojiView.this.listener.onBackspace()) {
                            backspaceButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        }
                    }
                }
                super.onTouchEvent(event);
                return true;
            }
        };
        backspaceButton.setImageResource(R.drawable.ic_smiles_backspace);
        backspaceButton.setBackgroundResource(R.drawable.ic_emoji_backspace);
        backspaceButton.setScaleType(ImageView.ScaleType.CENTER);
        frameLayout.addView(backspaceButton, new FrameLayout.LayoutParams(AndroidUtilities.dp(52), AndroidUtilities.dp(48)));

        View view = new View(context);
        view.setBackgroundColor(0xffe2e5e7);
        frameLayout.addView(view, new FrameLayout.LayoutParams(AndroidUtilities.dp(52), AndroidUtilities.dp(1), Gravity.LEFT | Gravity.BOTTOM));

        recentsWrap = new FrameLayout(context);
        recentsWrap.addView(views.get(0));

        TextView textView = new TextView(context);
        textView.setText(LocaleController.getString("NoRecent", R.string.NoRecent));
        textView.setTextSize(18);
        textView.setTextColor(0xff888888);
        textView.setGravity(Gravity.CENTER);
        recentsWrap.addView(textView);
        views.get(0).setEmptyView(textView);

        if (views.size() > 6) {
            emojiWrap = new FrameLayout(context);
            emojiWrap.addView(views.get(6));

            textView = new TextView(context);
            textView.setText(LocaleController.getString("NoStickers", R.string.NoStickers));
            textView.setTextSize(18);
            textView.setTextColor(0xff888888);
            textView.setGravity(Gravity.CENTER);
            emojiWrap.addView(textView);
            views.get(6).setEmptyView(textView);
        }

        addView(pager);

        loadRecents();

        if (Emoji.data[0] == null || Emoji.data[0].length == 0) {
            pager.setCurrentItem(1);
        }
    }

    private void postBackspaceRunnable(final int time) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (!backspacePressed) {
                    return;
                }
                if (EmojiView.this.listener != null && EmojiView.this.listener.onBackspace()) {
                    backspaceButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                }
                backspaceOnce = true;
                postBackspaceRunnable(Math.max(50, time - 100));
            }
        }, time);
    }

    private void addToRecent(long code) {
        if (pager.getCurrentItem() == 0) {
            return;
        }
        ArrayList<Long> recent = new ArrayList<>();
        long[] currentRecent = Emoji.data[0];
        boolean was = false;
        for (long aCurrentRecent : currentRecent) {
            if (code == aCurrentRecent) {
                recent.add(0, code);
                was = true;
            } else {
                recent.add(aCurrentRecent);
            }
        }
        if (!was) {
            recent.add(0, code);
        }
        Emoji.data[0] = new long[Math.min(recent.size(), 50)];
        for (int q = 0; q < Emoji.data[0].length; q++) {
            Emoji.data[0][q] = recent.get(q);
        }
        adapters.get(0).data = Emoji.data[0];
        adapters.get(0).notifyDataSetChanged();
        saveRecents();
    }

    private String convert(long paramLong) {
        String str = "";
        for (int i = 0; ; i++) {
            if (i >= 4) {
                return str;
            }
            int j = (int) (0xFFFF & paramLong >> 16 * (3 - i));
            if (j != 0) {
                str = str + (char) j;
            }
        }
    }

    private void saveRecents() {
        ArrayList<Long> arrayList = new ArrayList<>(Emoji.data[0].length);
        for (int j = 0; j < Emoji.data[0].length; j++) {
            arrayList.add(Emoji.data[0][j]);
        }
        getContext().getSharedPreferences("emoji", 0).edit().putString("recents", TextUtils.join(",", arrayList)).commit();
    }




    public void loadRecents() {
        SharedPreferences preferences = getContext().getSharedPreferences("emoji", Activity.MODE_PRIVATE);
        String str = preferences.getString("recents", "");
        try {
            if (str != null && str.length() > 0) {
                String[] args = str.split(",");
                Emoji.data[0] = new long[args.length];
                for (int i = 0; i < args.length; i++) {
                    Emoji.data[0][i] = Long.parseLong(args[i]);
                }
            } else {
                Emoji.data[0] = new long[]{0x00000000D83DDE02L, 0x00000000D83DDE18L, 0x0000000000002764L, 0x00000000D83DDE0DL, 0x00000000D83DDE0AL, 0x00000000D83DDE01L,
                        0x00000000D83DDC4DL, 0x000000000000263AL, 0x00000000D83DDE14L, 0x00000000D83DDE04L, 0x00000000D83DDE2DL, 0x00000000D83DDC8BL,
                        0x00000000D83DDE12L, 0x00000000D83DDE33L, 0x00000000D83DDE1CL, 0x00000000D83DDE48L, 0x00000000D83DDE09L, 0x00000000D83DDE03L,
                        0x00000000D83DDE22L, 0x00000000D83DDE1DL, 0x00000000D83DDE31L, 0x00000000D83DDE21L, 0x00000000D83DDE0FL, 0x00000000D83DDE1EL,
                        0x00000000D83DDE05L, 0x00000000D83DDE1AL, 0x00000000D83DDE4AL, 0x00000000D83DDE0CL, 0x00000000D83DDE00L, 0x00000000D83DDE0BL,
                        0x00000000D83DDE06L, 0x00000000D83DDC4CL, 0x00000000D83DDE10L, 0x00000000D83DDE15L};
            }
            adapters.get(0).data = Emoji.data[0];
            adapters.get(0).notifyDataSetChanged();
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
    }

    public void setListener(Listener value) {
        listener = value;
    }

    public void invalidateViews() {
        for (GridView gridView : views) {
            if (gridView != null) {
                gridView.invalidateViews();
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }



    private class EmojiGridAdapter extends BaseAdapter {
        long[] data;

        public EmojiGridAdapter(long[] arg2) {
            this.data = arg2;
        }

        public int getCount() {
            return data.length;
        }

        public Object getItem(int i) {
            return null;
        }

        public long getItemId(int i) {
            return data[i];
        }

        public View getView(int i, View view, ViewGroup paramViewGroup) {
            ImageView imageView = (ImageView)view;
            if (imageView == null) {
                imageView = new ImageView(EmojiView.this.getContext()) {
                    public void onMeasure(int paramAnonymousInt1, int paramAnonymousInt2) {
                        setMeasuredDimension(View.MeasureSpec.getSize(paramAnonymousInt1), View.MeasureSpec.getSize(paramAnonymousInt1));
                    }
                };
                imageView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (EmojiView.this.listener != null) {
                            EmojiView.this.listener.onEmojiSelected(EmojiView.this.convert((Long)view.getTag()));
                        }
                        EmojiView.this.addToRecent((Long)view.getTag());
                    }
                });
                imageView.setBackgroundResource(R.drawable.list_selector);
                imageView.setScaleType(ImageView.ScaleType.CENTER);
            }
            imageView.setImageDrawable(Emoji.getEmojiBigDrawable(data[i]));
            imageView.setTag(data[i]);
            return imageView;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }

    private class EmojiPagesAdapter extends PagerAdapter implements PagerSlidingTabStrip.IconTabProvider {

        public void destroyItem(ViewGroup paramViewGroup, int paramInt, Object paramObject) {
            View localObject;
            if (paramInt == 0) {
                localObject = recentsWrap;
            } else if (paramInt == 6) {
                localObject = emojiWrap;
            } else {
                localObject = views.get(paramInt);
            }
            paramViewGroup.removeView(localObject);
        }

        public int getCount() {
            return views.size();
        }

        public int getPageIconResId(int paramInt) {
            return icons[paramInt];
        }

        public Object instantiateItem(ViewGroup paramViewGroup, int paramInt) {
            View localObject;
            if (paramInt == 0) {
                localObject = recentsWrap;
            } else if (paramInt == 6) {
                localObject = emojiWrap;
            } else {
                localObject = views.get(paramInt);
            }
            paramViewGroup.addView(localObject);
            return localObject;
        }

        public boolean isViewFromObject(View paramView, Object paramObject) {
            return paramView == paramObject;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }
    }
}