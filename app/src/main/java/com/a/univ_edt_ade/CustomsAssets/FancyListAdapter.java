package com.a.univ_edt_ade.CustomsAssets;

import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;

import com.a.univ_edt_ade.R;

/**
 * Created by 7 on 12/02/2018.
 */

public abstract class FancyListAdapter extends RecyclerView.Adapter<FancyListAdapter.ItemHolder> {

    private final int cardLayout;
    private final int cardBackground;


    public FancyListAdapter(@LayoutRes int cardLayout, @DrawableRes int cardBackground) {
        this.cardLayout = cardLayout;
        this.cardBackground = cardBackground;
    }


    public class ItemHolder extends RecyclerView.ViewHolder {
        public CardView cardView;
        public ItemHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater
                .from(parent.getContext())
                .inflate(cardLayout, parent, false);

        TransitionDrawable background = (TransitionDrawable) cardView.getContext().getResources().getDrawable(cardBackground);
        cardView.setBackground(background);

        return new ItemHolder(cardView);
    }


    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
        if (position < getItemCount() - 1) {
            holder.cardView.setOnLongClickListener(new CardViewLongClickListener(holder));

        } else {
            // dernier élément
            ((ImageView) holder.cardView.getChildAt(0)).setImageResource(R.drawable.ic_add_24dp);
            holder.setIsRecyclable(false);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemOnClick(holder.getAdapterPosition());
            }
        });
    }


    public abstract void itemOnClick(int adapterPostion);

    public abstract void trashViewOnClick(int adapterPosition);


    private class CardViewLongClickListener implements View.OnLongClickListener {

        private final ItemHolder holder;
        private final ImageView trashView;
        private final Runnable fadeOutRun;

        private int widthOfTextViewBeforeResizing = 0;

        public CardViewLongClickListener(ItemHolder vHolder) {
            this.holder = vHolder;
            trashView = (ImageView) holder.cardView.findViewById(R.id.deleteView);

            trashView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (trashView.getVisibility() == View.VISIBLE) {
                        trashViewOnClick(holder.getAdapterPosition());

                        trashView.setVisibility(View.GONE);
                    }
                }
            });

            fadeOutRun = new Runnable() {
                @Override
                public void run() {
                    Animation fadeOutAnim = AnimationUtils.loadAnimation(trashView.getContext(), R.anim.fadeout);
                    fadeOutAnim.setAnimationListener(new FadeOutAnimListener(trashView));
                    trashView.startAnimation(fadeOutAnim);

                    if (widthOfTextViewBeforeResizing > 0) {
                        animateTheTextViewInReverse();
                    }
                }
            };
        }


        @Override
        public boolean onLongClick(View v) {
            switch (trashView.getVisibility()) {
                case View.VISIBLE:
                    holder.cardView.removeCallbacks(fadeOutRun);
                    break;

                case View.GONE:
                    trashView.setVisibility(View.VISIBLE);

                    Animation fadeInAnim = AnimationUtils.loadAnimation(trashView.getContext(), R.anim.fadein);
                    fadeInAnim.setFillAfter(true);
                    trashView.startAnimation(fadeInAnim);

                    animateTextViewIfNecessary();

                    break;
            }

            final TransitionDrawable transition = (TransitionDrawable) v.getBackground();
            transition.startTransition(250);

            holder.cardView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    transition.reverseTransition(250);
                }
            }, 250);

            holder.cardView.postDelayed(fadeOutRun, 2000);

            return true;
        }


        private void animateTextViewIfNecessary() {
            int parentWidth = holder.cardView.getMeasuredWidth();

            // la taille de l'icône de la cardView est la même que celle de 'trashView'
            int trashViewWidth = holder.cardView.getChildAt(0).getMeasuredWidth();

            // la marge à gauche et à droite sont les mêmes pour toutes les views de la cardView
            // il y a 3 marges en tout
            int margins = ((ViewGroup.MarginLayoutParams) holder.cardView.getChildAt(0).getLayoutParams())
                    .leftMargin * 3;

            int textViewWidth = holder.cardView.getChildAt(1).getMeasuredWidth();

            if (textViewWidth + 2 * trashViewWidth + margins > parentWidth) {
                // 'textView' prend alors trop de place et va empiéter sur 'trashView' lorsqu'elle
                // est à sa position finale
                TextViewResizerAnimation textAnim =  new TextViewResizerAnimation(
                        holder.cardView.getChildAt(1),
                        parentWidth - trashViewWidth * 2 - margins,
                        textViewWidth
                );
                textAnim.setDuration(250);
                holder.cardView.getChildAt(1).startAnimation(textAnim);

                widthOfTextViewBeforeResizing = textViewWidth;
            }
        }

        private void animateTheTextViewInReverse() {
            final View textView = holder.cardView.getChildAt(1);

            int textViewWidth = textView.getMeasuredWidth();

            TextViewResizerAnimation textAnim = new TextViewResizerAnimation(
                    textView,
                    widthOfTextViewBeforeResizing,
                    textViewWidth
            );

            textAnim.setDuration(250);
            textView.startAnimation(textAnim);
        }
    }

    private class FadeOutAnimListener implements Animation.AnimationListener {

        private final View view;
        public FadeOutAnimListener(View v) {
            view = v;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (view.getVisibility() == View.VISIBLE)
                view.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }


    /**
     * basé sur :
     * https://stackoverflow.com/a/33095268/8662187
     */
    private class TextViewResizerAnimation extends Animation {
        private final int targetWidth;
        private View textView;
        private int startWidth;

        public TextViewResizerAnimation(View textView, int targetWidth, int startWidth) {
            this.textView = textView;
            this.targetWidth = targetWidth;
            this.startWidth = startWidth;
        }


        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            textView.getLayoutParams().width = startWidth + (int) ((targetWidth - startWidth) * interpolatedTime);
            textView.requestLayout();
        }

        @Override
        public void initialize(int width, int height, int parentWidth, int parentHeight) {
            super.initialize(width, height, parentWidth, parentHeight);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
