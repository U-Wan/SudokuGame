package com.tsu.sudokugame.controler.helper;

import static com.tsu.sudokugame.controler.helper.SudokuButtonType.Spacer;
import static com.tsu.sudokugame.controler.helper.SudokuButtonType.getSpecialButtons;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.tsu.sudokugame.controler.GameController;
import com.tsu.sudokugame.model.game.listener.IHighlightChangedListener;
import com.tsu.sudokugame.ui.view.R;
import com.tsu.sudokugame.controler.listener.IHintDialogFragmentListener;

import java.util.LinkedList;


public class SudokuSpecialButtonLayout extends LinearLayout implements IHighlightChangedListener {


    SudokuSpecialButton[] fixedButtons;
    public int fixedButtonsCount = getSpecialButtons().size();
    GameController gameController;
    SudokuKeyboardLayout keyboard;

    FragmentManager fragmentManager;
    Context context;
    float buttonMargin;

    OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v instanceof SudokuSpecialButton) {
                SudokuSpecialButton btn = (SudokuSpecialButton)v;

                //int row = gameController.getSelectedRow();
                //int col = gameController.getSelectedCol();

                switch(btn.getType()) {
                    case Delete:
                        gameController.deleteSelectedCellsValue();
                        break;
                    case Do:
                        gameController.ReDo();
                        break;
                    case Undo:
                        gameController.UnDo();
                        break;
                    case Hint:
                        if(gameController.isValidCellSelected()) {
                            if(gameController.getUsedHints() == 0 && !gameController.gameIsCustom()) {
                                // are you sure you want to use a hint?
                                HintConfirmationDialog hintDialog = new HintConfirmationDialog();
                                hintDialog.show(fragmentManager, "HintDialogFragment");

                            } else {
                                gameController.hint();
                            }
                        } else {
                            // Display a Toast that explains how to use the Hint function.
                            Toast t = Toast.makeText(getContext(), R.string.hint_usage, Toast.LENGTH_SHORT);
                            t.show();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };


    public SudokuSpecialButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SudokuSpecialButtonLayout);
        buttonMargin = a.getDimension(R.styleable.SudokuSpecialButtonLayout_sudokuSpecialKeyboardMargin, 5f);
        a.recycle();

        setWeightSum(fixedButtonsCount);
        this.context = context;
    }

    public void setButtonsEnabled(boolean enabled) {
        for(SudokuSpecialButton b : fixedButtons) {
            b.setEnabled(enabled);
        }
    }

    public void setButtons(int width, GameController gc, SudokuKeyboardLayout key, FragmentManager fm, int orientation, Context cxt) {
        fragmentManager = fm;
        keyboard=key;
        gameController = gc;
        context = cxt;
        if(gameController != null) {
            gameController.registerHighlightChangedListener(this);
        }
        fixedButtons = new SudokuSpecialButton[fixedButtonsCount];
        LayoutParams p;
        int i = 0;
        //ArrayList<SudokuButtonType> type = (ArrayList<SudokuButtonType>) SudokuButtonType.getSpecialButtons();
        for (SudokuButtonType t : getSpecialButtons()){
            fixedButtons[i] = new SudokuSpecialButton(getContext(),null);
            if(orientation == LinearLayout.HORIZONTAL) {
                p = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);
            } else {
                p = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
            }
            fixedButtons[i].setPadding((int)buttonMargin*5, 0, (int)buttonMargin*5, 0);
            p.setMargins((int)buttonMargin, (int)buttonMargin, (int)buttonMargin, (int)buttonMargin);

            //int width2 =width/(fixedButtonsCount);
            //p.width= width2-15;
            if(t == Spacer) {
                fixedButtons[i].setVisibility(View.INVISIBLE);
            }

            fixedButtons[i].setLayoutParams(p);
            fixedButtons[i].setType(t);
            fixedButtons[i].setImageDrawable(ContextCompat.getDrawable(context, fixedButtons[i].getType().getResID()));
            fixedButtons[i].setScaleType(ImageView.ScaleType.FIT_XY);
            fixedButtons[i].setAdjustViewBounds(true);
            fixedButtons[i].setOnClickListener(listener);
            fixedButtons[i].setBackgroundResource(R.drawable.numpad_highlighted_four);
            addView(fixedButtons[i]);

            i++;
        }

    }

    @Override
    public void onHighlightChanged() {
        for(int i = 0; i < fixedButtons.length; i++) {
            switch(fixedButtons[i].getType()) {
                case Undo:
                    fixedButtons[i].setBackgroundResource(gameController.isUndoAvailable() ?
                            R.drawable.numpad_highlighted_four : R.drawable.button_inactive);
                    break;
                case Do:
                    fixedButtons[i].setBackgroundResource(gameController.isRedoAvailable() ?
                            R.drawable.numpad_highlighted_four : R.drawable.button_inactive);
                    break;
                default:
                    break;
            }
        }
    }

    public static class HintConfirmationDialog extends DialogFragment {

        LinkedList<IHintDialogFragmentListener> listeners = new LinkedList<>();

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            // Verify that the host activity implements the callback interface
            if(activity instanceof IHintDialogFragmentListener) {
                listeners.add((IHintDialogFragmentListener) activity);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
            builder.setMessage(R.string.hint_confirmation)
                    .setPositiveButton(R.string.hint_confirmation_confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            for(IHintDialogFragmentListener l : listeners) {
                                l.onHintDialogPositiveClick();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            return builder.create();
        }
    }
}
