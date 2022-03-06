package edu.illinois.cs.statecapture;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

import java.lang.reflect.Field;

public class CustomReflectionConverter extends ReflectionConverter {

    public CustomReflectionConverter(final Mapper mapper, final ReflectionProvider reflectionProvider) {
        super(mapper, reflectionProvider);
    }

    @Override
    public Object doUnmarshal(final Object result, final HierarchicalStreamReader reader,
                              final UnmarshallingContext context) {
        if (!UnmarshalChain.isInitialized()) {
            UnmarshalChain.initializeChain(System.getProperty("currentClassInXStream"), System.getProperty("currentFieldInXStream"));
        }
        super.doUnmarshal(result, reader, context);
        return result;
    }

    @Override
    protected Object unmarshallField(final UnmarshallingContext context, final Object result, final Class<?> type,
                                     final Field field) {
        // Assume properties define the root node and should be initialized as such if not yet
        System.err.println("invoke unmarshallField;");
        if (!UnmarshalChain.isInitialized()) {
            UnmarshalChain.initializeChain(System.getProperty("currentClassInXStream"), System.getProperty("currentFieldInXStream"));
            System.out.println("INITIALIZED TO " + System.getProperty("currentClassInXStream") + "::" + System.getProperty("currentFieldInXStream"));
        }
        UnmarshalChain.pushNode(UnmarshalChain.makeUnmarshalFieldNode(field.getDeclaringClass().getName(), field.getName()));
        try {
            return context.convertAnother(result, type, mapper.getLocalConverter(field.getDeclaringClass(), field
                    .getName()));
        } catch (ConversionException ce) {
            ce.printStackTrace();
            try {
                return UnmarshalChain.getCurrObject();
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
                throw new ConversionException(e);
            }
        } finally {
            UnmarshalChain.popNode();
            System.out.println("FINISHED RETRIEVING AND POPPING FOR " + field.getDeclaringClass().getName() + "::" + field.getName());
        }
    }


}