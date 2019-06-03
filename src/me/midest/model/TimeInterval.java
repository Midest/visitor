package me.midest.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Set;

public class TimeInterval implements Comparable<TimeInterval> {

    protected static final DateTimeFormatter tf = DateTimeFormatter.ofPattern( "HH:mm" );
    private static Set<TimeInterval> intervalList = new LinkedHashSet<>();

    public static void clearCache(){
        intervalList.clear();
    }

    public static TimeInterval create( String bounds ){
        return new TimeInterval( bounds );
    }

    private LocalTime start;
    private LocalTime end;

    public LocalTime getStart() {
        return start;
    }

    public void setStart( LocalTime start ) {
        this.start = start;
        normalize();
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd( LocalTime end ) {
        this.end = end;
        normalize();
    }

    public void setBounds( String bounds ){
        String bound[] = bounds.trim().replaceAll( "[\\s-]+", " " ).split( " " );
        String first[] = bound[0].split( ":" );
        String second[] = bound[1].split( ":" );
        start = LocalTime.of( Integer.valueOf( first[0] ), Integer.valueOf( first[1] ) );
        end = LocalTime.of(  Integer.valueOf( second[0] ), Integer.valueOf( second[1] )  );
        normalize();
    }

    public boolean isEmpty(){
        return start == null || end == null;
    }

    public static TimeInterval EMPTY = new TimeInterval();
    static {
        intervalList.add( EMPTY );
    }

    protected TimeInterval(){}

    private TimeInterval( String bounds ){
        setBounds( bounds );
        intervalList.add( this );
    }

    protected boolean hasTime(){
        return getStart() != null;
    }

    /**
     * Исправление неправильного порядка времен начала и конца.
     * Если есть только одно значение времени, второе приравнивается к нему.
     */
    protected void normalize() {
        if( start != null && end != null
                && start.isAfter( end )){
            LocalTime t = end;
            end = start;
            start = t;
        }
        if( start == null && end != null )
            start = end;
        if( end == null && start != null )
            end = start;
    }

    public boolean overlaps( TimeInterval that ){
        return !( ( this == EMPTY || that == EMPTY )
                || ( this.hasTime() && that.hasTime() && this.start.isBefore( that.start ) && !this.end.isAfter( that.start ))
                || ( this.hasTime() && that.hasTime() && !this.start.isBefore( that.end ) && this.end.isAfter( that.end ))
                );
    }

    /**
     * Два объекта {@link TimeInterval} равны, если {@link #overlaps(TimeInterval) пересекаются}.
     * @param other объект для сравнения
     * @return
     */
    @Override
    public boolean equals( Object other ) {
        if( this == other ) return true;
        if( other == null ) return false;
        if( !(other instanceof TimeInterval )) return false;
        final TimeInterval that = (TimeInterval)other;
        return this.overlaps( that );
    }

    public boolean strictEquals( TimeInterval that ){
        return that != null && (
                    ( !this.hasTime() && !that.hasTime() ) ||
                    ( this.hasTime() && this.getStart().equals( that.getStart())
                        && this.getEnd().equals( that.getEnd()))
                );
    }

    @Override
    public int compareTo( TimeInterval that ) {
        return this.start.equals( that.start ) ?
                this.end.compareTo( that.end ) : this.start.compareTo( that.start );
    }

    @Override
    public int hashCode() {
        int i = 0;
        for( TimeInterval ti : intervalList ) {
            i++;
            if( ti.equals( this ) )
                return i * 179 * TimeInterval.class.getName().hashCode();
        }
        return ( start == null ? 0 : start.hashCode())
                + ( end == null ? 0 : 29 * end.hashCode());
    }

    @Override
    public String toString() {
        return tf.format( start ) + "-" + tf.format( end );
    }
}
