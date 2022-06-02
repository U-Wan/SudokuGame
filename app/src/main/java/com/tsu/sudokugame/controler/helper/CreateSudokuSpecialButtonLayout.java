package com.tsu.sudokugame.controler.helper;

import static com.tsu.sudokugame.controler.helper.CreateSudokuButtonType.getSpecialButtons;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.tsu.sudokugame.controler.GameController;
import com.tsu.sudokugame.model.game.listener.IHighlightChangedListener;
import com.tsu.sudokugame.ui.view.R;
import com.tsu.sudokugame.controler.listener.IFinalizeDialogFragmentListener;

import java.util.LinkedList;

public class CreateSudokuSpecialButtonLayout extends LinearLayout implements IHighlightChangedListener {

    CreateSudokuSpecialButton[] fixedButtons;
    public int fixedButtonsCount = getSpecialButtons().size();
    GameController gameController;
    SudokuKeyboardLayout keyboard;
    Bitmap bitMap,bitResult;
    Canvas canvas;
    FragmentManager fragmentManager;
    Context context;
    float buttonMargin;

    OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v instanceof CreateSudokuSpecialButton) {
                CreateSudokuSpecialButton btn = (CreateSudokuSpecialButton)v;

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
                    case Finalize:
                        FinalizeConfirmationDialog dialog = new FinalizeConfirmationDialog();
                        dialog.show(fragmentManager, "FinalizeDialogFragment");
                    default:
                        break;
                }
            }
        }
    };

    public CreateSudokuSpecialButtonLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CreateSudokuSpecialButtonLayout);
        buttonMargin = a.getDimension(R.styleable.CreateSudokuSpecialButtonLayout_createSudokuSpecialKeyboardMargin, 5f);
        a.recycle();

        setWeightSum(fixedButtonsCount);
        this.context = context;
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

    private void setUpVectorDrawable(Drawable drawable) {
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        bitMap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        bitResult = Bitmap.createBitmap(bitMap.getWidth(), bitMap.getHeight(), Bitmap.Config.ARGB_8888);

        canvas = new Canvas(bitResult);
    }

    public static class FinalizeConfirmationDialog extends DialogFragment {

        LinkedList<IFinalizeDialogFragmentListener> listeners = new LinkedList<>();

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            // Verify that the host activity implements the callback interface
            if(activity instanceof IFinalizeDialogFragmentListener) {
                listeners.add((IFinalizeDialogFragmentListener) activity);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog);
            builder.setMessage("Do you wish to finalize this sudoku?")
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            for(IFinalizeDialogFragmentListener l : listeners) {
                                l.onFinalizeDialogPositiveClick();
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
