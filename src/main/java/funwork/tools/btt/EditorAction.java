package funwork.tools.btt;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class EditorAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {

        PsiElement data1 = anActionEvent.getDataContext().getData(CommonDataKeys.PSI_ELEMENT);
        if (data1 == null) {
            System.out.println("failed");
            return;
        }
        PsiClass psiClass = null;
        if (data1 instanceof PsiClass) {
            psiClass = (PsiClass) data1;
        } else {
            System.out.println("not support");
        }
        CreateTableStatement createTableStatement = new CreateTableStatement(true, psiClass.getName());
        createTableStatement.visitField(psiClass.getFields());

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(createTableStatement.getSql()), null);
    }


    @Override
    public boolean isDumbAware() {
        return false;
    }
}
