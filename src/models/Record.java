package models;

public class Record {
    private String key;
    private String value;

    public Record(String record) {
        setRecord(record);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setRecord(String record) {
        String[] properties = record.split(":");
        key = properties[0];
        value = properties[1];
    }

    public boolean equals(String key) {
        return key.equals(this.key) ;
    }

    @Override
    public String toString() {
        return key + ":" + value;
    }
}