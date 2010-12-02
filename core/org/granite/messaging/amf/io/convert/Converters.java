/*
  GRANITE DATA SERVICES
  Copyright (C) 2007-2010 ADEQUATE SYSTEMS SARL

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.messaging.amf.io.convert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 *
 * @see Converter
 * @see Reverter
 */
public class Converters {

    /** Array of all configured converters */
    private final Converter[] converters;

    /** Array of all configured reverters */
    private final Reverter[] reverters;

    /**
     * Constructs a new Converters instance with the supplied list of converters (possibly reverters).
     *
     * @param converterClasses the list of all used converters.
     * @throws NoSuchMethodException if one of the Converter does not have a constructor with a
     * 		Converters parameter.
     * @throws IllegalAccessException if something goes wrong when creating an instance of one
     * 		of the supplied Converter classes.
     * @throws InvocationTargetException if something goes wrong when creating an instance of one
     * 		of the supplied Converter classes.
     * @throws InstantiationException if something goes wrong when creating an instance of one
     * 		of the supplied Converter classes.
     */
    public Converters(List<Class<? extends Converter>> converterClasses)
        throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        List<Converter> converters = new ArrayList<Converter>();
        List<Reverter> reverters = new ArrayList<Reverter>();

        if (converterClasses != null) {
            for (Class<? extends Converter> converterClass : converterClasses) {
                Constructor<? extends Converter> constructor = converterClass.getConstructor(Converters.class);
                Converter converter = constructor.newInstance(this);
                converters.add(converter);
                if (converter instanceof Reverter)
                    reverters.add((Reverter)converter);
            }
        }

        this.converters = converters.toArray(new Converter[converters.size()]);
        this.reverters = reverters.toArray(new Reverter[reverters.size()]);
    }

    /**
     * Returns a suitable converter for supplied parameters or null if no converter
     * can be found. This method is equivalent to the
     * {@link Converters#getConverter(Object, Type, boolean)} method with the
     * throwNotFoundException parameter set to false.
     *
     * @param value the value to be converted
     * @param targetType the type of the converted value
     * @return a Converter instance or null if no suitable converter can be found
     */
    public Converter getConverter(Object value, Type targetType) {
        return getConverter(value, targetType, false);
    }

    /**
     * Returns a suitable converter for supplied parameters or either returns null if no converter
     * can be found or throws a {@link NoConverterFoundException}.
     *
     * @param value the value to be converted
     * @param targetType the type of the converted value
     * @param throwNotFoundException should an exception be thrown if no converter is found?
     * @return a Converter instance or null if no suitable converter can be found
     * @throws NoConverterFoundException if the throwNotFoundException parameter is set to true
     * 		and no converter can be found.
     */
    public Converter getConverter(Object value, Type targetType, boolean throwNotFoundException)
        throws NoConverterFoundException {
    	
    	// Small optimization: this avoids to make TypeVariable conversion in all converters...
    	if (targetType instanceof TypeVariable<?>)
    		targetType = ClassUtil.getBoundType((TypeVariable<?>)targetType);
    	
        for (Converter converter : converters) {
            if (converter.canConvert(value, targetType))
                return converter;
        }

        if (!throwNotFoundException)
            return null;

        throw new NoConverterFoundException(value, targetType);
    }

    /**
     * Converts the supplied object to the supplied target type. This method is
     * a simple shortcut for: <tt>this.getConverter(value, target, true).convert(value, targetType)</tt>.
     *
     * @param value the object to be converted.
     * @param targetType the target type.
     * @return the converted object.
     * @throws NoConverterFoundException if no suitable converter can be found.
     */
    public Object convert(Object value, Type targetType) throws NoConverterFoundException {
        return getConverter(value, targetType, true).convert(value, targetType);
    }

    /**
     * Returns true if at least one reverter is configured for this Converters instance.
     *
     * @return true if at least one reverter is configured for this Converters instance.
     */
    public boolean hasReverters() {
        return reverters.length > 0;
    }

    /**
     * Revert back to standard, AMF3 known Java type the supplied value. This method iterates
     * on all configured Reverters and returns the {@link Reverter#revert(Object)} method result
     * if the {@link Reverter#canRevert(Object)} method returns true for the current Reverter
     * instance.
     *
     * @param value the value to be reverted.
     * @return the reverted value (same instance if none of the configured reverters apply).
     */
    public Object revert(Object value) {
        for (Reverter reverter : reverters) {
            if (reverter.canRevert(value))
                return reverter.revert(value);
        }
        return value;
    }
    
    public Converter[] getConverters() {
    	Converter[] copy = new Converter[converters.length];
    	System.arraycopy(converters, 0, copy, 0, converters.length);
    	return copy;
    }
}
