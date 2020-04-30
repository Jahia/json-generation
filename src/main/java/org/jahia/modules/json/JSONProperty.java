/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.json;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Christophe Laprun
 */
@XmlRootElement
public class JSONProperty<D extends JSONDecorator<D>> extends JSONItem<Property, D> {
    @XmlElement
    private boolean multiValued;

    @XmlElement
    private Object value;

    @XmlElement
    private boolean reference;

    private boolean isPath;

    private JSONProperty() {
        this(null);
    }

    protected JSONProperty(D decorator) {
        super(decorator);
    }

    protected JSONProperty(D decorator, Property property) throws RepositoryException {
        this(decorator);
        initWith(property);
    }

    public void initWith(Property property) throws RepositoryException {
        super.initWith(property);

        // check whether we need to add a target link
        final int type = property.getType();
        reference = type == PropertyType.PATH || type == PropertyType.REFERENCE || type == PropertyType.WEAKREFERENCE;
        isPath = PropertyType.PATH == type;

        // retrieve value
        this.multiValued = property.isMultiple();
        if (multiValued) {
            final Value[] values = property.getValues();
            value = new Object[values.length];

            for (int i = 0; i < values.length; i++) {
                final Value val = values[i];
                ((Object[]) value)[i] = convertValue(val);
            }
        } else {
            this.value = convertValue(property.getValue());
        }

        getDecoratorOrNullOpIfNull().initFrom(this);
    }

    @Override
    public String getUnescapedTypeName(Property item) throws RepositoryException {
        return getHumanReadablePropertyType(item.getType());
    }

    static String getHumanReadablePropertyType(int type) throws RepositoryException {
        return PropertyType.nameFromValue(type);
    }

    public Object getValue() {
        return value;
    }

    public String getValueAsString() {
        if (multiValued) {
            throw new IllegalStateException("Cannot call getValueAsString on a multi-valued property.");
        }
        return value.toString();
    }

    public String[] getValueAsStringArray() {
        if (!multiValued) {
            throw new IllegalStateException("Cannot call getValueAsStringArray on a simple-valued property.");
        }
        if (value instanceof Object[]) {
            // first check if we already have a String[]
            if (value.getClass().getComponentType().equals(String.class)) {
                return (String[]) value;
            } else {
                Object[] values = (Object[]) value;
                String[] result = new String[values.length];
                int i = 0;
                for (Object o : values) {
                    result[i++] = o.toString();
                }

                return result;
            }
        } else if (value instanceof List) {
            List values = (List) value;

            // first check if we don't already have a List of Strings
            if (!values.isEmpty() && values.get(0) instanceof String) {
                return (String[]) values.toArray(new String[values.size()]);
            } else {
                String[] result = new String[values.size()];
                int i = 0;
                for (Object o : values) {
                    result[i++] = o.toString();
                }

                return result;
            }

        } else {
            throw new IllegalArgumentException("Unknown value type: " + value.getClass().getSimpleName());
        }
    }

    public boolean isMultiValued() {
        return multiValued;
    }

    public static Object convertValue(Value val) throws RepositoryException {
        Object theValue;
        if (val == null) {
            return null;
        }
        switch (val.getType()) {
            case PropertyType.BINARY:
                theValue = val.getString();
                break;
            case PropertyType.BOOLEAN:
                theValue = val.getBoolean();
                break;
            case PropertyType.DATE:
                theValue = val.getDate();
                break;
            case PropertyType.DOUBLE:
                theValue = val.getDouble();
                break;
            case PropertyType.LONG:
                theValue = val.getLong();
                break;
            case PropertyType.NAME:
                theValue = val.getString();
                break;
            case PropertyType.PATH:
                theValue = val.getString();
                break;
            case PropertyType.REFERENCE:
                theValue = val.getString();
                break;
            case PropertyType.STRING:
                theValue = val.getString();
                break;
            case PropertyType.UNDEFINED:
                theValue = val.getString();
                break;
            default:
                theValue = val.getString();
                break;
        }
        return theValue;
    }

    public boolean isReference() {
        return reference;
    }

    public boolean isPath() {
        return isPath;
    }
}
