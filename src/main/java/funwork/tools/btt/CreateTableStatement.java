package funwork.tools.btt;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiType;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;




public class CreateTableStatement {


    private StringBuilder bd = new StringBuilder();

    public static final String LPH = "(";
    public static final String RPH = ")";

    private boolean appendFields;

    public String getSql() {
        return bd.toString();
    }

    public CreateTableStatement(boolean cover, String tableName) {
        bd.append("create table");
        if (cover) {
            bd.append(" if not exists");
        }
        bd.append(" ").append(tableName);
    }

    public String convertToBaseLine(String camelName) {
        StringBuilder stringBuilder = new StringBuilder();
        int n = camelName.length();
        for (int i = 0; i < n; i++) {
            char c = camelName.charAt(i);
            if (c >= 65 && c <= 90) {
                stringBuilder.append("_").append((char)( c + 32));
            }else{
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    public void appendField(PsiField field, String type, Integer length) {
        bd.append(getColumnName(field));
        bd.append(" ").append(type);
        visitAnnontation(type,field);
    }

    public String getColumnName(PsiField psiField) {
        PsiAnnotation columnAt = psiField.getAnnotation("javax.persistence.Column");
        if (columnAt != null) {
            String name = AnnotationUtil.getStringAttributeValue(columnAt, "name");
            if (StringUtils.isNotEmpty(name)) {
                return name;
            }
        }
        return convertToBaseLine(psiField.getName());
    }

    public void visitAnnontation(String type, PsiField field) {
        @NotNull PsiAnnotation[] annotations = field.getAnnotations();
        if (annotations == null) {
            return;
        }
        PsiAnnotation idAt = field.getAnnotation("javax.persistence.Id");
        PsiAnnotation columnAt = field.getAnnotation("javax.persistence.Column");
        if (columnAt != null) {
            Long length = AnnotationUtil.getLongAttributeValue(columnAt, "length");
            bd.append("(").append(length).append(") ");
            Boolean nullable = AnnotationUtil.getBooleanAttributeValue(columnAt, "nullable");
            if (nullable != null && !nullable) {
                bd.append("not null");
            }else{
                bd.append("null");
            }
        }
        if (idAt != null) {
            bd.append(" primary key");
        }

    }


    public void visitField(PsiField[] fields) {
        if (appendFields) {
            throw new IllegalStateException();
        }
        bd.append(LPH);
        bd.append("\n");
        for (int i = 0; i < fields.length; i++) {
            PsiField field = fields[i];
            PsiType type = field.getType();
            type.getCanonicalText();
            boolean valid = true;
            switch (type.getPresentableText()) {
                case "Boolean":
                case "boolean":
                    appendField(field, "tinyint", 1);
                case "String":
                    appendField(field, "varchar", 100);
                    break;
                case "int":
                case "Integer":
                    appendField(field, "int", 11);
                    break;
                case "long":
                case "Long":
                    appendField(field, "bigint", 30);
                    break;
                case "LocalDateTime":
                case "Date":
                    appendField(field, "datetime", 0);
                    break;
                default:
                    valid = false;
                    break;
            }
            if (valid && i < fields.length - 1) {
                bd.append(",");
                bd.append("\n");
            }
        }
        bd.deleteCharAt(bd.length() - 1);
        bd.deleteCharAt(bd.length() - 1);

        bd.append("\n");
        bd.append(RPH);
        this.appendFields = true;
    }

}
