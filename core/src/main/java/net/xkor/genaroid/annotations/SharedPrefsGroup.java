package net.xkor.genaroid.annotations;

import android.content.Context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface SharedPrefsGroup {

    /**
     * Represents the scope of a SharedPreference.
     */
    enum Scope {
        /**
         * The default shared SharedPreference.
         */
        APPLICATION_DEFAULT, //
        /**
         * The name of the SharedPreference will contain the name of the
         * Activity and the name annotated interface.
         */
        ACTIVITY, //

        /**
         * The name of the SharedPreference will contain the name of the
         * Activity (also available through activity.getPreferences()).
         */
        ACTIVITY_DEFAULT, //

        /**
         * The name of the SharedPreference will be the name of the annotated
         * interface.
         */
        UNIQUE;
    }

    /**
     * The scope of the preferences, this will change the name of the
     * SharedPreference.
     *
     * @return the scope of the preferences
     */
    Scope value() default Scope.ACTIVITY;

    /**
     * The operating mode.
     *
     * @return the operating mode
     * @see Context#MODE_PRIVATE
     * @see Context#MODE_WORLD_READABLE
     * @see Context#MODE_WORLD_WRITEABLE
     */
    int mode() default Context.MODE_PRIVATE;
}