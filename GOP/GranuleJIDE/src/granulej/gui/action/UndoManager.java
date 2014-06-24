package granulej.gui.action;

import java.util.ArrayList;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

public class UndoManager extends AbstractUndoableEdit implements UndoableEditListener {
    String lastEditName=null;
    ArrayList<MyCompoundEdit> edits=new ArrayList<MyCompoundEdit>();
    MyCompoundEdit current;
    int pointer=-1;

    public void undoableEditHappened(UndoableEditEvent e) {
        UndoableEdit edit=e.getEdit();
        if (edit instanceof AbstractDocument.DefaultDocumentEvent) {
            try {
                AbstractDocument.DefaultDocumentEvent event=(AbstractDocument.DefaultDocumentEvent)edit;
                int start=event.getOffset();
                int len=event.getLength();
                String text=event.getDocument().getText(start, len);
                boolean isNeedStart=false;
                if (current==null) {
                    isNeedStart=true;
                }
                else if (text.contains("\n")) {
                    isNeedStart=true;
                }
                else if (lastEditName==null || !lastEditName.equals(edit.getPresentationName())) {
                    isNeedStart=true;
                }

                while (pointer<edits.size()-1) {
                    edits.remove(edits.size()-1);
                    isNeedStart=true;
                }
                if (isNeedStart) {
                    createCompoundEdit();
                }

                current.addEdit(edit);
                lastEditName=edit.getPresentationName();

                refreshControls();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void createCompoundEdit() {
        if (current==null) {
            current= new MyCompoundEdit();
        }
        else if (current.getLength()>0) {
            current= new MyCompoundEdit();
        }

        edits.add(current);
        pointer++;
    }

    public void undo() throws CannotUndoException {
        if (!canUndo()) {
            throw new CannotUndoException();
        }

        MyCompoundEdit u=edits.get(pointer);
        u.undo();
        pointer--;

        refreshControls();
    }

    public void redo() throws CannotUndoException {
        if (!canRedo()) {
            throw new CannotUndoException();
        }

        pointer++;
        MyCompoundEdit u=edits.get(pointer);
        u.redo();

        refreshControls();
    }

    public boolean canUndo() {
        return pointer>=0;
    }

    public boolean canRedo() {
        return edits.size()>0 && pointer<edits.size()-1;
    }

    public void refreshControls() {
//        btnUndo.setEnabled(canUndo());
//        btnRedo.setEnabled(canRedo());
    }
    
    class MyCompoundEdit extends CompoundEdit {
        boolean isUnDone=false;
        public int getLength() {
            return edits.size();
        }

        public void undo() throws CannotUndoException {
            super.undo();
            isUnDone=true;
        }
        public void redo() throws CannotUndoException {
            super.redo();
            isUnDone=false;
        }
        public boolean canUndo() {
            return edits.size()>0 && !isUnDone;
        }

        public boolean canRedo() {
            return edits.size()>0 && isUnDone;
        }

    }
    
    
}
