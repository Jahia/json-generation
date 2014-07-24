/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * @author Christophe Laprun
 */
@SuppressWarnings("unused")
public class DefaultJSONObjectFactory extends JSONObjectFactory {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        final TypeFactory typeFactory = mapper.getTypeFactory();
        mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(new JacksonAnnotationIntrospector(), new JaxbAnnotationIntrospector(typeFactory)));
    }

    private DefaultJSONObjectFactory() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final DefaultJSONObjectFactory INSTANCE = new DefaultJSONObjectFactory();

        private Holder() {
        }
    }

    public static DefaultJSONObjectFactory getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public JSONDecorator createDecorator() {
        return null;
    }

    public String getAsString(Item item) {
        try {
            JSONBase base;
            if (item instanceof Node) {
                base = createNode((Node) item, 1);
            }
            else {
                base = createProperty((Property) item);
            }

            return getAsString(base);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getAsString(JSONBase jsonBase) {
        try {
            return mapper.writeValueAsString(jsonBase);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
