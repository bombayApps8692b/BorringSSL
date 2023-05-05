package com.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MContextMenu implements DialogInterface.OnCancelListener, DialogInterface.OnDismissListener {

    private static final int LIST_PREFERED_HEIGHT = 65;

    private MContextMenuAdapter menuAdapter = null;
    private Activity parentActivity = null;
    private int dialogId = 0;

    private MContextMenuOnClickListener clickHandler = null;

    /**
     * constructor
     *
     * @param parent
     * @param id
     */
    public MContextMenu(Activity parent, int id) {
        this.parentActivity = parent;
        this.dialogId = id;

        menuAdapter = new MContextMenuAdapter(parentActivity);
    }

    /**
     * Add menu item
     *
     * @param menuItem
     */
    public void addItem(Resources res, CharSequence title, int imageResourceId,
                        int actionTag) {
        menuAdapter.addItem(new MContextMenuItem(res, title, imageResourceId,
                            actionTag));
    }

    public void addItem(Resources res, int textResourceId, int imageResourceId,
                        int actionTag) {
        menuAdapter.addItem(new MContextMenuItem(res, textResourceId,
                            imageResourceId, actionTag));
    }

    /**
     * Set menu onclick listener
     *
     * @param listener
     */
    public void setOnClickListener(MContextMenuOnClickListener listener) {
        clickHandler = listener;
    }

    /**
     * Create menu
     *
     * @return
     */
    public Dialog createMenu(String menuItitle) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setTitle(menuItitle);
        builder.setAdapter(menuAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialoginterface, int i) {
                MContextMenuItem item = (MContextMenuItem) menuAdapter.getItem(i);

                if (clickHandler != null) {
                    clickHandler.onClick(item.actionTag);
                }
            }
        });

        builder.setInverseBackgroundForced(true);

        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(this);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    public void onCancel(DialogInterface dialog) {
        cleanup();
    }

    public void onDismiss(DialogInterface dialog) {
    }

    @SuppressWarnings("deprecation")
    private void cleanup() {
        parentActivity.dismissDialog(dialogId);
    }

    /**
     * IconContextMenu On Click Listener interface
     */
    public interface MContextMenuOnClickListener {
        public abstract void onClick(int menuId);
    }

    /**
     * Menu-like list adapter with icon
     */
    protected class MContextMenuAdapter extends BaseAdapter {
        private Context context = null;

        private ArrayList<MContextMenuItem> mItems = new ArrayList<MContextMenuItem>();

        public MContextMenuAdapter(Context context) {
            this.context = context;
        }

        /**
         * add item to adapter
         *
         * @param menuItem
         */
        public void addItem(MContextMenuItem menuItem) {
            mItems.add(menuItem);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            MContextMenuItem item = (MContextMenuItem) getItem(position);
            return item.actionTag;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MContextMenuItem item = (MContextMenuItem) getItem(position);

            Resources res = parentActivity.getResources();

            if (convertView == null) {
                TextView temp = new TextView(context);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                    AbsListView.LayoutParams.MATCH_PARENT,
                    AbsListView.LayoutParams.WRAP_CONTENT);
                temp.setLayoutParams(param);
                temp.setPadding((int) toPixel(res, 15), 0,
                                (int) toPixel(res, 15), 0);
                temp.setGravity(android.view.Gravity.CENTER_VERTICAL);

                Theme th = context.getTheme();
                TypedValue tv = new TypedValue();

                if (th.resolveAttribute(
                            android.R.attr.textAppearanceLargeInverse, tv, true)) {
                    temp.setTextAppearance(context, tv.resourceId);
                }

                temp.setMinHeight(LIST_PREFERED_HEIGHT);
                temp.setCompoundDrawablePadding((int) toPixel(res, 14));
                convertView = temp;
            }

            TextView textView = (TextView) convertView;
            textView.setTag(item);
            textView.setText(item.text);
            textView.setCompoundDrawablesWithIntrinsicBounds(item.image, null, null, null);

            return textView;
        }

        private float toPixel(Resources res, int dip) {
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                 dip, res.getDisplayMetrics());
            return px;
        }
    }

    /**
     * menu-like list item with icon
     */
    protected class MContextMenuItem {
        public final CharSequence text;
        public final Drawable image;
        public final int actionTag;

        /**
         * public constructor
         *
         * @param res
         *            resource handler
         * @param textResourceId
         *            id of title in resource
         * @param imageResourceId
         *            id of icon in resource
         * @param actionTag
         *            indicate action of menu item
         */
        public MContextMenuItem(Resources res, int textResourceId,
                                int imageResourceId, int actionTag) {
            text = res.getString(textResourceId);
            if (imageResourceId != -1) {
                image = res.getDrawable(imageResourceId);
            } else {
                image = null;
            }
            this.actionTag = actionTag;
        }

        /**
         * public constructor
         *
         * @param res
         *            resource handler
         * @param title
         *            menu item title
         * @param imageResourceId
         *            id of icon in resource
         * @param actionTag
         *            indicate action of menu item
         */
        public MContextMenuItem(Resources res, CharSequence title,
                                int imageResourceId, int actionTag) {
            text = title;
            if (imageResourceId != -1) {
                image = res.getDrawable(imageResourceId);
            } else {
                image = null;
            }
            this.actionTag = actionTag;
        }
    }
}
