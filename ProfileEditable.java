package model;

public interface ProfileEditable {
    String[] getEditableFields();
    String[] getNonEditableFields();
    boolean isFieldEditable(String fieldName);
    void updateEditableField(String fieldName, String value);
    String getFieldValue(String fieldName);
}
