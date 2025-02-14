package io.hypersistence.utils.hibernate.type.array;

import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import io.hypersistence.utils.hibernate.type.model.BaseEntity;
import io.hypersistence.utils.hibernate.util.AbstractTest;
import io.hypersistence.utils.hibernate.util.transaction.JPATransactionFunction;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.junit.Test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Vlad Mihalcea
 */
public class HSQLDBArrayTypeTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
            Event.class,
        };
    }

    @Test
    public void test() {

        final Date date1 = Timestamp.valueOf("1991-12-31 00:00:00");
        final Date date2 = Timestamp.valueOf("1990-01-01 00:00:00");

        doInJPA(new JPATransactionFunction<Void>() {

            @Override
            public Void apply(EntityManager entityManager) {
                Event nullEvent = new Event();
                nullEvent.setId(0L);
                entityManager.persist(nullEvent);

                Event event = new Event();
                event.setId(1L);
                event.setSensorNames(new String[]{"Temperature", "Pressure"});
                event.setSensorValues(new int[]{12, 756});
                event.setSensorLongValues(new long[]{42L, 9223372036854775800L});
                event.setSensorDoubleValues(new double[]{0.123, 456.789});
                event.setDateValues(new Date[]{date1, date2});
                event.setTimestampValues(new Date[]{date1, date2});

                entityManager.persist(event);

                return null;
            }
        });

        doInJPA(new JPATransactionFunction<Void>() {

            @Override
            public Void apply(EntityManager entityManager) {
                Event event = entityManager.find(Event.class, 1L);

                assertArrayEquals(new String[]{"Temperature", "Pressure"}, event.getSensorNames());
                assertArrayEquals(new int[]{12, 756}, event.getSensorValues());
                assertArrayEquals(new long[]{42L, 9223372036854775800L}, event.getSensorLongValues());
                assertArrayEquals(new double[]{0.123, 456.789}, event.getSensorDoubleValues(), 0.01);
                assertEquals(date1.getTime(), event.getDateValues()[0].getTime());
                assertEquals(date2.getTime(), event.getDateValues()[1].getTime());
                assertArrayEquals(new Date[]{date1, date2}, event.getTimestampValues());

                return null;
            }
        });
    }

    @Entity(name = "Event")
    @Table(name = "event")
    @TypeDefs({
        @TypeDef(
            name = "hsqldb-double-array",
            typeClass = DoubleArrayType.class,
            parameters = {
                @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE, value = "double")
            }
        ),
        @TypeDef(
            name = "hsqldb-string-array",
            typeClass = StringArrayType.class,
            parameters = {
                @Parameter(name = AbstractArrayType.SQL_ARRAY_TYPE, value = "varchar")
            }
        )
    })
    public static class Event extends BaseEntity {

        @Type(type = "hsqldb-string-array")
        @Column(name = "sensor_names", columnDefinition = "VARCHAR(20) ARRAY[10]")
        private String[] sensorNames;

        @Type(type = "int-array")
        @Column(name = "sensor_values", columnDefinition = "INT ARRAY DEFAULT ARRAY[]")
        private int[] sensorValues;

        @Type(type = "long-array")
        @Column(name = "sensor_long_values", columnDefinition = "BIGINT ARRAY DEFAULT ARRAY[]")
        private long[] sensorLongValues;

        @Type(type = "hsqldb-double-array")
        @Column(name = "sensor_double_values", columnDefinition = "DOUBLE ARRAY DEFAULT ARRAY[]")
        private double[] sensorDoubleValues;

        @Type(type = "date-array")
        @Column(name = "date_values", columnDefinition = "DATE ARRAY DEFAULT ARRAY[]")
        private Date[] dateValues;

        @Type(type = "timestamp-array")
        @Column(name = "timestamp_values", columnDefinition = "TIMESTAMP ARRAY DEFAULT ARRAY[]")
        private Date[] timestampValues;

        public String[] getSensorNames() {
            return sensorNames;
        }

        public void setSensorNames(String[] sensorNames) {
            this.sensorNames = sensorNames;
        }

        public int[] getSensorValues() {
            return sensorValues;
        }

        public void setSensorValues(int[] sensorValues) {
            this.sensorValues = sensorValues;
        }

        public long[] getSensorLongValues() {
            return sensorLongValues;
        }

        public void setSensorLongValues(long[] sensorLongValues) {
            this.sensorLongValues = sensorLongValues;
        }

        public double[] getSensorDoubleValues() {
            return sensorDoubleValues;
        }

        public void setSensorDoubleValues(double[] sensorDoubleValues) {
            this.sensorDoubleValues = sensorDoubleValues;
        }

        public Date[] getDateValues() {
            return dateValues;
        }

        public void setDateValues(Date[] dateValues) {
            this.dateValues = dateValues;
        }

        public Date[] getTimestampValues() {
            return timestampValues;
        }

        public void setTimestampValues(Date[] timestampValues) {
            this.timestampValues = timestampValues;
        }
    }
}
