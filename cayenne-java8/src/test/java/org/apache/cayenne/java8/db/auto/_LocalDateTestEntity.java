package org.apache.cayenne.java8.db.auto;

import java.time.LocalDate;

import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.exp.Property;

/**
 * Class _LocalDateTestEntity was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _LocalDateTestEntity extends CayenneDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String ID_PK_COLUMN = "ID";

    public static final Property<LocalDate> DATE = new Property<LocalDate>("date");

    public void setDate(LocalDate date) {
        writeProperty("date", date);
    }
    public LocalDate getDate() {
        return (LocalDate)readProperty("date");
    }

}