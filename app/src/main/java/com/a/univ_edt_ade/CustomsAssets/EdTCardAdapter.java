package com.a.univ_edt_ade.CustomsAssets;

import android.graphics.drawable.TransitionDrawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.TextView;

import com.a.univ_edt_ade.R;

/**
 * Created by 7 on 14/01/2018.
 */

public class EdTCardAdapter extends RecyclerView.Adapter<EdTCardAdapter.ViewHolder> {

    private String[] EdTList;

    public EdTCardAdapter(String[] EdTList) {
        this.EdTList = EdTList;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        public ViewHolder(CardView view) {
            super(view);
            cardView = view;
        }
    }


    @Override
    public EdTCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.edt_list_card, parent, false);

        TransitionDrawable background = (TransitionDrawable) cardView.getContext().getResources().getDrawable(R.drawable.edt_list_card_background);
        cardView.setBackground(background);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(final EdTCardAdapter.ViewHolder holder, int position) {
        if (position < EdTList.length) {
            //Log.d("EdTCardAdapter", "Child at pos : " + position + " - name : " + EdTList[position]);
            ((TextView) holder.cardView.getChildAt(1)).setText(EdTList[position]);

            holder.cardView.setOnLongClickListener(new CardViewLongClickListener(holder));

        } else {
            Log.d("EdTCardAdapter", "Child at pos : " + position + " - name : new EdT");

            // la cardView supplémentaire utilisée pour rajouter un nouvel emploi du temps
            ((TextView) holder.cardView.getChildAt(1)).setText("Nouveau...");

            ((ImageView) holder.cardView.getChildAt(0)).setImageResource(R.drawable.ic_add_24dp);

            holder.setIsRecyclable(false);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int pos = holder.getAdapterPosition();

                        ((edtAdapterCallback) v.getContext())
                                .makeEdTInfoFrag(pos, pos < EdTList.length ? EdTList[pos] : "Nouvel Emploi du Temps");
                    }
                });
    }

    @Override
    public int getItemCount() {
        return EdTList.length + 1;
    }


    public void updateData() {
        Log.d("EdTCardAdapter", "Updating data...");

        EdTList = com.a.univ_edt_ade.Activities.EdTList.getEdTNames();

        notifyDataSetChanged();
    }

    public interface edtAdapterCallback {
        void makeEdTInfoFrag(int pos, String name);

        void deleteEdTAt(int pos);
    }



    private class CardViewLongClickListener implements View.OnLongClickListener {

        private final ViewHolder holder;
        private final ImageView trashView;
        private final Runnable fadeOutRun;
        private int widthOfTextViewBeforeResizing = 0;
        public CardViewLongClickListener(ViewHolder vHolder) {
            this.holder = vHolder;
            trashView = (ImageView) holder.cardView.findViewById(R.id.deleteView);

            trashView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (trashView.getVisibility() == View.VISIBLE) {
                        ((edtAdapterCallback) v.getContext()).deleteEdTAt(holder.getAdapterPosition());

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


        private boolean animateTextViewIfNecessary() {
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

                return true;
            }

            return false;
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
